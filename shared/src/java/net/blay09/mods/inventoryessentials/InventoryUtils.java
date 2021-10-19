package net.blay09.mods.inventoryessentials;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.SlotItemHandler;

public class InventoryUtils {

    public static boolean isSameInventory(Slot targetSlot, Slot slot) {
        return isSameInventory(targetSlot, slot, false);
    }

    public static boolean isSameInventory(Slot targetSlot, Slot slot, boolean treatHotBarAsSeparate) {
        boolean isTargetPlayerInventory = targetSlot.container instanceof Inventory;
        boolean isTargetHotBar = isTargetPlayerInventory && Inventory.isHotbarSlot(targetSlot.getSlotIndex());
        boolean isPlayerInventory = slot.container instanceof Inventory;
        boolean isHotBar = isPlayerInventory && Inventory.isHotbarSlot(slot.getSlotIndex());

        if (isTargetPlayerInventory && isPlayerInventory && treatHotBarAsSeparate) {
            return isHotBar == isTargetHotBar;
        }

        // TODO port platform-specific
        if (targetSlot instanceof SlotItemHandler && slot instanceof SlotItemHandler) {
            return ((SlotItemHandler) targetSlot).getItemHandler() == ((SlotItemHandler) slot).getItemHandler();
        }

        return slot.isSameInventory(targetSlot);
    }

    public static boolean containerContainsPlayerInventory(AbstractContainerMenu menu) {
        for (Slot slot : menu.slots) {
            if (slot.container instanceof Inventory && slot.getSlotIndex() >= 9 && slot.getSlotIndex() < 37) {
                return true;
            }
        }

        return false;
    }
}
