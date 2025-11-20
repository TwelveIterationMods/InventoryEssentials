package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.balm.client.BalmClientRegistrars;
import net.blay09.mods.balm.client.platform.event.callback.ClientLifecycleCallback;
import net.blay09.mods.balm.client.platform.event.callback.ScreenCallback;
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

    public static void initialize(BalmClientRegistrars registrars) {
        ClientLifecycleCallback.DisconnectedFromServer.EVENT.register(client -> InventoryEssentials.isServerSideInstalled = false);

        ModKeyMappings.initialize();

        ScreenCallback.MouseDrag.BEFORE.register(InventoryEssentialsClient::onMouseDrag);
        ScreenCallback.MouseRelease.BEFORE.register(InventoryEssentialsClient::onMouseRelease);
    }

    public static InventoryControls getInventoryControls(Screen screen) {
        if (screen instanceof CreativeModeInventoryScreenAccessor) {
            return creativeControls;
        }

        return InventoryEssentials.isServerSideInstalled && !InventoryEssentialsConfig.getActive().forceClientImplementation ? serverSupportedControls : clientOnlyControls;
    }

    public static boolean onMouseRelease(Screen screen, double mouseX, double mouseY, int button, boolean consumed) {
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            Slot hoverSlot = ((AbstractContainerScreenAccessor) containerScreen).getHoveredSlot();
            if (hoverSlot == null || InventoryEssentialsIgnores.shouldIgnoreScreen(containerScreen) || InventoryEssentialsIgnores.shouldIgnoreSlot(containerScreen, hoverSlot)) {
                return false;
            }

            if (hasDragClicked) {
                hasDragClicked = false;
                return true;
            }
        }
        return false;
    }

    public static boolean onMouseDrag(Screen screen, double mouseX, double mouseY, int button, double horizontalAmount, double verticalAmount, boolean consumed) {
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            Slot hoverSlot = ((AbstractContainerScreenAccessor) containerScreen).getHoveredSlot();
            if (hoverSlot == null || InventoryEssentialsIgnores.shouldIgnoreScreen(containerScreen) || InventoryEssentialsIgnores.shouldIgnoreSlot(containerScreen, hoverSlot)) {
                return false;
            }

            // If shift is held, perform drag transfer
            if (ModKeyMappings.keyDragTransfer.isActiveAndDown() && (button == 0 || button == 1)) {
                if (hoverSlot.hasItem() && hoverSlot != lastDragHoverSlot) {
                    InventoryControls controls = getInventoryControls(containerScreen);
                    if (InventoryEssentialsConfig.getActive().enableShiftDrag) {
                        controls.dragTransfer(containerScreen, hoverSlot);
                    }
                    lastDragHoverSlot = hoverSlot;
                }
                return false;
            }

            // If dragging mouse button while holding a bundle, perform drag clicks
            if (InventoryEssentialsConfig.getActive().enableBundleDrag) {
                final var carriedStack = containerScreen.getMenu().getCarried();
                if (carriedStack.is(ItemTags.BUNDLES)) {
                    if (hoverSlot != lastDragHoverSlot) {
                        if ((button == 0 && hoverSlot.hasItem()) || (button == 1 && !hoverSlot.hasItem())) {
                            final var controls = getInventoryControls(containerScreen);
                            controls.dragClick(containerScreen, hoverSlot, button);
                            hasDragClicked = true;
                            // Quick-craft causes subsequent clicks to not work right because it never gets reset due to our cancels
                            ((AbstractContainerScreenAccessor) containerScreen).setIsQuickCrafting(false);
                        }
                        lastDragHoverSlot = hoverSlot;
                    }
                    return true;
                }
            }

            lastDragHoverSlot = null;
        } else {
            lastDragHoverSlot = null;
        }

        return false;
    }

}
