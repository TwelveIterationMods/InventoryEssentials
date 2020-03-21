package net.blay09.mods.inventoryessentials.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

public class ClientOnlyInventoryControls implements InventoryControls {

    @Override
    public boolean singleTransfer(ContainerScreen<?> screen, Slot targetSlot) {
        Container container = screen.getContainer();
        ItemStack targetStack = targetSlot.getStack().copy();
        if (targetStack.getCount() == 1) {
            slotClick(container, targetSlot, 0, ClickType.QUICK_MOVE);
            return true;
        }

        Slot fallbackSlot = null;
        for (Slot slot : container.inventorySlots) {
            ItemStack stack = slot.getStack();
            if (slot == targetSlot || !slot.isItemValid(targetStack) || isSameInventory(targetSlot, slot) || stack.getCount() >= Math.min(slot.getSlotStackLimit(), slot.getItemStackLimit(stack))) {
                continue;
            }

            if (ItemStack.areItemsEqualIgnoreDurability(targetStack, stack)) {
                slotClick(container, targetSlot, 1, ClickType.PICKUP);
                slotClick(container, slot, 1, ClickType.PICKUP);
                slotClick(container, targetSlot, 0, ClickType.PICKUP);
                return true;
            } else if (!slot.getHasStack() && fallbackSlot == null) {
                fallbackSlot = slot;
            }
        }

        if (fallbackSlot != null) {
            slotClick(container, targetSlot, 1, ClickType.PICKUP);
            slotClick(container, fallbackSlot, 1, ClickType.PICKUP);
            slotClick(container, targetSlot, 0, ClickType.PICKUP);
        }

        return true;
    }

    private boolean isSameInventory(Slot targetSlot, Slot slot) {
        if (targetSlot instanceof SlotItemHandler && slot instanceof SlotItemHandler) {
            return ((SlotItemHandler) targetSlot).getItemHandler() == ((SlotItemHandler) slot).getItemHandler();
        }

        return slot.isSameInventory(targetSlot);
    }

    @Override
    public boolean bulkTransfer(ContainerScreen<?> screen, Slot targetSlot) {
        ItemStack targetStack = targetSlot.getStack().copy();
        Container container = screen.getContainer();
        List<Slot> transferSlots = new ArrayList<>();
        transferSlots.add(targetSlot);
        for (Slot slot : container.inventorySlots) {
            if (slot == targetSlot) {
                continue;
            }

            if (isSameInventory(slot, targetSlot)) {
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
    public void dragBulkTransfer(ContainerScreen<?> screen, Slot targetSlot) {
        slotClick(screen.getContainer(), targetSlot, 0, ClickType.QUICK_MOVE);
    }

    private void slotClick(Container container, Slot slot, int mouseButton, ClickType clickType) {
        Minecraft.getInstance().playerController.windowClick(container.windowId, slot.slotNumber, mouseButton, clickType, Minecraft.getInstance().player);
    }
}
