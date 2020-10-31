package net.blay09.mods.inventoryessentials.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class BulkTransferAllMessage {

    private final int slotNumber;

    public BulkTransferAllMessage(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public static BulkTransferAllMessage decode(PacketBuffer buf) {
        int slotNumber = buf.readByte();
        return new BulkTransferAllMessage(slotNumber);
    }

    public static void encode(BulkTransferAllMessage message, PacketBuffer buf) {
        buf.writeByte(message.slotNumber);
    }

    public static void handle(BulkTransferAllMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            PlayerEntity player = context.getSender();
            if (player == null) {
                return;
            }

            Container container = player.openContainer;
            if (container != null && message.slotNumber >= 0 && message.slotNumber < container.inventorySlots.size()) {
                Slot slot = container.inventorySlots.get(message.slotNumber);


            }
        });
        context.setPacketHandled(true);
    }
}
