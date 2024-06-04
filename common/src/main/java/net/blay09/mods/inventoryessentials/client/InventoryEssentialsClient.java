package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.client.screen.ScreenMouseEvent;
import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.blay09.mods.inventoryessentials.InventoryEssentialsConfig;
import net.blay09.mods.inventoryessentials.mixin.AbstractContainerScreenAccessor;
import net.blay09.mods.inventoryessentials.mixin.CreativeModeInventoryScreenAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;

public class InventoryEssentialsClient {

    private static final InventoryControls clientOnlyControls = new ClientOnlyInventoryControls();
    private static final InventoryControls creativeControls = new CreativeInventoryControls();
    private static final InventoryControls serverSupportedControls = new ServerSupportedInventoryControls();

    private static Slot lastDragHoverSlot;

    public static void initialize() {
        ModKeyMappings.initialize();

        Balm.getEvents().onEvent(ScreenMouseEvent.Drag.Pre.class, InventoryEssentialsClient::onMouseDrag);
    }

    public static InventoryControls getInventoryControls(Screen screen) {
        if (screen instanceof CreativeModeInventoryScreenAccessor) {
            return creativeControls;
        }

        return InventoryEssentials.isServerSideInstalled && !InventoryEssentialsConfig.getActive().forceClientImplementation ? serverSupportedControls : clientOnlyControls;
    }

    public static boolean shouldHandleInput(Screen screen) {
        if (!(screen instanceof AbstractContainerScreenAccessor accessor)) {
            return false;
        }

        final var hoverSlot = accessor.getHoveredSlot();

        // Do not handle drags on crafting result slots
        if (hoverSlot instanceof ResultSlot) {
            return false;
        }

        if (screen instanceof CreativeModeInventoryScreenAccessor creativeAccessor) {
            return hoverSlot == null || hoverSlot.container instanceof Inventory || hoverSlot.container != creativeAccessor.getCONTAINER();
        }

        return true;
    }

    public static void onMouseDrag(ScreenMouseEvent.Drag.Pre event) {
        if (ModKeyMappings.keyDragTransfer.isActiveAndDown()) {
            if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
                Slot hoverSlot = ((AbstractContainerScreenAccessor) screen).getHoveredSlot();
                if (hoverSlot == null || !shouldHandleInput(screen) || !shouldHandleSlot(hoverSlot)) {
                    return;
                }

                if (hoverSlot.hasItem() && hoverSlot != lastDragHoverSlot) {
                    InventoryControls controls = getInventoryControls(screen);
                    controls.dragTransfer(screen, hoverSlot);
                    lastDragHoverSlot = hoverSlot;
                }
            }
        } else {
            lastDragHoverSlot = null;
        }
    }

    private static boolean shouldHandleSlot(Slot slot) {
        // Try to detect fake slots and ignore them
        //noinspection ConstantValue
        if (slot.container == null || slot.container.getContainerSize() == 0) {
            return false;
        }

        return true;
    }

}
