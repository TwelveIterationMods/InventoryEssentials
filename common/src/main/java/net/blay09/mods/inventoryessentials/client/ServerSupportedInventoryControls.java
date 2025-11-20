package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.inventoryessentials.InventoryEssentialsConfig;
import net.blay09.mods.inventoryessentials.network.BulkTransferAllMessage;
import net.blay09.mods.inventoryessentials.network.BulkTransferSingleMessage;
import net.blay09.mods.inventoryessentials.network.SingleTransferMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class ServerSupportedInventoryControls extends ClientOnlyInventoryControls {

    @Override
    public boolean singleTransfer(AbstractContainerScreen<?> screen, Slot clickedSlot) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        if (clickedSlot.mayPickup(player)) {
            Balm.networking().sendToServer(new SingleTransferMessage(clickedSlot.index));
            return true;
        }

        return false;
    }

    @Override
    public boolean bulkTransferSingle(AbstractContainerScreen<?> screen, Slot clickedSlot) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        if (!clickedSlot.hasItem() && !InventoryEssentialsConfig.getActive ().allowBulkTransferAllOnEmptySlot) {
            return false;
        }

        if (clickedSlot.mayPickup(player)) {
            Balm.networking().sendToServer(new BulkTransferSingleMessage(clickedSlot.index));
            return true;
        }

        return false;
    }

    @Override
    public boolean bulkTransferAll(AbstractContainerScreen<?> screen, Slot clickedSlot) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        if (!clickedSlot.hasItem() && !InventoryEssentialsConfig.getActive ().allowBulkTransferAllOnEmptySlot) {
            return false;
        }

        if (clickedSlot.mayPickup(player)) {
            Balm.networking().sendToServer(new BulkTransferAllMessage(clickedSlot.index));
            return true;
        }

        return false;
    }
}
