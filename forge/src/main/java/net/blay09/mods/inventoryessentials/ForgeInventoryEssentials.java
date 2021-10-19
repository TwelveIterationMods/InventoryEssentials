package net.blay09.mods.inventoryessentials;

import net.blay09.mods.balm.api.Balm;
import net.minecraftforge.fml.common.Mod;

@Mod(InventoryEssentials.MOD_ID)
public class ForgeInventoryEssentials {

    public ForgeInventoryEssentials() {
        Balm.initialize(InventoryEssentials.MOD_ID, InventoryEssentials::initialize);
    }

}
