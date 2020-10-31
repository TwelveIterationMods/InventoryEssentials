package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.inventoryessentials.network.BulkTransferAllMessage;
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
    public boolean singleTransfer(ContainerScreen<?> screen, Slot clickedSlot) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (clickedSlot.canTakeStack(Objects.requireNonNull(player))) {
            NetworkHandler.channel.sendToServer(new SingleTransferMessage(clickedSlot.slotNumber));
            return true;
        }

        return false;
    }

    @Override
    public boolean bulkTransferAll(ContainerScreen<?> screen, Slot clickedSlot) {
        return super.bulkTransferAll(screen, clickedSlot);

        /*PlayerEntity player = Minecraft.getInstance().player;
        if (clickedSlot.canTakeStack(Objects.requireNonNull(player))) {
            NetworkHandler.channel.sendToServer(new BulkTransferAllMessage(clickedSlot.slotNumber));
            return true;
        }

        return false;*/
    }
}
