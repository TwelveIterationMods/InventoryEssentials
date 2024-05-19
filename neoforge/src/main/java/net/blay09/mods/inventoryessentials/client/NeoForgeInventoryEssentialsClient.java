package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.balm.api.client.BalmClient;
import net.blay09.mods.balm.neoforge.NeoForgeLoadContext;
import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = InventoryEssentials.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeInventoryEssentialsClient {

    public NeoForgeInventoryEssentialsClient(IEventBus modEventBus) {
        final var context = new NeoForgeLoadContext(modEventBus);
        BalmClient.initialize(InventoryEssentials.MOD_ID, context, InventoryEssentialsClient::initialize);
    }

}
