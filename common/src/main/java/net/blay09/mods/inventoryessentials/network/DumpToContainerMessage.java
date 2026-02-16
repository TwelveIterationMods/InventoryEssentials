package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.blay09.mods.inventoryessentials.InventoryOperations;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record DumpToContainerMessage(boolean fillEmptySlots) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DumpToContainerMessage> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
            InventoryEssentials.MOD_ID,
            "dump_to_container"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DumpToContainerMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            DumpToContainerMessage::fillEmptySlots,
            DumpToContainerMessage::new
    );

    public static void handle(ServerPlayer player, DumpToContainerMessage message) {
        final var menu = player.containerMenu;
        InventoryOperations.forServerPlayer(player).transferToContainer(menu, player, message.fillEmptySlots());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
