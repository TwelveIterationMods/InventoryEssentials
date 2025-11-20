package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class HelloMessage implements CustomPacketPayload {

    public static final HelloMessage INSTANCE = new HelloMessage();
    public static final CustomPacketPayload.Type<HelloMessage> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(InventoryEssentials.MOD_ID,
            "hello"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HelloMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private HelloMessage() {
    }

    public static void handle(Player player, HelloMessage message) {
        InventoryEssentials.isServerSideInstalled = true;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
