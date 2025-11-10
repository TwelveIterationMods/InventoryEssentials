package net.blay09.mods.inventoryessentials.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {

    public static final TagKey<Item> BUNDLES = TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("bundles"));

}
