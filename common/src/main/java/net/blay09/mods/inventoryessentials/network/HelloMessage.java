package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class HelloMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<HelloMessage> TYPE = new CustomPacketPayload.Type<>(new ResourceLocation(InventoryEssentials.MOD_ID, "hello"));

    public static void encode(FriendlyByteBuf buf, HelloMessage message) {
    }

    public static HelloMessage decode(FriendlyByteBuf buf) {
        return new HelloMessage();
    }

    public static void handle(Player player, HelloMessage message) {
        InventoryEssentials.isServerSideInstalled = true;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
