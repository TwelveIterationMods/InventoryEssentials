package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.inventoryessentials.InventoryEssentialsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

final class StackRefillHandler {

    private int pendingMenuSlot = -1;
    private ItemStack pendingStack = ItemStack.EMPTY;

    public void beforeUseItemOn(Minecraft client, LocalPlayer player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }

        reset();
        if (client.player != player || client.screen != null || !InventoryEssentialsConfig.getActive().enableStackRefill) {
            return;
        }

        final var stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof BlockItem)) {
            return;
        }

        pendingMenuSlot = InventoryMenu.USE_ROW_SLOT_START + player.getInventory().selected;
        pendingStack = stack.copy();
    }

    public void afterUseItemOn(Minecraft client, LocalPlayer player, InteractionHand hand, InteractionResult result) {
        if (hand != InteractionHand.MAIN_HAND || pendingMenuSlot == -1) {
            return;
        }

        if (client.player != player || !result.consumesAction() || !player.getItemInHand(hand).isEmpty()) {
            reset();
            return;
        }

        final var refillStack = pendingStack;
        final var targetMenuSlot = pendingMenuSlot;
        reset();

        if (client.gameMode == null || client.screen != null || !InventoryEssentialsConfig.getActive().enableStackRefill) {
            return;
        }

        final var menu = player.inventoryMenu;
        if (!menu.getCarried().isEmpty()) {
            return;
        }

        final var targetSlot = menu.isValidSlotIndex(targetMenuSlot) ? menu.getSlot(targetMenuSlot) : null;
        if (targetSlot == null || !targetSlot.getItem().isEmpty()) {
            return;
        }

        final var sourceSlot = findReplacementSlot(menu, refillStack);
        if (sourceSlot != null) {
            refillSlot(menu, sourceSlot, targetSlot, refillStack);
        }
    }

    private boolean matchesRefillStack(ItemStack emptiedStack, ItemStack refillStack) {
        return refillStack.getItem() instanceof BlockItem && ItemStack.isSameItemSameComponents(emptiedStack, refillStack);
    }

    private Slot findReplacementSlot(AbstractContainerMenu menu, ItemStack refillStack) {
        final var player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }

        for (final var slot : menu.slots) {
            if (!(slot.container instanceof Inventory)
                    || slot.getContainerSlot() < Inventory.getSelectionSize()
                    || slot.getContainerSlot() >= Inventory.INVENTORY_SIZE
                    || !slot.isActive()
                    || slot.isFake()
                    || !slot.hasItem()
                    || !slot.mayPickup(player)) {
                continue;
            }

            final var slotStack = slot.getItem();
            if (matchesRefillStack(refillStack, slotStack)) {
                return slot;
            }
        }

        return null;
    }

    private void refillSlot(AbstractContainerMenu menu, Slot sourceSlot, Slot targetSlot, ItemStack refillStack) {
        clickSlot(menu, sourceSlot);
        if (!matchesRefillStack(refillStack, menu.getCarried())) {
            if (!menu.getCarried().isEmpty()) {
                clickSlot(menu, sourceSlot);
            }
            return;
        }

        clickSlot(menu, targetSlot);
        if (!menu.getCarried().isEmpty()) {
            clickSlot(menu, sourceSlot);
        }
    }

    private void clickSlot(AbstractContainerMenu menu, Slot slot) {
        final var player = Minecraft.getInstance().player;
        final var gameMode = Minecraft.getInstance().gameMode;
        if (player != null && gameMode != null) {
            gameMode.handleInventoryMouseClick(menu.containerId, slot.index, 0, ClickType.PICKUP, player);
        }
    }

    public void reset() {
        pendingMenuSlot = -1;
        pendingStack = ItemStack.EMPTY;
    }
}
