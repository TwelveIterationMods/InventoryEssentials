package net.blay09.mods.inventoryessentials.fabric;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.EmptyLoadContext;
import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.blay09.mods.inventoryessentials.PlatformBindings;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.inventory.Slot;

public class FabricInventoryEssentials implements ModInitializer {
    @Override
    public void onInitialize() {
        PlatformBindings.INSTANCE = new PlatformBindings() {
            @Override
            public boolean isSameInventory(Slot targetSlot, Slot slot) {
                return slot.container == targetSlot.container;
            }
        };

        Balm.initialize(InventoryEssentials.MOD_ID, EmptyLoadContext.INSTANCE, InventoryEssentials::initialize);
    }
}
