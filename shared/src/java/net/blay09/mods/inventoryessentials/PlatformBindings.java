package net.blay09.mods.inventoryessentials;

import net.minecraft.world.inventory.Slot;

import java.util.Optional;

public abstract class PlatformBindings {

    public static PlatformBindings INSTANCE;

    public abstract Optional<Boolean> isSameInventory(Slot targetSlot, Slot slot, boolean treatHotBarAsSeparate);

}
