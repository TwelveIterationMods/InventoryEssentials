package net.blay09.mods.inventoryessentials;

import net.minecraft.world.inventory.Slot;

public abstract class PlatformBindings {

    public static PlatformBindings INSTANCE;

    public abstract boolean isSameInventory(Slot targetSlot, Slot slot);

}
