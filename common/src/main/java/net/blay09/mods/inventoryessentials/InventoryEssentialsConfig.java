package net.blay09.mods.inventoryessentials;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.common.config.ConfigLocalization;

public class InventoryEssentialsConfig {
    public static InventoryEssentialsConfigData getActive() {
        return Balm.getConfig().getActive(InventoryEssentialsConfigData.class);
    }

    public static void initialize() {
        ConfigLocalization.enableModernTranslationKeys(InventoryEssentials.MOD_ID);
        Balm.getConfig().registerConfig(InventoryEssentialsConfigData.class, null);
    }
}

