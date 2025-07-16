package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class HelloMessage {
    private boolean dummy;

    public static void encode(HelloMessage message, FriendlyByteBuf buf) {
    }

    public static HelloMessage decode(FriendlyByteBuf buf) {
        return new HelloMessage();
    }

    public static void handle(Player player, HelloMessage message) {
        InventoryEssentials.isServerSideInstalled = true;
    }
}
