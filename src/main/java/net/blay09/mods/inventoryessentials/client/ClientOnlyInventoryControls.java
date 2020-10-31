package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.inventoryessentials.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientOnlyInventoryControls implements InventoryControls {

    @Override
    public boolean singleTransfer(ContainerScreen<?> screen, Slot targetSlot) {
        Container container = screen.getContainer();
        PlayerEntity player = Minecraft.getInstance().player;
        if (!targetSlot.canTakeStack(Objects.requireNonNull(player))) {
            return false;
        }

        ItemStack targetStack = targetSlot.getStack().copy();
        // If clicked stack only has a count of one to begin with, just do a normal shift-click on it
        if (targetStack.getCount() == 1) {
            slotClick(container, targetSlot, 0, ClickType.QUICK_MOVE);
            return true;
        }

        Slot fallbackSlot = null;

        // Go through all slots in the container
        for (Slot slot : container.inventorySlots) {
            ItemStack stack = slot.getStack();
            // Skip the clicked slot, skip slots that do not accept the clicked item, skip slots that are of the same inventory (since we're moving between inventories), and skip slots that are already full
            if (slot == targetSlot || !slot.isItemValid(targetStack) || InventoryUtils.isSameInventory(targetSlot, slot)
                    || stack.getCount() >= Math.min(slot.getSlotStackLimit(), slot.getItemStackLimit(stack))) {
                continue;
            }

            // Prefer inputting into an existing stack if the items match
            if (ItemStack.areItemsEqualIgnoreDurability(targetStack, stack)) {
                slotClick(container, targetSlot, 1, ClickType.PICKUP);
                slotClick(container, slot, 1, ClickType.PICKUP);
                slotClick(container, targetSlot, 0, ClickType.PICKUP);
                return true;
            } else if (!slot.getHasStack() && fallbackSlot == null) {
                // Remember the first empty slot and move the item there later in case we didn't find an existing stack
                fallbackSlot = slot;
            }
        }

        // There was no existing stack, so move the item into the first empty slot we found
        if (fallbackSlot != null) {
            slotClick(container, targetSlot, 1, ClickType.PICKUP);
            slotClick(container, fallbackSlot, 1, ClickType.PICKUP);
            slotClick(container, targetSlot, 0, ClickType.PICKUP);
            return true;
        }

        return false;
    }

    @Override
    public boolean bulkTransferByType(ContainerScreen<?> screen, Slot targetSlot) {
        ItemStack targetStack = targetSlot.getStack().copy();
        Container container = screen.getContainer();
        List<Slot> transferSlots = new ArrayList<>();
        transferSlots.add(targetSlot);
        for (Slot slot : container.inventorySlots) {
            if (slot == targetSlot) {
                continue;
            }

            if (InventoryUtils.isSameInventory(slot, targetSlot)) {
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
    public boolean bulkTransferAll(ContainerScreen<?> screen, Slot targetSlot) {
        PlayerEntity player = Minecraft.getInstance().player;
        Container container = screen.getContainer();
        boolean movedAny = false;
        for (Slot slot : container.inventorySlots) {
            if (!slot.canTakeStack(Objects.requireNonNull(player))) {
                continue;
            }

            if (InventoryUtils.isSameInventory(slot, targetSlot, true)) {
                slotClick(container, slot, 0, ClickType.QUICK_MOVE);
                movedAny = true;
            }
        }

        return movedAny;
    }

    @Override
    public void dragTransfer(ContainerScreen<?> screen, Slot targetSlot) {
        slotClick(screen.getContainer(), targetSlot, 0, ClickType.QUICK_MOVE);
    }

    private void slotClick(Container container, Slot slot, int mouseButton, ClickType clickType) {
        Minecraft.getInstance().playerController.windowClick(container.windowId, slot.slotNumber, mouseButton, clickType, Minecraft.getInstance().player);
    }
}
