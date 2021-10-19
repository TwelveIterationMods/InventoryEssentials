package net.blay09.mods.inventoryessentials;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.PlayerLoginEvent;
import net.blay09.mods.balm.api.event.client.ConnectedToServerEvent;
import net.blay09.mods.inventoryessentials.network.HelloMessage;
import net.blay09.mods.inventoryessentials.network.ModNetworking;

public class InventoryEssentials {

    public static final String MOD_ID = "inventoryessentials";
    public static boolean isServerSideInstalled;

    public static void initialize() {
        InventoryEssentialsConfig.initialize();
        ModNetworking.initialize(Balm.getNetworking());

        Balm.getEvents().onEvent(PlayerLoginEvent.class, event -> Balm.getNetworking().sendTo(event.getPlayer(), new HelloMessage()));
        Balm.getEvents().onEvent(ConnectedToServerEvent.class, event -> isServerSideInstalled = false);
    }

}
