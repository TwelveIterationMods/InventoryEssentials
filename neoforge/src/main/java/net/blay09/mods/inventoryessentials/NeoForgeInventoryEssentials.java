package net.blay09.mods.inventoryessentials;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.client.BalmClient;
import net.blay09.mods.inventoryessentials.client.InventoryEssentialsClient;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.DistExecutor;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.items.SlotItemHandler;

@Mod(InventoryEssentials.MOD_ID)
public class NeoForgeInventoryEssentials {

    public NeoForgeInventoryEssentials() {
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

        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> true));
    }

}
