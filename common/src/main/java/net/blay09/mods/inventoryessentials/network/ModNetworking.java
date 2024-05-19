package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.balm.api.network.BalmNetworking;
import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.minecraft.resources.ResourceLocation;

public class ModNetworking {

    public static void initialize(BalmNetworking networking) {
        networking.allowClientAndServerOnly(InventoryEssentials.MOD_ID);

        networking.registerClientboundPacket(HelloMessage.TYPE, HelloMessage.class, HelloMessage::encode, HelloMessage::decode, HelloMessage::handle);

        networking.registerServerboundPacket(SingleTransferMessage.TYPE, SingleTransferMessage.class, SingleTransferMessage::encode, SingleTransferMessage::decode, SingleTransferMessage::handle);
        networking.registerServerboundPacket(BulkTransferAllMessage.TYPE, BulkTransferAllMessage.class, BulkTransferAllMessage::encode, BulkTransferAllMessage::decode, BulkTransferAllMessage::handle);
    }
}
