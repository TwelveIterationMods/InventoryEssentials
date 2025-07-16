package net.blay09.mods.inventoryessentials;

import net.blay09.mods.balm.api.Balm;

public class InventoryEssentialsConfig {
    public static InventoryEssentialsConfigData getActive() {
        return Balm.getConfig().getActive(InventoryEssentialsConfigData.class);
    }

    public static void initialize() {
        Balm.getConfig().registerConfig(InventoryEssentialsConfigData.class, null);
    }
}

