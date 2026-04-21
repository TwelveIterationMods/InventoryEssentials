package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.inventoryessentials.ServerInventoryTransfers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class SingleTransferMessage {

    private final int slotNumber;

    public SingleTransferMessage(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public static SingleTransferMessage decode(FriendlyByteBuf buf) {
        int slotNumber = buf.readVarInt();
        return new SingleTransferMessage(slotNumber);
    }

    public static void encode(SingleTransferMessage message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.slotNumber);
    }

    public static void handle(ServerPlayer player, SingleTransferMessage message) {
        final var menu = player.containerMenu;
        if (menu != null && message.slotNumber >= 0 && message.slotNumber < menu.slots.size()) {
            final var slot = menu.slots.get(message.slotNumber);
            ServerInventoryTransfers.singleTransfer(player, menu, slot);
        }
    }
}
