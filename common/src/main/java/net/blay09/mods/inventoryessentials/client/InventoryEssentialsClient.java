package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.client.screen.ScreenMouseEvent;
import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.blay09.mods.inventoryessentials.InventoryEssentialsIgnores;
import net.blay09.mods.inventoryessentials.InventoryEssentialsConfig;
import net.blay09.mods.inventoryessentials.mixin.AbstractContainerScreenAccessor;
import net.blay09.mods.inventoryessentials.mixin.CreativeModeInventoryScreenAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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

    public static void onMouseDrag(ScreenMouseEvent.Drag.Pre event) {
        if (ModKeyMappings.keyDragTransfer.isActiveAndDown() && (event.getButton() == 0 || event.getButton() == 1)) {
            if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
                Slot hoverSlot = ((AbstractContainerScreenAccessor) screen).getHoveredSlot();
                if (hoverSlot == null || InventoryEssentialsIgnores.shouldIgnoreScreen(screen) || InventoryEssentialsIgnores.shouldIgnoreSlot(screen, hoverSlot)) {
                    return;
                }

                if (hoverSlot.hasItem() && hoverSlot != lastDragHoverSlot) {
                    InventoryControls controls = getInventoryControls(screen);
                    if (InventoryEssentialsConfig.getActive().enableShiftDrag) {
                        controls.dragTransfer(screen, hoverSlot);
                    }
                    lastDragHoverSlot = hoverSlot;
                }
            }
        } else {
            lastDragHoverSlot = null;
        }
    }

}
