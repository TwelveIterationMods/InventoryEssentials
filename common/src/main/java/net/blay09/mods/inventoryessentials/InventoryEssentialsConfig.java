package net.blay09.mods.inventoryessentials;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.config.reflection.Comment;
import net.blay09.mods.balm.api.config.reflection.Config;

@Config(InventoryEssentials.MOD_ID)
public class InventoryEssentialsConfig {

    @Comment("Use the client implementation even on servers that have the mod installed - only useful for development purposes.")
    public boolean forceClientImplementation;

    @Comment("Should space-clicking move all items even if an empty slot was clicked?")
    public boolean allowBulkTransferAllOnEmptySlot = false;

    @Comment("Should space-clicking armor in the inventory swap to all matching armor?")
    public boolean bulkTransferArmorSets = true;

    public static InventoryEssentialsConfig getActive() {
        return Balm.getConfig().getActiveConfig(InventoryEssentialsConfig.class);
    }

    public static void initialize() {
        Balm.getConfig().registerConfig(InventoryEssentialsConfig.class);
    }
}

