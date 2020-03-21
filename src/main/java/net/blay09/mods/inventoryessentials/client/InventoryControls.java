package net.blay09.mods.inventoryessentials.client;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;

public interface InventoryControls {
    boolean singleTransfer(ContainerScreen<?> screen, Slot targetSlot);
    boolean bulkTransfer(ContainerScreen<?> screen, Slot targetSlot);
    void dragBulkTransfer(ContainerScreen<?> screen, Slot targetSlot);
}
