package net.blay09.mods.inventoryessentials.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.blay09.mods.balm.api.client.keymappings.BalmKeyMappings;
import net.blay09.mods.balm.api.client.keymappings.KeyConflictContext;
import net.blay09.mods.balm.api.client.keymappings.KeyModifier;
import net.blay09.mods.balm.api.client.keymappings.KeyModifiers;
import net.minecraft.client.KeyMapping;

public class ModKeyMappings {

    public static KeyMapping keySingleTransfer;
    public static KeyMapping keyBulkTransfer;
    public static KeyMapping keyBulkTransferAll;
    public static KeyMapping keyBulkDrop;
    public static KeyMapping keyScreenBulkDrop;
    public static KeyMapping keyDragTransfer;

    public static void initialize(BalmKeyMappings keyMappings) {
        final var category = "key.categories.inventoryessentials";
        keySingleTransfer = keyMappings.registerKeyMapping("key.inventoryessentials.single_transfer",
                KeyConflictContext.GUI,
                KeyModifier.CONTROL,
                InputConstants.Type.MOUSE,
                InputConstants.MOUSE_BUTTON_LEFT,
                category);

        keyBulkTransfer = keyMappings.registerKeyMapping("key.inventoryessentials.bulk_transfer",
                KeyConflictContext.GUI,
                KeyModifiers.of(KeyModifier.SHIFT, KeyModifier.CONTROL),
                InputConstants.Type.MOUSE,
                InputConstants.MOUSE_BUTTON_LEFT,
                category);

        keyBulkTransferAll = keyMappings.registerKeyMapping("key.inventoryessentials.bulk_transfer_all",
                KeyConflictContext.GUI,
                KeyModifiers.ofCustom(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_SPACE)),
                InputConstants.Type.MOUSE,
                InputConstants.MOUSE_BUTTON_LEFT,
                category);

        keyBulkDrop = keyMappings.registerKeyMapping("key.inventoryessentials.bulk_drop",
                KeyConflictContext.GUI,
                KeyModifiers.of(KeyModifier.SHIFT, KeyModifier.CONTROL),
                InputConstants.KEY_Q,
                category);

        keyScreenBulkDrop = keyMappings.registerKeyMapping("key.inventoryessentials.screen_bulk_drop",
                KeyConflictContext.GUI,
                KeyModifier.SHIFT,
                InputConstants.Type.MOUSE,
                InputConstants.MOUSE_BUTTON_LEFT,
                category);

        keyDragTransfer = keyMappings.registerKeyMapping("key.inventoryessentials.drag_transfer",
                KeyConflictContext.GUI,
                KeyModifier.NONE,
                InputConstants.KEY_LSHIFT,
                category);

        keyMappings.ignoreConflicts(keySingleTransfer);
        keyMappings.ignoreConflicts(keyBulkTransfer);
        keyMappings.ignoreConflicts(keyBulkTransferAll);
        keyMappings.ignoreConflicts(keyBulkDrop);
        keyMappings.ignoreConflicts(keyScreenBulkDrop);
        keyMappings.ignoreConflicts(keyDragTransfer);
    }
}
