package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.client.screen.ScreenMouseEvent;
import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.blay09.mods.inventoryessentials.InventoryEssentialsConfig;
import net.blay09.mods.inventoryessentials.InventoryEssentialsIgnores;
import net.blay09.mods.inventoryessentials.mixin.AbstractContainerScreenAccessor;
import net.blay09.mods.inventoryessentials.mixin.CreativeModeInventoryScreenAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.Slot;

public class InventoryEssentialsClient {

    private static final InventoryControls clientOnlyControls = new ClientOnlyInventoryControls();
    private static final InventoryControls creativeControls = new CreativeInventoryControls();
    private static final InventoryControls serverSupportedControls = new ServerSupportedInventoryControls();

    private static Slot lastDragHoverSlot;
    private static boolean hasDragClicked;

    public static void initialize() {
        ModKeyMappings.initialize();

        Balm.getEvents().onEvent(ScreenMouseEvent.Drag.Pre.class, InventoryEssentialsClient::onMouseDrag);
        Balm.getEvents().onEvent(ScreenMouseEvent.Release.Pre.class, InventoryEssentialsClient::onMouseRelease);
    }

    public static InventoryControls getInventoryControls(Screen screen) {
        if (screen instanceof CreativeModeInventoryScreenAccessor) {
            return creativeControls;
        }

        return InventoryEssentials.isServerSideInstalled && !InventoryEssentialsConfig.getActive().forceClientImplementation ? serverSupportedControls : clientOnlyControls;
    }

    public static void onMouseRelease(ScreenMouseEvent.Release.Pre event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            Slot hoverSlot = ((AbstractContainerScreenAccessor) screen).getHoveredSlot();
            if (hoverSlot == null || InventoryEssentialsIgnores.shouldIgnoreScreen(screen) || InventoryEssentialsIgnores.shouldIgnoreSlot(screen, hoverSlot)) {
                return;
            }

            if (hasDragClicked) {
                event.setCanceled(true);
                hasDragClicked = false;
            }
        }
    }

    public static void onMouseDrag(ScreenMouseEvent.Drag.Pre event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            Slot hoverSlot = ((AbstractContainerScreenAccessor) screen).getHoveredSlot();
            if (hoverSlot == null || InventoryEssentialsIgnores.shouldIgnoreScreen(screen) || InventoryEssentialsIgnores.shouldIgnoreSlot(screen, hoverSlot)) {
                return;
            }

            // If shift is held, perform drag transfer
            if (ModKeyMappings.keyDragTransfer.isActiveAndDown() && (event.getButton() == 0 || event.getButton() == 1)) {
                if (hoverSlot.hasItem() && hoverSlot != lastDragHoverSlot) {
                    InventoryControls controls = getInventoryControls(screen);
                    if (InventoryEssentialsConfig.getActive().enableShiftDrag) {
                        controls.dragTransfer(screen, hoverSlot);
                    }
                    lastDragHoverSlot = hoverSlot;
                }
                return;
            }

            // If dragging mouse button while holding a bundle, perform drag clicks
            if (InventoryEssentialsConfig.getActive().enableBundleDrag) {
                final var carriedStack = screen.getMenu().getCarried();
                if (carriedStack.is(ItemTags.BUNDLES)) {
                    if (hoverSlot != lastDragHoverSlot) {
                        if ((event.getButton() == 0 && hoverSlot.hasItem()) || (event.getButton() == 1 && !hoverSlot.hasItem())) {
                            final var controls = getInventoryControls(screen);
                            controls.dragClick(screen, hoverSlot, event.getButton());
                            hasDragClicked = true;
                            // Quick-craft causes subsequent clicks to not work right because it never gets reset due to our cancels
                            ((AbstractContainerScreenAccessor) screen).setIsQuickCrafting(false);
                        }
                        lastDragHoverSlot = hoverSlot;
                    }
                    event.setCanceled(true);
                    return;
                }
            }

            lastDragHoverSlot = null;
        } else {
            lastDragHoverSlot = null;
        }
    }

}
