package net.blay09.mods.inventoryessentials;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.client.BalmClient;
import net.blay09.mods.inventoryessentials.client.InventoryEssentialsClient;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.NetworkConstants;

@Mod(InventoryEssentials.MOD_ID)
public class ForgeInventoryEssentials {

    public ForgeInventoryEssentials() {
        PlatformBindings.INSTANCE = new PlatformBindings() {
            @Override
            public boolean isSameInventory(Slot targetSlot, Slot slot) {
                if (targetSlot instanceof SlotItemHandler && slot instanceof SlotItemHandler) {
                    return ((SlotItemHandler) targetSlot).getItemHandler() == ((SlotItemHandler) slot).getItemHandler();
                }

                return slot.isSameInventory(targetSlot);
            }
        };

        Balm.initialize(InventoryEssentials.MOD_ID, InventoryEssentials::initialize);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> BalmClient.initialize(InventoryEssentials.MOD_ID, InventoryEssentialsClient::initialize));

        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

}
