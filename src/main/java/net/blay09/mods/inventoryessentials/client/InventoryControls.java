package net.blay09.mods.inventoryessentials.client;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;

public interface InventoryControls {
    boolean singleTransfer(ContainerScreen<?> screen, Slot targetSlot);
    boolean bulkTransferByType(ContainerScreen<?> screen, Slot targetSlot);
    boolean bulkTransferAll(ContainerScreen<?> screen, Slot targetSlot);
    void dragTransfer(ContainerScreen<?> screen, Slot targetSlot);
}
