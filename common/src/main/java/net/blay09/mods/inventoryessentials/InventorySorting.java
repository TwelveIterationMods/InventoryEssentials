package net.blay09.mods.inventoryessentials;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum InventorySorting implements StringRepresentable {
    CONSOLIDATE_ONLY,
    ALPHABETICAL,
    CREATIVE;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
