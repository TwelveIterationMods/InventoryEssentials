package net.blay09.mods.inventoryessentials;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.Optional;

public class InventoryUtils {

    public static boolean isSameInventory(Slot targetSlot, Slot slot) {
        return isSameInventory(targetSlot, slot, false);
    }

    public static boolean isSameInventory(Slot targetSlot, Slot slot, boolean treatHotBarAsSeparate) {
        boolean isTargetPlayerInventory = targetSlot.container instanceof Inventory;
        boolean isTargetHotBar = isTargetPlayerInventory && Inventory.isHotbarSlot(targetSlot.getContainerSlot());
        boolean isPlayerInventory = slot.container instanceof Inventory;
        boolean isHotBar = isPlayerInventory && Inventory.isHotbarSlot(slot.getContainerSlot());

        if (isTargetPlayerInventory && isPlayerInventory && treatHotBarAsSeparate) {
            return isHotBar == isTargetHotBar;
        }

        return PlatformBindings.INSTANCE.isSameInventory(targetSlot, slot);
    }

    public static boolean containerContainsPlayerInventory(AbstractContainerMenu menu) {
        for (Slot slot : menu.slots) {
            if (slot.container instanceof Inventory && slot.getContainerSlot() >= 9 && slot.getContainerSlot() < 37) {
                return true;
            }
        }

        return false;
    }
}
