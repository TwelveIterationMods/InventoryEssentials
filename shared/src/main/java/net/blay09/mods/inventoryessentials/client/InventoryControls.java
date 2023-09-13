package net.blay09.mods.inventoryessentials.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public interface InventoryControls {
    boolean singleTransfer(AbstractContainerScreen<?> screen, Slot clickedSlot);

    boolean bulkTransferByType(AbstractContainerScreen<?> screen, Slot clickedSlot);

    boolean bulkTransferAll(AbstractContainerScreen<?> screen, Slot clickedSlot);

    void dragTransfer(AbstractContainerScreen<?> screen, Slot clickedSlot);

    boolean dropByType(AbstractContainerScreen<?> screen, Slot hoverSlot);

    boolean dropByType(AbstractContainerScreen<?> screen, ItemStack itemStack);
}
