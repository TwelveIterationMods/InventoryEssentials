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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;

public class InventoryEssentialsClient {

    private static final InventoryControls clientOnlyControls = new ClientOnlyInventoryControls();
    private static final InventoryControls creativeControls = new CreativeInventoryControls();
    private static final InventoryControls serverSupportedControls = new ServerSupportedInventoryControls();

    private static Slot lastDragHoverSlot;

    public static void initialize() {
        ModKeyMappings.initialize(BalmClient.getKeyMappings());

        Balm.getEvents().onEvent(ScreenMouseEvent.Drag.Pre.class, InventoryEssentialsClient::onMouseDrag);
        Balm.getEvents().onEvent(ScreenMouseEvent.Click.Pre.class, InventoryEssentialsClient::onMouseClick);
        Balm.getEvents().onEvent(ScreenKeyEvent.Press.Pre.class, InventoryEssentialsClient::onKeyPress);
    }

    private static InventoryControls getInventoryControls(AbstractContainerScreen<?> screen) {
        if (screen instanceof CreativeModeInventoryScreenAccessor) {
            return creativeControls;
        }

        return InventoryEssentials.isServerSideInstalled && !InventoryEssentialsConfig.getActive().forceClientImplementation ? serverSupportedControls : clientOnlyControls;
    }

    private static boolean shouldHandleInput(AbstractContainerScreen<?> screen) {
        final var hoverSlot = ((AbstractContainerScreenAccessor) screen).getHoveredSlot();

        // Do not handle drags on crafting result slots
        if (hoverSlot instanceof ResultSlot) {
            return false;
        }

        if (screen instanceof CreativeModeInventoryScreenAccessor accessor) {
            return hoverSlot == null || hoverSlot.container instanceof Inventory || hoverSlot.container != accessor.getCONTAINER();
        }

        return true;
    }

    public static void onMouseDrag(ScreenMouseEvent.Drag.Pre event) {
        if (BalmClient.getKeyMappings().isActiveAndKeyDown(ModKeyMappings.keyDragTransfer)) {
            if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
                Slot hoverSlot = ((AbstractContainerScreenAccessor) screen).getHoveredSlot();
                if (hoverSlot == null || !shouldHandleInput(screen)) {
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
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) containerScreen;
            Slot hoverSlot = accessor.getHoveredSlot();
            if (!shouldHandleInput(containerScreen)) {
                return false;
            }

            InventoryControls controls = getInventoryControls(containerScreen);
            if (BalmClient.getKeyMappings().isActiveAndMatches(ModKeyMappings.keyBulkTransfer, key)) {
                return hoverSlot != null && controls.bulkTransferByType(containerScreen, hoverSlot);
            } else if (BalmClient.getKeyMappings().isActiveAndMatches(ModKeyMappings.keySingleTransfer, key)) {
                return hoverSlot != null && controls.singleTransfer(containerScreen, hoverSlot);
            } else if (BalmClient.getKeyMappings().isActiveAndMatches(ModKeyMappings.keyBulkTransferAll, key)) {
                return hoverSlot != null && controls.bulkTransferAll(containerScreen, hoverSlot);
            } else if (BalmClient.getKeyMappings().isActiveAndMatches(ModKeyMappings.keyScreenBulkDrop, key)) {
                final var clickedOutside = accessor.callHasClickedOutside(mouseX, mouseY, accessor.getLeftPos(), accessor.getTopPos(), key.getValue());
                return clickedOutside && controls.dropByType(containerScreen, containerScreen.getMenu().getCarried());
            } else if (BalmClient.getKeyMappings().isActiveAndMatches(ModKeyMappings.keyBulkDrop, key)) {
                return hoverSlot != null && controls.dropByType(containerScreen, hoverSlot);
            }
        }
        return false;
    }

}
