package net.blay09.mods.inventoryessentials.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SingleTransferMessage {

    private final int slotNumber;

    public SingleTransferMessage(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public static SingleTransferMessage decode(FriendlyByteBuf buf) {
        int slotNumber = buf.readByte();
        return new SingleTransferMessage(slotNumber);
    }

    public static void encode(SingleTransferMessage message, FriendlyByteBuf buf) {
        buf.writeByte(message.slotNumber);
    }

    public static void handle(ServerPlayer player, SingleTransferMessage message) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu != null && message.slotNumber >= 0 && message.slotNumber < menu.slots.size()) {
            Slot slot = menu.slots.get(message.slotNumber);
            if (!slot.mayPickup(player)) {
                return;
            }

            ItemStack sourceStack = slot.getItem();
            if (sourceStack.getCount() == 1) {
                menu.clicked(message.slotNumber, 0, ClickType.QUICK_MOVE, player);
            } else if (!sourceStack.isEmpty()) {
                ItemStack restStack = sourceStack.copy();
                sourceStack.setCount(1);

                // We specifically set the slot stack as some mods return transient copies in getItem that will not be reflected back to the inventory
                slot.set(sourceStack);

                restStack.shrink(1);
                menu.clicked(message.slotNumber, 0, ClickType.QUICK_MOVE, player);
                if (!slot.hasItem()) {
                    slot.set(restStack);
                } else {
                    if (!player.addItem(restStack)) {
                        player.drop(restStack, false);
                    }
                }
            }
        }
    }
}
