package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.blay09.mods.inventoryessentials.InventoryOperations;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class RestockInventoryMessage implements CustomPacketPayload {

    public static final RestockInventoryMessage INSTANCE = new RestockInventoryMessage();
    public static final CustomPacketPayload.Type<RestockInventoryMessage> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
            InventoryEssentials.MOD_ID,
            "restock_inventory"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RestockInventoryMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private RestockInventoryMessage() {
    }

    public static void handle(ServerPlayer player, RestockInventoryMessage message) {
        InventoryOperations.forServerPlayer(player).transferToInventory(player.containerMenu, player, true, false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
