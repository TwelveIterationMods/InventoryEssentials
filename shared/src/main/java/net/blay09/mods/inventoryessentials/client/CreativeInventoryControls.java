package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.inventoryessentials.mixin.SlotWrapperAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public class CreativeInventoryControls extends ClientOnlyInventoryControls {
    @Override
    protected boolean isValidTargetSlot(Slot slot) {
        return slot.container instanceof Inventory;
    }

    @Override
    protected void slotClick(AbstractContainerMenu menu, Slot slot, int mouseButton, ClickType clickType) {
        if (slot instanceof SlotWrapperAccessor accessor) {
            slotClick(menu, accessor.getTarget().index, mouseButton, clickType);
        } else {
            slotClick(menu, slot.index, mouseButton, clickType);
        }
    }

    @Override
    protected void slotClick(AbstractContainerMenu menu, int slotIndex, int mouseButton, ClickType clickType) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.player.inventoryMenu.clicked(slotIndex, mouseButton, clickType, minecraft.player);
        minecraft.player.inventoryMenu.broadcastChanges();
    }
}
