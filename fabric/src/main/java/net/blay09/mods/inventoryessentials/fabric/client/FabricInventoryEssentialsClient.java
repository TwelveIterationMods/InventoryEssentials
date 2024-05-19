package net.blay09.mods.inventoryessentials.fabric.client;

import net.blay09.mods.balm.api.EmptyLoadContext;
import net.blay09.mods.balm.api.client.BalmClient;
import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.blay09.mods.inventoryessentials.client.InventoryEssentialsClient;
import net.fabricmc.api.ClientModInitializer;

public class FabricInventoryEssentialsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BalmClient.initialize(InventoryEssentials.MOD_ID, EmptyLoadContext.INSTANCE, InventoryEssentialsClient::initialize);
    }
}
