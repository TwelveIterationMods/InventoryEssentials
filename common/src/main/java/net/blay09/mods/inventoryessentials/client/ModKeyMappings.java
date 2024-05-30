package net.blay09.mods.inventoryessentials.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.blay09.mods.inventoryessentials.mixin.AbstractContainerScreenAccessor;
import net.blay09.mods.kuma.api.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

import java.util.function.BiFunction;

public class ModKeyMappings {

    public static ManagedKeyMapping keySingleTransfer;
    public static ManagedKeyMapping keyBulkTransfer;
    public static ManagedKeyMapping keyBulkTransferAll;
    public static ManagedKeyMapping keyBulkDrop;
    public static ManagedKeyMapping keyScreenBulkDrop;
    public static ManagedKeyMapping keyDragTransfer;

    public static void initialize() {
        keySingleTransfer = Kuma.createKeyMapping(new ResourceLocation(InventoryEssentials.MOD_ID, "single_transfer"))
                .withDefault(InputBinding.mouse(InputConstants.MOUSE_BUTTON_LEFT, KeyModifiers.of(KeyModifier.CONTROL)))
                .handleScreenInput(event -> handleSlotInput(event,
                        (screen, slot) -> InventoryEssentialsClient.getInventoryControls(screen).singleTransfer(screen, slot)))
                .build();

        keyBulkTransfer = Kuma.createKeyMapping(new ResourceLocation(InventoryEssentials.MOD_ID, "bulk_transfer"))
                .withDefault(InputBinding.mouse(InputConstants.MOUSE_BUTTON_LEFT, KeyModifiers.of(KeyModifier.SHIFT, KeyModifier.CONTROL)))
                .handleScreenInput(event -> handleSlotInput(event,
                        (screen, slot) -> InventoryEssentialsClient.getInventoryControls(screen).bulkTransferByType(screen, slot)))
                .build();

        keyBulkTransferAll = Kuma.createKeyMapping(new ResourceLocation(InventoryEssentials.MOD_ID, "bulk_transfer_all"))
                .withDefault(InputBinding.mouse(InputConstants.MOUSE_BUTTON_LEFT, KeyModifiers.ofCustom(InputConstants.getKey(InputConstants.KEY_SPACE, -1))))
                .handleScreenInput(event -> handleSlotInput(event,
                        (screen, slot) -> InventoryEssentialsClient.getInventoryControls(screen).bulkTransferAll(screen, slot)))
                .build();

        keyBulkDrop = Kuma.createKeyMapping(new ResourceLocation(InventoryEssentials.MOD_ID, "bulk_drop"))
                .withDefault(InputBinding.key(InputConstants.KEY_Q, KeyModifiers.of(KeyModifier.SHIFT, KeyModifier.CONTROL)))
                .handleScreenInput(event -> handleSlotInput(event,
                        (screen, slot) -> InventoryEssentialsClient.getInventoryControls(screen).dropByType(screen, slot)))
                .build();

        keyScreenBulkDrop = Kuma.createKeyMapping(new ResourceLocation(InventoryEssentials.MOD_ID, "screen_bulk_drop"))
                .withDefault(InputBinding.mouse(InputConstants.MOUSE_BUTTON_LEFT, KeyModifiers.of(KeyModifier.SHIFT)))
                .handleScreenInput(event -> {
                    if (!InventoryEssentialsClient.shouldHandleInput(event.screen())) {
                        return false;
                    }

                    if (!(event.screen() instanceof AbstractContainerScreen<?> containerScreen)) {
                        return false;
                    }

                    final var accessor = (AbstractContainerScreenAccessor) containerScreen;
                    int button = keyScreenBulkDrop.getBinding().key().getValue();
                    final var clickedOutside = accessor.callHasClickedOutside(event.mouseX(),
                            event.mouseY(),
                            accessor.getLeftPos(),
                            accessor.getTopPos(),
                            button);
                    return clickedOutside && InventoryEssentialsClient.getInventoryControls(containerScreen)
                            .dropByType(containerScreen, containerScreen.getMenu().getCarried());
                })
                .build();

        keyDragTransfer = Kuma.createKeyMapping(new ResourceLocation(InventoryEssentials.MOD_ID, "drag_transfer"))
                .withDefault(InputBinding.key(InputConstants.KEY_LSHIFT))
                .withContext(KeyConflictContext.SCREEN)
                .build();
    }

    private static boolean handleSlotInput(ScreenInputEvent event, BiFunction<AbstractContainerScreen<?>, Slot, Boolean> handler) {
        if (!InventoryEssentialsClient.shouldHandleInput(event.screen())) {
            return false;
        }

        if (!(event.screen() instanceof AbstractContainerScreen<?> containerScreen)) {
            return false;
        }

        final var hoverSlot = ((AbstractContainerScreenAccessor) containerScreen).getHoveredSlot();
        if (hoverSlot != null) {
            return handler.apply(containerScreen, hoverSlot);
        }

        return false;
    }
}
