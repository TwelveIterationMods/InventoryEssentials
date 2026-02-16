package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;

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
        if (!menu.getCarried().isEmpty() || menu instanceof InventoryMenu) {
            return;
        }

        final var sourceSlots = new ArrayList<Slot>();
        final var nonEmptyTargetSlots = new ArrayList<Slot>();
        final var emptyTargetSlots = new ArrayList<Slot>();
        for (final var slot : menu.slots) {
            if (slot.container instanceof Inventory) {
                final var containerSlot = slot.getContainerSlot();
                if (containerSlot >= Inventory.SELECTION_SIZE && containerSlot < Inventory.INVENTORY_SIZE && slot.mayPickup(player) && slot.hasItem()) {
                    sourceSlots.add(slot);
                }
            } else if (slot.hasItem()) {
                nonEmptyTargetSlots.add(slot);
            } else {
                emptyTargetSlots.add(slot);
            }
        }

        if (sourceSlots.isEmpty() || nonEmptyTargetSlots.isEmpty()) {
            return;
        }

        for (final var sourceSlot : sourceSlots) {
            if (!sourceSlot.hasItem()) {
                continue;
            }

            menu.clicked(sourceSlot.index, 0, ClickType.PICKUP, player);
            var carried = menu.getCarried();
            if (carried.isEmpty()) {
                continue;
            }

            final var sourceStack = carried.copy();
            boolean hasMatchingItemInContainer = false;
            for (final var targetSlot : nonEmptyTargetSlots) {
                final var targetStack = targetSlot.getItem();
                if (targetStack.isEmpty() || !ItemStack.isSameItemSameComponents(sourceStack, targetStack)) {
                    continue;
                } else {
                    hasMatchingItemInContainer = true;
                }

                final int targetLimit = Math.min(targetSlot.getMaxStackSize(), targetSlot.getMaxStackSize(targetStack));
                if (targetStack.getCount() >= targetLimit) {
                    continue;
                }

                menu.clicked(targetSlot.index, 0, ClickType.PICKUP, player);
                carried = menu.getCarried();
                if (carried.isEmpty()) {
                    break;
                }
            }

            if (message.fillEmptySlots && !carried.isEmpty() && hasMatchingItemInContainer) {
                for (final Iterator<Slot> iterator = emptyTargetSlots.iterator(); iterator.hasNext(); ) {
                    final var emptyTargetSlot = iterator.next();
                    if (emptyTargetSlot.hasItem()) {
                        nonEmptyTargetSlots.add(emptyTargetSlot);
                        iterator.remove();
                        continue;
                    }

                    if (!emptyTargetSlot.mayPlace(sourceStack)) {
                        continue;
                    }

                    menu.clicked(emptyTargetSlot.index, 0, ClickType.PICKUP, player);
                    carried = menu.getCarried();
                    if (emptyTargetSlot.hasItem()) {
                        nonEmptyTargetSlots.add(emptyTargetSlot);
                        iterator.remove();
                    }

                    if (carried.isEmpty()) {
                        break;
                    }
                }
            }

            if (!menu.getCarried().isEmpty()) {
                menu.clicked(sourceSlot.index, 0, ClickType.PICKUP, player);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
