package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.inventoryessentials.network.NetworkHandler;
import net.blay09.mods.inventoryessentials.network.SingleTransferMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;

import java.util.Objects;

public class ServerSupportedInventoryControls extends ClientOnlyInventoryControls {

    @Override
    public boolean singleTransfer(ContainerScreen<?> screen, Slot targetSlot) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (targetSlot.canTakeStack(Objects.requireNonNull(player))) {
            NetworkHandler.channel.sendToServer(new SingleTransferMessage(targetSlot.slotNumber));
            return true;
        }

        return false;
    }

}
