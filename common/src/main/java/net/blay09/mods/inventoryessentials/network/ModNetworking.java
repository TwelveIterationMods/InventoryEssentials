package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.balm.api.network.BalmNetworking;
import net.blay09.mods.inventoryessentials.InventoryEssentials;

public class ModNetworking {

    public static void initialize(BalmNetworking networking) {
        networking.allowClientAndServerOnly(InventoryEssentials.MOD_ID);

        networking.registerClientboundPacket(HelloMessage.TYPE, HelloMessage.class, HelloMessage.STREAM_CODEC, HelloMessage::handle);

        networking.registerServerboundPacket(SingleTransferMessage.TYPE, SingleTransferMessage.class, SingleTransferMessage.STREAM_CODEC, SingleTransferMessage::handle);
        networking.registerServerboundPacket(BulkTransferAllMessage.TYPE, BulkTransferAllMessage.class, BulkTransferAllMessage.STREAM_CODEC, BulkTransferAllMessage::handle);
    }
}
