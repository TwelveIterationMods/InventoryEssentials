package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.balm.network.BalmNetworking;
import net.blay09.mods.inventoryessentials.InventoryEssentials;

public class ModNetworking {

    public static void initialize(BalmNetworking networking) {
        networking.defineNetworkVersion(InventoryEssentials.MOD_ID, "1");
        networking.allowClientAndServerOnly(InventoryEssentials.MOD_ID);

        networking.registerClientboundPacket(HelloMessage.TYPE, HelloMessage.class, HelloMessage.STREAM_CODEC, HelloMessage::handle);

        networking.registerServerboundPacket(SingleTransferMessage.TYPE, SingleTransferMessage.class, SingleTransferMessage.STREAM_CODEC, SingleTransferMessage::handle);
        networking.registerServerboundPacket(BulkTransferAllMessage.TYPE, BulkTransferAllMessage.class, BulkTransferAllMessage.STREAM_CODEC, BulkTransferAllMessage::handle);
        networking.registerServerboundPacket(BulkTransferSingleMessage.TYPE, BulkTransferSingleMessage.class, BulkTransferSingleMessage.STREAM_CODEC, BulkTransferSingleMessage::handle);
        networking.registerServerboundPacket(DumpToContainerMessage.TYPE, DumpToContainerMessage.class, DumpToContainerMessage.STREAM_CODEC, DumpToContainerMessage::handle);
    }
}
