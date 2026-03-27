package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.inventoryessentials.ServerInventoryTransfers;
import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record SingleTransferMessage(int slotNumber) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SingleTransferMessage> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
            InventoryEssentials.MOD_ID,
            "single_transfer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SingleTransferMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SingleTransferMessage::slotNumber,
            SingleTransferMessage::new
    );

    public static void handle(ServerPlayer player, SingleTransferMessage message) {
        final var menu = player.containerMenu;
        if (menu != null && message.slotNumber >= 0 && message.slotNumber < menu.slots.size()) {
            final var slot = menu.slots.get(message.slotNumber);
            ServerInventoryTransfers.singleTransfer(player, menu, slot);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
