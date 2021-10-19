package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.balm.api.network.BalmNetworking;
import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.minecraft.resources.ResourceLocation;

public class ModNetworking {

    public static void initialize(BalmNetworking networking) {
        networking.registerClientboundPacket(id("hello"), HelloMessage.class, HelloMessage::encode, HelloMessage::decode, HelloMessage::handle);

        networking.registerServerboundPacket(id("single_transfer"), SingleTransferMessage.class, SingleTransferMessage::encode, SingleTransferMessage::decode, SingleTransferMessage::handle);
        networking.registerServerboundPacket(id("bulk_transfer_all"), BulkTransferAllMessage.class, BulkTransferAllMessage::encode, BulkTransferAllMessage::decode, BulkTransferAllMessage::handle);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(InventoryEssentials.MOD_ID, path);
    }
}
