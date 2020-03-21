package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.inventoryessentials.network.NetworkHandler;
import net.blay09.mods.inventoryessentials.network.SingleTransferMessage;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;

public class ServerSupportedInventoryControls extends ClientOnlyInventoryControls {

    @Override
    public boolean singleTransfer(ContainerScreen<?> screen, Slot targetSlot) {
        NetworkHandler.channel.sendToServer(new SingleTransferMessage(targetSlot.slotNumber));
        return true;
    }

}
