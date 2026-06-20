package net.blay09.mods.inventoryessentials;

import net.blay09.mods.inventoryessentials.data.ExtensionData;
import net.blay09.mods.inventoryessentials.data.IgnoredSlot;
import net.blay09.mods.inventoryessentials.data.SortableData;
import net.blay09.mods.inventoryessentials.data.SortableSlot;
import net.blay09.mods.inventoryessentials.mixin.AbstractContainerMenuAccessor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.Slot;

import java.util.HashSet;
import java.util.Set;

public class InventoryEssentialsExtensions {

    private static final Set<IgnoredSlot> ignoredSlots = new HashSet<>();
    private static final Set<SortableSlot> sortableSlots = new HashSet<>();

    public static boolean shouldIgnoreScreen(AbstractContainerScreen<?> screen) {
        return ignoredSlots.stream().anyMatch(ignoredSlot ->
                ignoredSlot.slotClass == null && matches(ignoredSlot.screenClass,
                        ignoredSlot.menuClass,
                        null,
                        ignoredSlot.menuType,
                        screen,
                        null));
    }

    public static boolean shouldIgnoreSlot(AbstractContainerScreen<?> screen, Slot slot) {
        return ignoredSlots.stream().anyMatch(ignoredSlot ->
                matches(ignoredSlot.screenClass,
                        ignoredSlot.menuClass,
                        ignoredSlot.slotClass,
                        ignoredSlot.menuType,
                        screen,
                        slot));
    }

    public static boolean isSortableSlot(AbstractContainerScreen<?> screen, Slot slot) {
        return sortableSlots.stream().anyMatch(sortableSlot ->
                matches(sortableSlot.screenClass,
                        sortableSlot.menuClass,
                        sortableSlot.slotClass,
                        sortableSlot.menuType,
                        screen,
                        slot));
    }

    private static boolean matches(String screenClass,
                                   String menuClass,
                                   String slotClass,
                                   String menuType,
                                   AbstractContainerScreen<?> screen,
                                   Slot slot) {
        final var menu = screen.getMenu();
        final var registeredMenuType = ((AbstractContainerMenuAccessor) menu).balm$getMenuType();
        final var menuTypeId = registeredMenuType != null ? BuiltInRegistries.MENU.getKey(registeredMenuType) : null;
        return (screenClass == null || screenClass.equals(screen.getClass().getName()))
                && (menuClass == null || menuClass.equals(menu.getClass().getName()))
                && (slotClass == null || slot != null && slotClass.equals(slot.getClass().getName()))
                && (menuType == null || menuTypeId != null && menuType.equals(menuTypeId.toString()));
    }

    public static void addIgnoredSlot(IgnoredSlot ignoredSlot) {
        ignoredSlots.add(ignoredSlot);
    }

    public static void addSortableSlot(SortableSlot sortableSlot) {
        sortableSlots.add(sortableSlot);
    }

    public static void addSortableData(SortableData sortableData) {
        sortableData.sortableSlots.forEach(InventoryEssentialsExtensions::addSortableSlot);
    }

    public static void addExtensionData(ExtensionData extensionData) {
        if (extensionData.ignores != null) {
            extensionData.ignores.forEach(InventoryEssentialsExtensions::addIgnoredSlot);
        }
        if (extensionData.sorting != null) {
            addSortableData(extensionData.sorting);
        }
    }
}
