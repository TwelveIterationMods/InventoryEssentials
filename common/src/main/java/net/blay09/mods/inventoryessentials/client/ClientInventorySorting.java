package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.inventoryessentials.InventoryEssentialsExtensions;
import net.blay09.mods.inventoryessentials.InventoryUtils;
import net.blay09.mods.inventoryessentials.PlatformBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ClientInventorySorting {

    private static final Logger logger = LoggerFactory.getLogger(ClientInventorySorting.class);

    private static final Comparator<ItemStack> defaultComparator =
            Comparator.comparing((ItemStack itemStack) -> itemStack.getHoverName().getString(), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed())
                    .thenComparing(itemStack -> itemStack.isEnchanted() ? 0 : 1)
                    .thenComparingInt(ItemStack::getDamageValue)
                    .thenComparing(itemStack -> Objects.toString(itemStack.getTag(), ""));

    @FunctionalInterface
    public interface SlotClicker {
        void click(AbstractContainerMenu menu, Slot slot, int mouseButton, ClickType clickType);
    }

    public static boolean sort(AbstractContainerScreen<?> screen, Slot baseSlot, SlotClicker clicker) {
        final var player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        final var menu = screen.getMenu();
        final var slotsToSort = new ArrayList<Slot>();
        for (final var slot : menu.slots) {
            if (isSortableSlot(screen, slot) && InventoryUtils.isSameInventory(baseSlot, slot, true)) {
                slotsToSort.add(slot);
            }
        }

        if (slotsToSort.isEmpty()) {
            try {
                logger.debug("No slots to sort found; clicked slot was {} in {}", baseSlot.getClass().getName(), BuiltInRegistries.MENU.getKey(menu.getType()));
            } catch (UnsupportedOperationException e) {
                logger.debug("No slots to sort found; clicked slot was {} in {}", baseSlot.getClass().getName(), menu.getClass().getName());
            }
            return false;
        }

        // Merge matching stacks first before sorting
        consolidateStacks(menu, slotsToSort, clicker);

        // Compute the sorted order
        final var goalSorting = slotsToSort.stream()
                .map(Slot::getItem)
                .map(ItemStack::copy)
                .filter(stack -> !stack.isEmpty())
                .sorted(defaultComparator)
                .toList();

        // Swap items to match the new sorting
        for (int i = 0; i < goalSorting.size(); i++) {
            final var goalStack = goalSorting.get(i);
            final var currentStack = slotsToSort.get(i).getItem();
            // If current stack already matches goal stack, skip it
            if (ItemStack.isSameItemSameTags(goalStack, currentStack)
                    && goalStack.getCount() == currentStack.getCount()) {
                continue;
            }

            // Find the first stack from here that matches the goal stack
            int foundSwapIndex = -1;
            for (int j = i + 1; j < slotsToSort.size(); j++) {
                final var candidateStack = slotsToSort.get(j).getItem();
                if (ItemStack.isSameItemSameTags(goalStack, candidateStack)
                        && goalStack.getCount() == candidateStack.getCount()) {
                    foundSwapIndex = j;
                    break;
                }
            }

            // If we found a slot matching the goal stack, swap the two slots
            if (foundSwapIndex != -1) {
                swapSlots(menu, slotsToSort, i, foundSwapIndex, clicker);
            }
        }

        return true;
    }

    private static void swapSlots(AbstractContainerMenu menu, List<Slot> slots, int firstIndex, int secondIndex, SlotClicker clicker) {
        if (firstIndex != secondIndex) {
            final var firstSlot = slots.get(firstIndex);
            final var secondSlot = slots.get(secondIndex);

            // If one of the two slots is empty, we just have to do a simple move
            if (!firstSlot.hasItem() || !secondSlot.hasItem()) {
                final var fromSlot = firstSlot.hasItem() ? firstSlot : secondSlot;
                final var toSlot = firstSlot.hasItem() ? secondSlot : firstSlot;
                clicker.click(menu, fromSlot, 0, ClickType.PICKUP);
                clicker.click(menu, toSlot, 0, ClickType.PICKUP);
                return;
            }

            clicker.click(menu, firstSlot, 0, ClickType.PICKUP);
            clicker.click(menu, secondSlot, 0, ClickType.PICKUP);
            clicker.click(menu, firstSlot, 0, ClickType.PICKUP);
        }
    }

    private static void consolidateStacks(AbstractContainerMenu menu, List<Slot> slots, SlotClicker clicker) {
        for (int i = 0; i < slots.size(); i++) {
            final var thisSlot = slots.get(i);
            if (!thisSlot.hasItem()) {
                continue;
            }

            final var thisStack = thisSlot.getItem();
            for (int j = i + 1; j < slots.size(); j++) {
                final int thisStackLimit = Math.min(thisSlot.getMaxStackSize(), thisSlot.getMaxStackSize(thisStack));
                if (thisStack.getCount() >= thisStackLimit) {
                    break;
                }

                final var otherSlot = slots.get(j);
                final var otherStack = otherSlot.getItem();
                if (!otherStack.isEmpty() && ItemStack.isSameItemSameTags(thisStack, otherStack)) {
                    clicker.click(menu, otherSlot, 0, ClickType.PICKUP);
                    clicker.click(menu, thisSlot, 0, ClickType.PICKUP);
                    if (!menu.getCarried().isEmpty()) {
                        clicker.click(menu, otherSlot, 0, ClickType.PICKUP);
                    }
                }
            }
        }
    }

    private static boolean isSortableSlot(AbstractContainerScreen<?> screen, Slot slot) {
        // Hotbar and armor slots are never sortable
        if (slot.container instanceof Inventory) {
            final var containerSlot = slot.getContainerSlot();
            if (containerSlot < 9 || containerSlot >= Inventory.INVENTORY_SIZE) {
                return false;
            }
        }

        // We only sort the most standard slots you would find in your inventory or chests
        return PlatformBindings.INSTANCE.isSortableSlot(slot)
                || InventoryEssentialsExtensions.isSortableSlot(screen, slot);
    }
}
