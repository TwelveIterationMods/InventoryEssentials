package net.blay09.mods.inventoryessentials;

import me.shedaniel.autoconfig.annotation.Config;
import net.blay09.mods.balm.api.config.BalmConfigData;
import net.blay09.mods.balm.api.config.Comment;

@Config(name = InventoryEssentials.MOD_ID)
public class InventoryEssentialsConfigData implements BalmConfigData {

    @Comment("Use the client implementation even on servers that have the mod installed - only useful for development purposes.")
    public boolean forceClientImplementation;

    @Comment("Should ctrl-clicking only move one item at a time instead of the full stack?")
    public boolean enableSingleTransfer = true;

    @Comment("Should shift-ctrl-clicking move all items of the same type at once?")
    public boolean enableBulkTransfer = true;

    @Comment("Should space-clicking an item move all items from that inventory at once?")
    public boolean enableBulkTransferAll = true;

    @Comment("Should shift-ctrl-drop-clicking drop all items of the same type at once?")
    public boolean enableBulkDrop = true;

    @Comment("Should space-clicking move all items even if an empty slot was clicked?")
    public boolean allowBulkTransferAllOnEmptySlot = false;

    @Comment("Should holding shift and moving your mouse over items quick-transfer them without requiring each to be clicked?")
    public boolean enableShiftDrag = true;

}

