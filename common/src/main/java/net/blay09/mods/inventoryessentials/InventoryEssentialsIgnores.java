package net.blay09.mods.inventoryessentials;

import net.blay09.mods.inventoryessentials.data.IgnoredData;
import net.blay09.mods.inventoryessentials.mixin.AbstractContainerScreenAccessor;
import net.blay09.mods.inventoryessentials.mixin.CreativeModeInventoryScreenAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class InventoryEssentialsIgnores {

    private static final Set<String> ignoredScreenClasses = new HashSet<>();
    private static final Set<String> ignoredMenuClasses = new HashSet<>();
    private static final Set<String> ignored = new HashSet<>();
    private static final Set<ResourceLocation> ignoredMenuTypes = new HashSet<>();

    public static boolean shouldIgnoreScreen(Screen screen) {
        if (!(screen instanceof AbstractContainerScreenAccessor)) {
            return true;
        }

        if (ignoredScreenClasses.contains(screen.getClass().getName())) {
            return true;
        }

        final var menu = ((AbstractContainerScreen<?>) screen).getMenu();
        if (ignoredMenuClasses.contains(menu.getClass().getName())) {
            return true;
        }

        final var typeId = BuiltInRegistries.MENU.getKey(menu.getType());
        //noinspection RedundantIfStatement
        if (ignoredMenuTypes.contains(typeId)) {
            return true;
        }

        return false;
    }

    public static boolean shouldIgnoreSlot(AbstractContainerScreen<?> screen, @Nullable Slot slot) {
        if (slot == null) {
            return true;
        }

        if (ignored.contains(slot.getClass().getName())) {
            return true;
        }

        // Do not handle drags on crafting result slots
        if (slot instanceof ResultSlot) {
            return true;
        }

        if (screen instanceof CreativeModeInventoryScreenAccessor creativeAccessor) {
            return !(slot.container instanceof Inventory) && slot.container == creativeAccessor.getCONTAINER();
        }

        return false;
    }

    public static void addIgnoredMenuType(ResourceLocation menuId) {
        ignoredMenuTypes.add(menuId);
    }

    public static void addIgnoredMenuClass(String menuClass) {
        ignoredMenuClasses.add(menuClass);
    }

    public static void addIgnoredScreenClass(String screenClass) {
        ignoredScreenClasses.add(screenClass);
    }

    public static void addIgnoredSlotClass(String slotClass) {
        ignored.add(slotClass);
    }

    public static void addIgnoredData(IgnoredData ignoredData) {
        ignoredData.ignoredMenuClasses.forEach(InventoryEssentialsIgnores::addIgnoredMenuClass);
        ignoredData.ignoredMenuTypes.stream().map(ResourceLocation::parse).forEach(InventoryEssentialsIgnores::addIgnoredMenuType);
        ignoredData.ignoredScreenClasses.forEach(InventoryEssentialsIgnores::addIgnoredScreenClass);
        ignoredData.ignoredSlotClasses.forEach(InventoryEssentialsIgnores::addIgnoredSlotClass);
    }
}
