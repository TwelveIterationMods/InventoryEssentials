package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.inventoryessentials.InventoryEssentialsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

final class ToolRefillHandler {

    private int pendingMenuSlot = -1;
    private ItemStack pendingStack = ItemStack.EMPTY;

    public void beforeContainerSetSlot(Minecraft client, ClientboundContainerSetSlotPacket packet) {
        reset();

        final var player = client.player;
        final var config = InventoryEssentialsConfig.getActive();
        if (player == null || client.screen != null || !config.enableToolRefill) {
            return;
        }

        if (packet.getContainerId() != player.inventoryMenu.containerId) {
            return;
        }

        final int packetSlot = packet.getSlot();
        if (packetSlot < InventoryMenu.USE_ROW_SLOT_START || packetSlot >= InventoryMenu.USE_ROW_SLOT_END) {
            return;
        }

        final int hotbarSlot = packetSlot - InventoryMenu.USE_ROW_SLOT_START;
        if (hotbarSlot != player.getInventory().getSelectedSlot() || !packet.getItem().isEmpty()) {
            return;
        }

        final var oldStack = player.inventoryMenu.getSlot(packetSlot).getItem();
        if (!canRefill(oldStack)) {
            return;
        }

        pendingMenuSlot = packetSlot;
        pendingStack = oldStack.copy();
    }

    public void afterContainerSetSlot(Minecraft client, ClientboundContainerSetSlotPacket packet) {
        if (pendingMenuSlot == -1 || packet.getSlot() != pendingMenuSlot) {
            reset();
            return;
        }

        final var refillStack = pendingStack;
        reset();

        final var player = client.player;
        if (player == null || client.gameMode == null || client.screen != null || !InventoryEssentialsConfig.getActive().enableToolRefill) {
            return;
        }

        final var menu = player.inventoryMenu;
        if (packet.getContainerId() != menu.containerId || !menu.getCarried().isEmpty()) {
            return;
        }

        final var targetSlot = menu.isValidSlotIndex(packet.getSlot()) ? menu.getSlot(packet.getSlot()) : null;
        if (targetSlot == null || !targetSlot.getItem().isEmpty()) {
            return;
        }

        final var sourceSlot = findReplacementSlot(menu, refillStack);
        if (sourceSlot != null) {
            refillSlot(menu, sourceSlot, targetSlot, refillStack);
        }
    }

    public void reset() {
        pendingMenuSlot = -1;
        pendingStack = ItemStack.EMPTY;
    }

    private boolean canRefill(ItemStack emptiedStack) {
        return emptiedStack.isDamageableItem() && emptiedStack.nextDamageWillBreak();
    }

    private boolean matchesRefillStack(ItemStack emptiedStack, ItemStack refillStack) {
        return refillStack.isDamageableItem() && ItemStack.isSameItem(emptiedStack, refillStack);
    }

    private @Nullable Slot findReplacementSlot(AbstractContainerMenu menu, ItemStack refillStack) {
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
            gameMode.handleContainerInput(menu.containerId, slot.index, 0, ContainerInput.PICKUP, player);
        }
    }
}
