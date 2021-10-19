package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.inventoryessentials.InventoryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class BulkTransferAllMessage {

    private final int slotNumber;

    public BulkTransferAllMessage(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public static BulkTransferAllMessage decode(FriendlyByteBuf buf) {
        int slotNumber = buf.readByte();
        return new BulkTransferAllMessage(slotNumber);
    }

    public static void encode(BulkTransferAllMessage message, FriendlyByteBuf buf) {
        buf.writeByte(message.slotNumber);
    }

    public static void handle(ServerPlayer player, BulkTransferAllMessage message) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu != null && message.slotNumber >= 0 && message.slotNumber < menu.slots.size()) {
            Slot clickedSlot = menu.slots.get(message.slotNumber);

            boolean isProbablyMovingToPlayerInventory = false;
            // If the clicked slot is *not* from the player inventory,
            if (!(clickedSlot.container instanceof Inventory)) {
                // Search for any slot that belongs to the player inventory area (not hotbar)
                isProbablyMovingToPlayerInventory = InventoryUtils.containerContainsPlayerInventory(menu);
            }

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
                    } else if (!Inventory.isHotbarSlot(slot.getSlotIndex())) {
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
                        bulkTransferPreferInventory(player, menu, emptySlots, nonEmptySlots, slot);
                    }
                }
            } else {
                // Just a normal inventory-to-inventory transfer, simply shift-click the items
                for (Slot slot : menu.slots) {
                    if (!slot.mayPickup(player)) {
                        continue;
                    }

                    if (InventoryUtils.isSameInventory(slot, clickedSlot, true)) {
                        menu.clicked(slot.index, 0, ClickType.QUICK_MOVE, player);
                    }
                }
            }
        }
    }

    private static boolean bulkTransferPreferInventory(Player player, AbstractContainerMenu menu, Deque<Slot> emptySlots, List<Slot> nonEmptySlots, Slot slot) {
        Inventory playerInventory = player.getInventory();
        ItemStack targetStack = slot.getItem();
        if (targetStack.isEmpty()) {
            return false;
        }

        menu.clicked(slot.index, 0, ClickType.PICKUP, player);

        for (Slot nonEmptySlot : nonEmptySlots) {
            ItemStack stack = slot.getItem();
            if (ItemStack.isSameIgnoreDurability(targetStack, stack)) {
                boolean hasSpaceLeft = stack.getCount() < Math.min(slot.getMaxStackSize(), slot.getMaxStackSize(stack));
                if (!hasSpaceLeft) {
                    continue;
                }

                menu.clicked(nonEmptySlot.index, 0, ClickType.PICKUP, player);
                ItemStack mouseItem = menu.getCarried();
                if (mouseItem.isEmpty()) {
                    return true;
                }
            }
        }

        for (Iterator<Slot> iterator = emptySlots.iterator(); iterator.hasNext(); ) {
            Slot emptySlot = iterator.next();
            menu.clicked(emptySlot.index, 0, ClickType.PICKUP, player);
            if (emptySlot.hasItem()) {
                nonEmptySlots.add(emptySlot);
                iterator.remove();
            }

            ItemStack mouseItem = menu.getCarried();
            if (mouseItem.isEmpty()) {
                return true;
            }
        }

        ItemStack mouseItem = menu.getCarried();
        if (!mouseItem.isEmpty()) {
            menu.clicked(slot.index, 0, ClickType.PICKUP, player);
        }

        return false;
    }
}
