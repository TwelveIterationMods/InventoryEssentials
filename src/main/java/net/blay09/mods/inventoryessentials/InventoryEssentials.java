package net.blay09.mods.inventoryessentials;

import net.blay09.mods.inventoryessentials.network.NetworkHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(InventoryEssentials.MOD_ID)
public class InventoryEssentials {

    public static final String MOD_ID = "inventoryessentials";
    public static boolean isServerSideInstalled;

    public InventoryEssentials() {
        NetworkHandler.init();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, InventoryEssentialsConfig.clientSpec);
    }

}
