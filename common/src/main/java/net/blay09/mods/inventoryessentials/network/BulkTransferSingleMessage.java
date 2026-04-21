package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.blay09.mods.inventoryessentials.InventoryEssentialsConfig;
import net.blay09.mods.inventoryessentials.InventoryUtils;
import net.blay09.mods.inventoryessentials.ServerInventoryTransfers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class BulkTransferSingleMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BulkTransferSingleMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(InventoryEssentials.MOD_ID,
            "bulk_transfer_single"));

    private final int slotNumber;

    public BulkTransferSingleMessage(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public static BulkTransferSingleMessage decode(FriendlyByteBuf buf) {
        int slotNumber = buf.readVarInt();
        return new BulkTransferSingleMessage(slotNumber);
    }

    public static void encode(FriendlyByteBuf buf, BulkTransferSingleMessage message) {
        buf.writeVarInt(message.slotNumber);
    }

    public static void handle(ServerPlayer player, BulkTransferSingleMessage message) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu != null && message.slotNumber >= 0 && message.slotNumber < menu.slots.size()) {
            Slot clickedSlot = menu.slots.get(message.slotNumber);

            boolean isProbablyMovingToPlayerInventory = false;
            // If the clicked slot is *not* from the player inventory,
            if (!(clickedSlot.container instanceof Inventory)) {
                // Search for any slot that belongs to the player inventory area (not hotbar)
                isProbablyMovingToPlayerInventory = InventoryUtils.containerContainsPlayerInventory(menu);
            }

            boolean clickedAnArmorItem = clickedSlot.getItem().getItem() instanceof Equipable equipable && equipable.getEquipmentSlot().isArmor();
            boolean isInsideInventory = menu instanceof InventoryMenu;

            if (isProbablyMovingToPlayerInventory) {
                // To avoid O(n²), find empty and non-empty slots beforehand in one loop iteration
                Deque<Slot> emptySlots = new ArrayDeque<>();
                List<Slot> nonEmptySlots = new ArrayList<>();
                for (Slot slot : menu.slots) {
                    if (InventoryUtils.isSameInventory(slot, clickedSlot) || !(slot.container instanceof Inventory)) {
                        continue;
                    }

                    if (slot.hasItem()) {
                        nonEmptySlots.add(slot);
                    } else if (!Inventory.isHotbarSlot(slot.getContainerSlot())) {
                        emptySlots.add(slot);
                    }
                }

                // Now go through each slot that is accessible and belongs to the same inventory as the clicked slot
                for (Slot slot : menu.slots) {
                    if (!slot.mayPickup(player)) {
                        continue;
                    }

                    if (InventoryUtils.isSameInventory(slot, clickedSlot, true)) {
                        // and bulk-transfer each of them using the prefer-inventory behaviour
                        quickTransferSingle(player, menu, emptySlots, nonEmptySlots, slot);
                    }
                }
            } else if (clickedAnArmorItem && isInsideInventory) {
                if (!InventoryEssentialsConfig.getActive().bulkTransferArmorSets) {
                    return;
                }

                // When clicking an equipped armor, un-equip all
                if (clickedSlot.index >= InventoryMenu.ARMOR_SLOT_START && clickedSlot.index < InventoryMenu.ARMOR_SLOT_END) {
                    for (int i = InventoryMenu.ARMOR_SLOT_START; i < InventoryMenu.ARMOR_SLOT_END; i++) {
                        menu.clicked(i, 0, ClickType.QUICK_MOVE, player);
                    }
                    return;
                }

                // Swap current armor with clicked armor set
                final var armorSlots = InventoryUtils.findMatchingArmorSetSlots(menu, clickedSlot);
                final var equipmentSlots = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                for (int i = InventoryMenu.ARMOR_SLOT_START; i < InventoryMenu.ARMOR_SLOT_END; i++) {
                    final var equipmentSlot = equipmentSlots.get(i - InventoryMenu.ARMOR_SLOT_START);
                    final var swapSlot = armorSlots.get(equipmentSlot);
                    if (swapSlot != null) {
                        menu.clicked(i, 0, ClickType.PICKUP, player);
                        menu.clicked(swapSlot.index, 0, ClickType.PICKUP, player);
                        menu.clicked(i, 0, ClickType.PICKUP, player);
                    }
                }
            } else {
                // Just a normal inventory-to-inventory transfer, simply shift-click the items
                for (Slot slot : menu.slots) {
                    if (!slot.mayPickup(player)) {
                        continue;
                    }

                    if (InventoryUtils.isSameInventory(slot, clickedSlot, true)) {
                        ServerInventoryTransfers.singleTransfer(player, menu, slot);
                    }
                }
            }
        }
    }

    private static boolean quickTransferSingle(Player player, AbstractContainerMenu menu, Deque<Slot> emptySlots, List<Slot> nonEmptySlots, Slot slot) {
        final var targetStack = slot.getItem().copy();
        if (targetStack.isEmpty()) {
            return false;
        }

        menu.clicked(slot.index, 0, ClickType.PICKUP, player);

        for (final var nonEmptySlot : nonEmptySlots) {
            final var stack = nonEmptySlot.getItem();
            if (ItemStack.isSameItemSameComponents(targetStack, stack)) {
                boolean hasSpaceLeft = stack.getCount() < Math.min(nonEmptySlot.getMaxStackSize(), nonEmptySlot.getMaxStackSize(stack));
                if (!hasSpaceLeft) {
                    continue;
                }

                menu.clicked(nonEmptySlot.index, 1, ClickType.PICKUP, player);
                ItemStack mouseItem = menu.getCarried();
                if (mouseItem.getCount() < targetStack.getCount()) {
                    menu.clicked(slot.index, 0, ClickType.PICKUP, player);
                    return true;
                }
            }
        }

        for (Iterator<Slot> iterator = emptySlots.iterator(); iterator.hasNext(); ) {
            Slot emptySlot = iterator.next();
            menu.clicked(emptySlot.index, 1, ClickType.PICKUP, player);
            if (emptySlot.hasItem()) {
                nonEmptySlots.add(emptySlot);
                iterator.remove();
            }

            ItemStack mouseItem = menu.getCarried();
            if (mouseItem.getCount() < targetStack.getCount()) {
                menu.clicked(slot.index, 0, ClickType.PICKUP, player);
                return true;
            }
        }

        ItemStack mouseItem = menu.getCarried();
        if (!mouseItem.isEmpty()) {
            menu.clicked(slot.index, 0, ClickType.PICKUP, player);
        }

        return false;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
