package net.blay09.mods.inventoryessentials;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;

public class InventoryOperations {

    @FunctionalInterface
    public interface SlotPolicy {
        boolean isValidSlot(Slot slot);

        static SlotPolicy always() {
            return slot -> true;
        }
    }

    @FunctionalInterface
    public interface SlotClickHandler {
        void click(AbstractContainerMenu menu, Slot slot, int mouseButton, ClickType clickType);
    }

    private final SlotClickHandler slotClickHandler;
    private final SlotPolicy slotPolicy;

    public InventoryOperations(SlotClickHandler slotClickHandler, SlotPolicy slotPolicy) {
        this.slotClickHandler = slotClickHandler;
        this.slotPolicy = slotPolicy;
    }

    public boolean transferToContainer(AbstractContainerMenu menu, Player player, boolean fillEmptySlots) {
        if (!menu.getCarried().isEmpty() || menu instanceof InventoryMenu) {
            return false;
        }

        final var sourceSlots = new ArrayList<Slot>();
        final var nonEmptyTargetSlots = new ArrayList<Slot>();
        final var emptyTargetSlots = new ArrayList<Slot>();
        for (final var slot : menu.slots) {
            if (!slotPolicy.isValidSlot(slot)) {
                continue;
            }

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
            return false;
        }

        boolean movedAny = false;
        for (final var sourceSlot : sourceSlots) {
            if (!sourceSlot.hasItem()) {
                continue;
            }

            slotClickHandler.click(menu, sourceSlot, 0, ClickType.PICKUP);
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

                final int oldCarriedCount = menu.getCarried().getCount();
                slotClickHandler.click(menu, targetSlot, 0, ClickType.PICKUP);
                carried = menu.getCarried();
                if (carried.getCount() < oldCarriedCount) {
                    movedAny = true;
                }
                if (carried.isEmpty()) {
                    break;
                }
            }

            if (fillEmptySlots && !carried.isEmpty() && hasMatchingItemInContainer) {
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

                    final int oldCarriedCount = menu.getCarried().getCount();
                    slotClickHandler.click(menu, emptyTargetSlot, 0, ClickType.PICKUP);
                    carried = menu.getCarried();
                    if (carried.getCount() < oldCarriedCount) {
                        movedAny = true;
                        if (emptyTargetSlot.hasItem()) {
                            nonEmptyTargetSlots.add(emptyTargetSlot);
                            iterator.remove();
                        }
                    }

                    if (carried.isEmpty()) {
                        break;
                    }
                }
            }

            if (!menu.getCarried().isEmpty()) {
                slotClickHandler.click(menu, sourceSlot, 0, ClickType.PICKUP);
            }
        }

        return movedAny;
    }

    public boolean isValidSlot(Slot slot) {
        return slotPolicy.isValidSlot(slot);
    }

    public static InventoryOperations forServerPlayer(ServerPlayer player) {
        return new InventoryOperations((containerMenu, slot, mouseButton, clickType) -> containerMenu.clicked(slot.index, mouseButton, clickType, player), SlotPolicy.always());
    }

}
