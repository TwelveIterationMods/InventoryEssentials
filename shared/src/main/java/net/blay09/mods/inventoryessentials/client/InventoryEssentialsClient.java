package net.blay09.mods.inventoryessentials.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.client.BalmClient;
import net.blay09.mods.balm.api.event.client.screen.ScreenKeyEvent;
import net.blay09.mods.balm.api.event.client.screen.ScreenMouseEvent;
import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.blay09.mods.inventoryessentials.InventoryEssentialsConfig;
import net.blay09.mods.inventoryessentials.mixin.AbstractContainerScreenAccessor;
import net.blay09.mods.inventoryessentials.mixin.CreativeModeInventoryScreenAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;

public class InventoryEssentialsClient {

    private static final InventoryControls clientOnlyControls = new ClientOnlyInventoryControls();
    private static final InventoryControls serverSupportedControls = new ServerSupportedInventoryControls();

    private static Slot lastDragHoverSlot;

    public static void initialize() {
        ModKeyMappings.initialize(BalmClient.getKeyMappings());

        Balm.getEvents().onEvent(ScreenMouseEvent.Drag.Pre.class, InventoryEssentialsClient::onMouseDrag);
        Balm.getEvents().onEvent(ScreenMouseEvent.Click.Pre.class, InventoryEssentialsClient::onMouseClick);
        Balm.getEvents().onEvent(ScreenKeyEvent.Press.Pre.class, InventoryEssentialsClient::onKeyPress);
    }

    private static InventoryControls getInventoryControls() {
        return InventoryEssentials.isServerSideInstalled && !InventoryEssentialsConfig.getActive().forceClientImplementation ? serverSupportedControls : clientOnlyControls;
    }

    public static void onMouseDrag(ScreenMouseEvent.Drag.Pre event) {
        if (BalmClient.getKeyMappings().isActiveAndKeyDown(ModKeyMappings.keyDragTransfer)) {
            InventoryControls controls = getInventoryControls();
            if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
                Slot hoverSlot = ((AbstractContainerScreenAccessor) screen).getHoveredSlot();
                if (hoverSlot == null) {
                    return;
                }

                // Do not handle drags on crafting result slots
                if (hoverSlot instanceof ResultSlot) {
                    return;
                }

                if (event.getScreen() instanceof CreativeModeInventoryScreenAccessor accessor) {
                    if (hoverSlot.container == accessor.getCONTAINER()) {
                        return;
                    }
                }

                if (hoverSlot.hasItem() && hoverSlot != lastDragHoverSlot) {
                    controls.dragTransfer(screen, hoverSlot);
                    lastDragHoverSlot = hoverSlot;
                }
            }
        } else {
            lastDragHoverSlot = null;
        }
    }

    public static void onMouseClick(ScreenMouseEvent.Click.Pre event) {
        if (onInput(event.getScreen(), InputConstants.Type.MOUSE.getOrCreate(event.getButton()), event.getMouseX(), event.getMouseY())) {
            event.setCanceled(true);
        }
    }

    public static void onKeyPress(ScreenKeyEvent.Press.Pre event) {
        if (onInput(event.getScreen(), InputConstants.getKey(event.getKey(), event.getScanCode()), 0, 0)) {
            event.setCanceled(true);
        }
    }

    public static boolean onInput(Screen screen, InputConstants.Key key, double mouseX, double mouseY) {
        InventoryControls controls = getInventoryControls();
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) containerScreen;
            Slot hoverSlot = accessor.getHoveredSlot();

            // Do not handle clicks on crafting result slots.
            if (hoverSlot instanceof ResultSlot) {
                return false;
            }

            if (BalmClient.getKeyMappings().isActiveAndMatches(ModKeyMappings.keyBulkTransfer, key)) {
                if (hoverSlot != null && controls.bulkTransferByType(containerScreen, hoverSlot)) {
                    return true;
                }
            } else if (BalmClient.getKeyMappings().isActiveAndMatches(ModKeyMappings.keySingleTransfer, key)) {
                if (hoverSlot != null && controls.singleTransfer(containerScreen, hoverSlot)) {
                    return true;
                }
            } else if (BalmClient.getKeyMappings().isActiveAndMatches(ModKeyMappings.keyBulkTransferAll, key)) {
                if (hoverSlot != null && controls.bulkTransferAll(containerScreen, hoverSlot)) {
                    return true;
                }
            } else if (BalmClient.getKeyMappings().isActiveAndMatches(ModKeyMappings.keyScreenBulkDrop, key)) {
                if (accessor.callHasClickedOutside(mouseX, mouseY, accessor.getLeftPos(), accessor.getTopPos(), key.getValue()) && controls.dropByType(
                        containerScreen,
                        containerScreen.getMenu().getCarried())) {
                    return true;
                }
            } else if (BalmClient.getKeyMappings().isActiveAndMatches(ModKeyMappings.keyBulkDrop, key)) {
                if (hoverSlot != null && controls.dropByType(containerScreen, hoverSlot)) {
                    return true;
                }
            }
        }
        return false;
    }

}
