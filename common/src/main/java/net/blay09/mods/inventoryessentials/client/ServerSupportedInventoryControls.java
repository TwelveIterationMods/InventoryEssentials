package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.inventoryessentials.InventoryEssentialsConfig;
import net.blay09.mods.inventoryessentials.network.BulkTransferAllMessage;
import net.blay09.mods.inventoryessentials.network.BulkTransferSingleMessage;
import net.blay09.mods.inventoryessentials.network.DumpToContainerMessage;
import net.blay09.mods.inventoryessentials.network.RestockInventoryMessage;
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
            Balm.getNetworking().sendToServer(new SingleTransferMessage(clickedSlot.index));
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
            Balm.getNetworking().sendToServer(new BulkTransferSingleMessage(clickedSlot.index));
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
            Balm.getNetworking().sendToServer(new BulkTransferAllMessage(clickedSlot.index));
            return true;
        }

        return false;
    }

    @Override
    public boolean restockContainer(AbstractContainerScreen<?> screen) {
        if (Minecraft.getInstance().player == null) {
            return false;
        }

        Balm.networking().sendToServer(new DumpToContainerMessage(false));
        return true;
    }

    @Override
    public boolean restockInventory(AbstractContainerScreen<?> screen) {
        if (Minecraft.getInstance().player == null) {
            return false;
        }

        Balm.networking().sendToServer(RestockInventoryMessage.INSTANCE);
        return true;
    }

    @Override
    public boolean dumpToContainer(AbstractContainerScreen<?> screen) {
        if (Minecraft.getInstance().player == null) {
            return false;
        }

        Balm.networking().sendToServer(new DumpToContainerMessage(true));
        return true;
    }
}
