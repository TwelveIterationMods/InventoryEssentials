package net.blay09.mods.inventoryessentials.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SingleTransferMessage {

    private final int slotNumber;

    public SingleTransferMessage(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public static SingleTransferMessage decode(PacketBuffer buf) {
        int slotNumber = buf.readByte();
        return new SingleTransferMessage(slotNumber);
    }

    public static void encode(SingleTransferMessage message, PacketBuffer buf) {
        buf.writeByte(message.slotNumber);
    }

    public static void handle(SingleTransferMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            PlayerEntity player = context.getSender();
            if (player == null) {
                return;
            }

            Container container = player.openContainer;
            if (container != null && message.slotNumber >= 0 && message.slotNumber < container.inventorySlots.size()) {
                Slot slot = container.inventorySlots.get(message.slotNumber);
                if (!slot.canTakeStack(player)) {
                    return;
                }

                ItemStack sourceStack = slot.getStack();
                if (sourceStack.getCount() == 1) {
                    container.slotClick(message.slotNumber, 0, ClickType.QUICK_MOVE, player);
                } else if (!sourceStack.isEmpty()) {
                    ItemStack copyStack = sourceStack.copy();
                    sourceStack.setCount(1);
                    copyStack.shrink(1);
                    container.slotClick(message.slotNumber, 0, ClickType.QUICK_MOVE, player);
                    if (!slot.getHasStack()) {
                        slot.putStack(copyStack);
                    } else {
                        if (!player.addItemStackToInventory(copyStack)) {
                            player.dropItem(copyStack, false);
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
