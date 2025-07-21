package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.inventoryessentials.ServerInventoryTransfers;
import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class SingleTransferMessage implements CustomPacketPayload {

    public static CustomPacketPayload.Type<SingleTransferMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(InventoryEssentials.MOD_ID, "single_transfer"));
    private final int slotNumber;

    public SingleTransferMessage(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public static SingleTransferMessage decode(FriendlyByteBuf buf) {
        int slotNumber = buf.readByte();
        return new SingleTransferMessage(slotNumber);
    }

    public static void encode(FriendlyByteBuf buf, SingleTransferMessage message) {
        buf.writeByte(message.slotNumber);
    }

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
