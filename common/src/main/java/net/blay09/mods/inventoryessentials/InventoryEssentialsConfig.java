package net.blay09.mods.inventoryessentials;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.config.reflection.Comment;
import net.blay09.mods.balm.platform.config.reflection.Config;

import java.util.Objects;

@Config(InventoryEssentials.MOD_ID)
public class InventoryEssentialsConfig {

    @Comment("Use the client implementation even on servers that have the mod installed - only useful for development purposes.")
    public boolean forceClientImplementation;

    @Comment("Should space-clicking move all items even if an empty slot was clicked?")
    public boolean allowBulkTransferAllOnEmptySlot = false;

    @Comment("Should space-clicking armor in the inventory swap to all matching armor?")
    public boolean bulkTransferArmorSets = true;

    @Comment("Should ctrl-clicking only move one item at a time instead of the full stack?")
    public boolean enableSingleTransfer = true;

    @Comment("Should shift-ctrl-clicking move all items of the same type at once?")
    public boolean enableBulkTransfer = true;

    @Comment("Should control-space-clicking an item move one of each item from that inventory at once?")
    public boolean enableBulkTransferSingle = true;

    @Comment("Should space-clicking an item move all items from that inventory at once?")
    public boolean enableBulkTransferAll = true;

    @Comment("Should shift-ctrl-drop-clicking drop all items of the same type at once?")
    public boolean enableBulkDrop = true;

    @Comment("Should holding shift and moving your mouse over items quick-transfer them without requiring each to be clicked?")
    public boolean enableShiftDrag = true;

    @Comment("Should holding click or right-click with a bundle empty or insert hovered slots?")
    public boolean enableBundleDrag = true;

    @Comment("Choose how middle-click inventory sorting should behave.")
    public InventorySorting inventorySorting = InventorySorting.ALPHABETICAL;

    public static InventoryEssentialsConfig getActive() {
        return Objects.requireNonNull(Balm.config().getActiveConfig(InventoryEssentialsConfig.class));
    }

    public static void initialize() {
        Balm.config().registerConfig(InventoryEssentialsConfig.class);
    }
}

