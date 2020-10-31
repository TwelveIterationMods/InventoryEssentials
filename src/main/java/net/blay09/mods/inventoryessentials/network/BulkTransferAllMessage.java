package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.inventoryessentials.InventoryUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

public class BulkTransferAllMessage {

    private final int slotNumber;

    public BulkTransferAllMessage(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public static BulkTransferAllMessage decode(PacketBuffer buf) {
        int slotNumber = buf.readByte();
        return new BulkTransferAllMessage(slotNumber);
    }

    public static void encode(BulkTransferAllMessage message, PacketBuffer buf) {
        buf.writeByte(message.slotNumber);
    }

    public static void handle(BulkTransferAllMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            PlayerEntity player = context.getSender();
            if (player == null) {
                return;
            }

            Container container = player.openContainer;
            if (container != null && message.slotNumber >= 0 && message.slotNumber < container.inventorySlots.size()) {
                Slot clickedSlot = container.inventorySlots.get(message.slotNumber);

                boolean isProbablyMovingToPlayerInventory = false;
                // If the clicked slot is *not* from the player inventory,
                if (!(clickedSlot.inventory instanceof PlayerInventory)) {
                    // Search for any slot that belongs to the player inventory area (not hotbar)
                    isProbablyMovingToPlayerInventory = InventoryUtils.containerContainsPlayerInventory(container);
                }

                if (isProbablyMovingToPlayerInventory) {
                    // To avoid O(n²), find empty and non-empty slots beforehand in one loop iteration
                    Deque<Slot> emptySlots = new ArrayDeque<>();
                    List<Slot> nonEmptySlots = new ArrayList<>();
                    for (Slot slot : container.inventorySlots) {
                        if (InventoryUtils.isSameInventory(slot, clickedSlot) || !(slot.inventory instanceof PlayerInventory)) {
                            continue;
                        }

                        if (slot.getHasStack()) {
                            nonEmptySlots.add(slot);
                        } else if (!PlayerInventory.isHotbar(slot.getSlotIndex())) {
                            emptySlots.add(slot);
                        }
                    }

                    // Now go through each slot that is accessible and belongs to the same inventory as the clicked slot
                    for (Slot slot : container.inventorySlots) {
                        if (!slot.canTakeStack(Objects.requireNonNull(player))) {
                            continue;
                        }

                        if (InventoryUtils.isSameInventory(slot, clickedSlot, true)) {
                            // and bulk-transfer each of them using the prefer-inventory behaviour
                            bulkTransferPreferInventory(player, container, emptySlots, nonEmptySlots, slot);
                        }
                    }
                } else {
                    // Just a normal inventory-to-inventory transfer, simply shift-click the items
                    for (Slot slot : container.inventorySlots) {
                        if (!slot.canTakeStack(Objects.requireNonNull(player))) {
                            continue;
                        }

                        if (InventoryUtils.isSameInventory(slot, clickedSlot, true)) {
                            container.slotClick(slot.slotNumber, 0, ClickType.QUICK_MOVE, player);
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }

    private static boolean bulkTransferPreferInventory(PlayerEntity player, Container container, Deque<Slot> emptySlots, List<Slot> nonEmptySlots, Slot slot) {
        PlayerInventory playerInventory = player.inventory;
        ItemStack targetStack = slot.getStack();
        if (targetStack.isEmpty()) {
            return false;
        }

        container.slotClick(slot.slotNumber, 0, ClickType.PICKUP, player);

        for (Slot nonEmptySlot : nonEmptySlots) {
            ItemStack stack = slot.getStack();
            if (ItemStack.areItemsEqualIgnoreDurability(targetStack, stack)) {
                boolean hasSpaceLeft = stack.getCount() < Math.min(slot.getSlotStackLimit(), slot.getItemStackLimit(stack));
                if (!hasSpaceLeft) {
                    continue;
                }

                container.slotClick(nonEmptySlot.slotNumber, 0, ClickType.PICKUP, player);
                ItemStack mouseItem = playerInventory.getItemStack();
                if (mouseItem.isEmpty()) {
                    return true;
                }
            }
        }

        for (Iterator<Slot> iterator = emptySlots.iterator(); iterator.hasNext(); ) {
            Slot emptySlot = iterator.next();
            container.slotClick(emptySlot.slotNumber, 0, ClickType.PICKUP, player);
            if (emptySlot.getHasStack()) {
                nonEmptySlots.add(emptySlot);
                iterator.remove();
            }

            ItemStack mouseItem = playerInventory.getItemStack();
            if (mouseItem.isEmpty()) {
                return true;
            }
        }

        ItemStack mouseItem = playerInventory.getItemStack();
        if (!mouseItem.isEmpty()) {
            container.slotClick(slot.slotNumber, 0, ClickType.PICKUP, player);
        }

        return false;
    }
}
