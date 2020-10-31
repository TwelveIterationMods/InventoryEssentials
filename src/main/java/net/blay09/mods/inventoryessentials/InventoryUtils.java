package net.blay09.mods.inventoryessentials;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraftforge.items.SlotItemHandler;

public class InventoryUtils {

    public static boolean isSameInventory(Slot targetSlot, Slot slot) {
        return isSameInventory(targetSlot, slot, false);
    }

    public static boolean isSameInventory(Slot targetSlot, Slot slot, boolean treatHotBarAsSeparate) {
        boolean isTargetPlayerInventory = targetSlot.inventory instanceof PlayerInventory;
        boolean isTargetHotBar = isTargetPlayerInventory && PlayerInventory.isHotbar(targetSlot.getSlotIndex());
        boolean isPlayerInventory = slot.inventory instanceof PlayerInventory;
        boolean isHotBar = isPlayerInventory && PlayerInventory.isHotbar(slot.getSlotIndex());

        if (isTargetPlayerInventory && isPlayerInventory && treatHotBarAsSeparate) {
            return isHotBar == isTargetHotBar;
        }

        if (targetSlot instanceof SlotItemHandler && slot instanceof SlotItemHandler) {
            return ((SlotItemHandler) targetSlot).getItemHandler() == ((SlotItemHandler) slot).getItemHandler();
        }

        return slot.isSameInventory(targetSlot);
    }

    public static boolean containerContainsPlayerInventory(Container container) {
        for (Slot slot : container.inventorySlots) {
            if (slot.inventory instanceof PlayerInventory && slot.getSlotIndex() >= 9 && slot.getSlotIndex() < 37) {
                return true;
            }
        }

        return false;
    }
}
