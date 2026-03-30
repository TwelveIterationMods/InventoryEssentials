package net.blay09.mods.inventoryessentials.client.sorting;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RetainOrderSorting {
    public static List<ItemStack> computeSortedList(List<ItemStack> stacks) {
        final var firstSeenStacks = new ArrayList<ItemStack>();
        for (final var stack : stacks) {
            if (findMatchingStackIndex(firstSeenStacks, stack) == -1) {
                firstSeenStacks.add(stack);
            }
        }

        return stacks.stream()
                .sorted(Comparator.comparingInt(stack -> findMatchingStackIndex(firstSeenStacks, stack)))
                .toList();
    }

    private static int findMatchingStackIndex(List<ItemStack> stacks, ItemStack targetStack) {
        for (int i = 0; i < stacks.size(); i++) {
            if (ItemStack.isSameItemSameComponents(stacks.get(i), targetStack)) {
                return i;
            }
        }
        return -1;
    }
}
