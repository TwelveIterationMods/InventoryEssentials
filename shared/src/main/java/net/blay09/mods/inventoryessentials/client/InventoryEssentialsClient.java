package net.blay09.mods.inventoryessentials.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.client.BalmClient;
import net.blay09.mods.balm.api.event.client.screen.ScreenKeyEvent;
import net.blay09.mods.balm.api.event.client.screen.ScreenMouseEvent;
import net.blay09.mods.balm.mixin.AbstractContainerScreenAccessor;
import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.blay09.mods.inventoryessentials.InventoryEssentialsConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

public class InventoryEssentialsClient {

    private static final InventoryControls clientOnlyControls = new ClientOnlyInventoryControls();
    private static final InventoryControls serverSupportedControls = new ServerSupportedInventoryControls();

    private static Slot lastDragHoverSlot;

    public static void initialize() {
        Balm.getEvents().onEvent(ScreenMouseEvent.Drag.Pre.class, InventoryEssentialsClient::onMouseDrag);
        Balm.getEvents().onEvent(ScreenMouseEvent.Click.Pre.class, InventoryEssentialsClient::onMouseClick);
        Balm.getEvents().onEvent(ScreenKeyEvent.Press.Pre.class, InventoryEssentialsClient::onKeyPress);
    }

    private static InventoryControls getInventoryControls() {
        return InventoryEssentials.isServerSideInstalled && !InventoryEssentialsConfig.getActive().forceClientImplementation ? serverSupportedControls : clientOnlyControls;
    }

    public static void onMouseDrag(ScreenMouseEvent.Drag.Pre event) {
        if (Screen.hasShiftDown() && InventoryEssentialsConfig.getActive().enableShiftDrag) {
            InventoryControls controls = getInventoryControls();
            if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
                Slot hoverSlot = ((AbstractContainerScreenAccessor) screen).getHoveredSlot();
                if (hoverSlot != null && hoverSlot.hasItem() && hoverSlot != lastDragHoverSlot) {
                    controls.dragTransfer(screen, hoverSlot);
                    lastDragHoverSlot = hoverSlot;
                }
            }
        } else {
            lastDragHoverSlot = null;
        }
    }

    public static void onMouseClick(ScreenMouseEvent.Click.Pre event) {
        InventoryControls controls = getInventoryControls();
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            Slot hoverSlot = ((AbstractContainerScreenAccessor) screen).getHoveredSlot();

            // Do not handle clicks on crafting result slots.
            if (hoverSlot instanceof ResultSlot) {
                return;
            }

            if (Screen.hasShiftDown() && Screen.hasControlDown() && InventoryEssentialsConfig.getActive().enableBulkTransfer) {
                if (hoverSlot != null && controls.bulkTransferByType(screen, hoverSlot)) {
                    event.setCanceled(true);
                }
            } else if (Screen.hasControlDown() && InventoryEssentialsConfig.getActive().enableSingleTransfer) {
                if (hoverSlot != null && controls.singleTransfer(screen, hoverSlot)) {
                    event.setCanceled(true);
                }
            } else if (hasSpaceDown() && InventoryEssentialsConfig.getActive().enableBulkTransferAll) {
                if (hoverSlot != null && controls.bulkTransferAll(screen, hoverSlot)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    public static void onKeyPress(ScreenKeyEvent.Press.Pre event) {
        InventoryControls controls = getInventoryControls();
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            Slot hoverSlot = ((AbstractContainerScreenAccessor) screen).getHoveredSlot();

            KeyMapping keyDrop = Minecraft.getInstance().options.keyDrop;
            if (Screen.hasShiftDown() && Screen.hasControlDown() && BalmClient.getKeyMappings().isActiveAndMatches(keyDrop, event.getKey(), event.getScanCode()) && InventoryEssentialsConfig.getActive().enableBulkDrop) {
                if (hoverSlot != null && controls.dropByType(screen, hoverSlot)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    private static boolean hasSpaceDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_SPACE);
    }

}
