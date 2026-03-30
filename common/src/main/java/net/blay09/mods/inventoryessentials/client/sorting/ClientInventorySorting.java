package net.blay09.mods.inventoryessentials.client.sorting;

import net.blay09.mods.inventoryessentials.InventorySorting;
import net.blay09.mods.inventoryessentials.InventoryUtils;
import net.blay09.mods.inventoryessentials.tags.ModItemTags;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ClientInventorySorting {

    private static final Comparator<ItemStack> defaultComparator =
            Comparator.comparing((ItemStack itemStack) -> itemStack.getHoverName().getString(), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed())
                    .thenComparing(itemStack -> itemStack.isEnchanted() ? 0 : 1)
                    .thenComparingInt(ItemStack::getDamageValue)
                    .thenComparing(itemStack -> Objects.toString(itemStack.getComponents(), ""));

    @FunctionalInterface
    public interface SlotClicker {
        void click(AbstractContainerMenu menu, Slot slot, int mouseButton, ClickType clickType);
    }

    public static boolean sort(AbstractContainerMenu menu, Slot baseSlot, InventorySorting sortingMode, SlotClicker clicker) {
        final var player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        final var slotsToSort = new ArrayList<Slot>();
        for (final var slot : menu.slots) {
            if (isSortableSlot(slot) && InventoryUtils.isSameInventory(baseSlot, slot, true)) {
                slotsToSort.add(slot);
            }
        }

        if (slotsToSort.isEmpty()) {
            return false;
        }

        // Merge matching stacks first before sorting
        consolidateStacks(menu, slotsToSort, clicker);

        if (sortingMode == InventorySorting.CONSOLIDATE_ONLY) {
            return true;
        }

        // Compute the sorted order
        final var goalSorting = computeSortedList(slotsToSort, sortingMode);

        // Swap items to match the new sorting
        for (int i = 0; i < goalSorting.size(); i++) {
            final var goalStack = goalSorting.get(i);
            final var currentStack = slotsToSort.get(i).getItem();
            // If current stack already matches goal stack, skip it
            if (ItemStack.isSameItemSameComponents(goalStack, currentStack)
                    && goalStack.getCount() == currentStack.getCount()) {
                continue;
            }

            // Find the first stack from here that matches the goal stack
            int foundSwapIndex = -1;
            for (int j = i + 1; j < slotsToSort.size(); j++) {
                final var candidateStack = slotsToSort.get(j).getItem();
                if (ItemStack.isSameItemSameComponents(goalStack, candidateStack)
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

    private static Comparator<ItemStack> getComparator(InventorySorting sortingMode) {
        return switch (sortingMode) {
            case CONSOLIDATE_ONLY -> throw new IllegalStateException("No comparator available for CONSOLIDATE_ONLY");
            case RETAIN_ORDER -> throw new IllegalStateException("No comparator available for RETAIN_ORDER");
            case ALPHABETICAL -> defaultComparator;
            case CREATIVE -> CreativeSorting.getCreativeComparator().thenComparing(defaultComparator);
        };
    }

    private static List<ItemStack> computeSortedList(List<Slot> slotsToSort, InventorySorting sortingMode) {
        final var stacks = slotsToSort.stream()
                .map(Slot::getItem)
                .map(ItemStack::copy)
                .filter(stack -> !stack.isEmpty())
                .toList();
        if (sortingMode == InventorySorting.RETAIN_ORDER) {
            return RetainOrderSorting.computeSortedList(stacks);
        }

        return stacks.stream()
                .sorted(getComparator(sortingMode))
                .toList();
    }

    private static void swapSlots(AbstractContainerMenu menu, List<Slot> slots, int firstIndex, int secondIndex, SlotClicker clicker) {
        if (firstIndex != secondIndex) {
            final var firstSlot = slots.get(firstIndex);
            final var secondSlot = slots.get(secondIndex);
            final var firstStack = firstSlot.getItem();
            final var secondStack = secondSlot.getItem();

            // If one of the two slots is empty, we just have to do a simple move
            if (!firstSlot.hasItem() || !secondSlot.hasItem()) {
                final var fromSlot = firstSlot.hasItem() ? firstSlot : secondSlot;
                final var toSlot = firstSlot.hasItem() ? secondSlot : firstSlot;
                clicker.click(menu, fromSlot, 0, ClickType.PICKUP);
                clicker.click(menu, toSlot, 0, ClickType.PICKUP);
                return;
            }

            // We can't swap with a bundle normally because clicking it would insert the item - try another way
            if (firstStack.is(ModItemTags.BUNDLES) || secondStack.is(ModItemTags.BUNDLES)) {
                Slot emptyBufferSlot = null;
                for (final var candidate : slots) {
                    if (!candidate.hasItem()) {
                        emptyBufferSlot = candidate;
                        break;
                    }
                }

                // If we found an empty slot to use as a buffer, use it to swap the two slots; otherwise just leave the bundle untouched
                if (emptyBufferSlot != null) {
                    clicker.click(menu, firstSlot, 0, ClickType.PICKUP);
                    clicker.click(menu, emptyBufferSlot, 0, ClickType.PICKUP);
                    clicker.click(menu, secondSlot, 0, ClickType.PICKUP);
                    clicker.click(menu, firstSlot, 0, ClickType.PICKUP);
                    clicker.click(menu, emptyBufferSlot, 0, ClickType.PICKUP);
                    clicker.click(menu, secondSlot, 0, ClickType.PICKUP);
                }
            } else {
                clicker.click(menu, firstSlot, 0, ClickType.PICKUP);
                clicker.click(menu, secondSlot, 0, ClickType.PICKUP);
                clicker.click(menu, firstSlot, 0, ClickType.PICKUP);
            }
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
                final var otherSlot = slots.get(j);
                final var otherStack = otherSlot.getItem();

                // We ignore bundles because clicking them would insert the item into the bundle
                if (thisStack.is(ModItemTags.BUNDLES) || otherStack.is(ModItemTags.BUNDLES)) {
                    continue;
                }

                if (!otherStack.isEmpty() && ItemStack.isSameItemSameComponents(thisStack, otherStack)) {
                    clicker.click(menu, otherSlot, 0, ClickType.PICKUP);
                    clicker.click(menu, thisSlot, 0, ClickType.PICKUP);
                    if (!menu.getCarried().isEmpty()) {
                        clicker.click(menu, otherSlot, 0, ClickType.PICKUP);
                    }

                    final var newThisStack = thisSlot.getItem();
                    final int newThisStackFull = newThisStack.getMaxStackSize();
                    if (newThisStack.getCount() >= newThisStackFull) {
                        break;
                    }
                }
            }
        }
    }

    private static boolean isSortableSlot(Slot slot) {
        // Hotbar and armor slots are never sortable
        if (slot.container instanceof Inventory) {
            final var containerSlot = slot.getContainerSlot();
            if (containerSlot < 9 || containerSlot >= Inventory.INVENTORY_SIZE) {
                return false;
            }
        }

        // We only sort the most standard slots you would find in your inventory or chests
        return slot.getClass() == Slot.class
                || slot.getClass() == ShulkerBoxSlot.class;
    }

}
