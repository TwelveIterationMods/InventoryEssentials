package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.inventoryessentials.InventoryEssentialsConfig;
import net.blay09.mods.inventoryessentials.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.util.*;

public class ClientOnlyInventoryControls implements InventoryControls {

    @Override
    public boolean singleTransfer(ContainerScreen<?> screen, Slot clickedSlot) {
        Container container = screen.getContainer();
        PlayerEntity player = Minecraft.getInstance().player;
        if (!clickedSlot.canTakeStack(Objects.requireNonNull(player))) {
            return false;
        }

        ItemStack targetStack = clickedSlot.getStack().copy();
        // If clicked stack only has a count of one to begin with, just do a normal shift-click on it
        if (targetStack.getCount() == 1) {
            slotClick(container, clickedSlot, 0, ClickType.QUICK_MOVE);
            return true;
        }

        Slot fallbackSlot = null;

        // Go through all slots in the container
        for (Slot slot : container.inventorySlots) {
            ItemStack stack = slot.getStack();
            // Skip the clicked slot, skip slots that do not accept the clicked item, skip slots that are of the same inventory (since we're moving between inventories), and skip slots that are already full
            if (slot == clickedSlot || !slot.isItemValid(targetStack) || InventoryUtils.isSameInventory(clickedSlot, slot)
                    || stack.getCount() >= Math.min(slot.getSlotStackLimit(), slot.getItemStackLimit(stack))) {
                continue;
            }

            // Prefer inputting into an existing stack if the items match
            if (ItemStack.areItemsEqualIgnoreDurability(targetStack, stack)) {
                slotClick(container, clickedSlot, 1, ClickType.PICKUP);
                slotClick(container, slot, 1, ClickType.PICKUP);
                slotClick(container, clickedSlot, 0, ClickType.PICKUP);
                return true;
            } else if (!slot.getHasStack() && fallbackSlot == null) {
                // Remember the first empty slot and move the item there later in case we didn't find an existing stack
                fallbackSlot = slot;
            }
        }

        // There was no existing stack, so move the item into the first empty slot we found
        if (fallbackSlot != null) {
            slotClick(container, clickedSlot, 1, ClickType.PICKUP);
            slotClick(container, fallbackSlot, 1, ClickType.PICKUP);
            slotClick(container, clickedSlot, 0, ClickType.PICKUP);
            return true;
        }

        return false;
    }

    @Override
    public boolean bulkTransferByType(ContainerScreen<?> screen, Slot clickedSlot) {
        ItemStack targetStack = clickedSlot.getStack().copy();
        Container container = screen.getContainer();
        List<Slot> transferSlots = new ArrayList<>();
        transferSlots.add(clickedSlot);
        for (Slot slot : container.inventorySlots) {
            if (slot == clickedSlot) {
                continue;
            }

            if (InventoryUtils.isSameInventory(slot, clickedSlot)) {
                ItemStack stack = slot.getStack();
                if (ItemStack.areItemsEqualIgnoreDurability(targetStack, stack)) {
                    transferSlots.add(slot);
                }
            }
        }

        for (Slot transferSlot : transferSlots) {
            slotClick(container, transferSlot, 0, ClickType.QUICK_MOVE);
        }

        return true;
    }

    @Override
    public boolean bulkTransferAll(ContainerScreen<?> screen, Slot clickedSlot) {
        if (!InventoryEssentialsConfig.CLIENT.allowBulkTransferAllOnEmptySlot.get() && !clickedSlot.getHasStack()) {
            return false;
        }

        PlayerEntity player = Minecraft.getInstance().player;
        Container container = screen.getContainer();

        boolean isProbablyMovingToPlayerInventory = false;
        // If the clicked slot is *not* from the player inventory,
        if (!(clickedSlot.inventory instanceof PlayerInventory)) {
            // Search for any slot that belongs to the player inventory area (not hotbar)
            isProbablyMovingToPlayerInventory = InventoryUtils.containerContainsPlayerInventory(container);
        }

        boolean movedAny = false;

        // If we're probably transferring to the player inventory, use transfer-to-inventory behaviour instead of just shift-clicking the items
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
                    if (bulkTransferPreferInventory(container, player.inventory, emptySlots, nonEmptySlots, slot)) {
                        movedAny = true;
                    }
                }
            }
        } else {
            // Just a normal inventory-to-inventory transfer, simply shift-click the items
            for (Slot slot : container.inventorySlots) {
                if (!slot.canTakeStack(Objects.requireNonNull(player))) {
                    continue;
                }

                if (InventoryUtils.isSameInventory(slot, clickedSlot, true)) {
                    slotClick(container, slot, 0, ClickType.QUICK_MOVE);
                    movedAny = true;
                }
            }

        }

        return movedAny;
    }

    private boolean bulkTransferPreferInventory(Container container, PlayerInventory playerInventory, Deque<Slot> emptySlots, List<Slot> nonEmptySlots, Slot slot) {
        ItemStack targetStack = slot.getStack();
        if (targetStack.isEmpty()) {
            return false;
        }

        slotClick(container, slot, 0, ClickType.PICKUP);

        for (Slot nonEmptySlot : nonEmptySlots) {
            ItemStack stack = slot.getStack();
            if (ItemStack.areItemsEqualIgnoreDurability(targetStack, stack)) {
                boolean hasSpaceLeft = stack.getCount() < Math.min(slot.getSlotStackLimit(), slot.getItemStackLimit(stack));
                if (!hasSpaceLeft) {
                    continue;
                }

                slotClick(container, nonEmptySlot, 0, ClickType.PICKUP);
                ItemStack mouseItem = playerInventory.getItemStack();
                if (mouseItem.isEmpty()) {
                    return true;
                }
            }
        }

        for (Iterator<Slot> iterator = emptySlots.iterator(); iterator.hasNext(); ) {
            Slot emptySlot = iterator.next();
            slotClick(container, emptySlot, 0, ClickType.PICKUP);
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
            slotClick(container, slot, 0, ClickType.PICKUP);
        }

        return false;
    }

    @Override
    public void dragTransfer(ContainerScreen<?> screen, Slot clickedSlot) {
        slotClick(screen.getContainer(), clickedSlot, 0, ClickType.QUICK_MOVE);
    }

    private void slotClick(Container container, Slot slot, int mouseButton, ClickType clickType) {
        Minecraft.getInstance().playerController.windowClick(container.windowId, slot.slotNumber, mouseButton, clickType, Minecraft.getInstance().player);
    }

    @Override
    public boolean dropByType(ContainerScreen<?> screen, Slot hoverSlot) {
        ItemStack targetStack = hoverSlot.getStack().copy();
        Container container = screen.getContainer();
        List<Slot> transferSlots = new ArrayList<>();
        transferSlots.add(hoverSlot);
        for (Slot slot : container.inventorySlots) {
            if (slot == hoverSlot) {
                continue;
            }

            if (InventoryUtils.isSameInventory(slot, hoverSlot)) {
                ItemStack stack = slot.getStack();
                if (ItemStack.areItemsEqualIgnoreDurability(targetStack, stack)) {
                    transferSlots.add(slot);
                }
            }
        }

        for (Slot transferSlot : transferSlots) {
            slotClick(container, transferSlot, 1, ClickType.THROW);
        }

        return true;
    }
}
