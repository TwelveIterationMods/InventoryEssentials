package net.blay09.mods.inventoryessentials.neoforge;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.blay09.mods.inventoryessentials.PlatformBindings;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.items.SlotItemHandler;

@Mod(InventoryEssentials.MOD_ID)
public class NeoForgeInventoryEssentials {

    public NeoForgeInventoryEssentials(ModContainer modContainer, IEventBus modEventBus) {
        PlatformBindings.INSTANCE = new PlatformBindings() {
            @Override
            public boolean isSameInventory(Slot targetSlot, Slot slot) {
                if (targetSlot instanceof SlotItemHandler && slot instanceof SlotItemHandler) {
                    return ((SlotItemHandler) targetSlot).getItemHandler() == ((SlotItemHandler) slot).getItemHandler();
                }

                return slot.isSameInventory(targetSlot);
            }
        };

        final var context = new NeoForgeLoadContext(modContainer, modEventBus);
        Balm.initializeMod(InventoryEssentials.MOD_ID, context, InventoryEssentials::initialize);
    }

}
