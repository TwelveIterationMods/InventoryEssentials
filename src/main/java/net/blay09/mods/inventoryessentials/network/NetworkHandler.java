package net.blay09.mods.inventoryessentials.network;

import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkHandler {

    public static SimpleChannel channel;

    public static void init() {
        channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(InventoryEssentials.MOD_ID, "network"), () -> "1.0", it -> {
            InventoryEssentials.isServerSideInstalled = it.equals("1.0");
            return true;
        }, it -> true);

        channel.registerMessage(0, SingleTransferMessage.class, SingleTransferMessage::encode, SingleTransferMessage::decode, SingleTransferMessage::handle);
    }

}
