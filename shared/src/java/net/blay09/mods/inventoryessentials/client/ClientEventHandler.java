package net.blay09.mods.inventoryessentials.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.blay09.mods.balm.api.event.client.screen.ScreenKeyEvent;
import net.blay09.mods.balm.api.event.client.screen.ScreenMouseEvent;
import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.blay09.mods.inventoryessentials.InventoryEssentialsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

public class ClientEventHandler {

    private static final InventoryControls clientOnlyControls = new ClientOnlyInventoryControls();
    private static final InventoryControls serverSupportedControls = new ServerSupportedInventoryControls();

    private static Slot lastDragHoverSlot;

    private static InventoryControls getInventoryControls() {
        return InventoryEssentials.isServerSideInstalled && !InventoryEssentialsConfig.getActive().forceClientImplementation ? serverSupportedControls : clientOnlyControls;
    }

    public static void onMouseDrag(ScreenMouseEvent.Drag event) {
        if (Screen.hasShiftDown() && InventoryEssentialsConfig.getActive().enableShiftDrag) {
            InventoryControls controls = getInventoryControls();
            if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
                Slot hoverSlot = screen.getSlotUnderMouse();
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
            Slot hoverSlot = screen.getSlotUnderMouse();

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
            Slot hoverSlot = screen.getSlotUnderMouse();
            InputConstants.Key input = InputConstants.getKey(event.getKey(), event.getScanCode());

            if (Screen.hasShiftDown() && Screen.hasControlDown() && Minecraft.getInstance().options.keyDrop.isActiveAndMatches(input) && InventoryEssentialsConfig.getActive().enableBulkDrop) {
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
