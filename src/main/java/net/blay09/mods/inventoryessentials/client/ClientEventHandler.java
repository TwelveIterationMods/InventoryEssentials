package net.blay09.mods.inventoryessentials.client;

import net.blay09.mods.inventoryessentials.InventoryEssentials;
import net.blay09.mods.inventoryessentials.InventoryEssentialsConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = InventoryEssentials.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {

    private static final InventoryControls clientOnlyControls = new ClientOnlyInventoryControls();
    private static final InventoryControls serverSupportedControls = new ServerSupportedInventoryControls();

    private static Slot lastDragHoverSlot;

    private static InventoryControls getInventoryControls() {
        return InventoryEssentials.isServerSideInstalled && !InventoryEssentialsConfig.CLIENT.forceClientImplementation.get() ? serverSupportedControls : clientOnlyControls;
    }

    @SubscribeEvent
    public static void onMouseDrag(GuiScreenEvent.MouseDragEvent event) {
        if (Screen.hasShiftDown() && InventoryEssentialsConfig.CLIENT.enableShiftDrag.get()) {
            InventoryControls controls = getInventoryControls();
            if (event.getGui() instanceof ContainerScreen<?>) {
                ContainerScreen<?> screen = (ContainerScreen<?>) event.getGui();
                Slot hoverSlot = screen.getSlotUnderMouse();
                if (hoverSlot != null && hoverSlot.getHasStack() && hoverSlot != lastDragHoverSlot) {
                    controls.dragBulkTransfer(screen, hoverSlot);
                    lastDragHoverSlot = hoverSlot;
                }
            }
        } else {
            lastDragHoverSlot = null;
        }
    }

    @SubscribeEvent
    public static void onMouseClick(GuiScreenEvent.MouseClickedEvent.Pre event) {
        InventoryControls controls = getInventoryControls();
        if (event.getGui() instanceof ContainerScreen<?>) {
            ContainerScreen<?> screen = (ContainerScreen<?>) event.getGui();
            Slot hoverSlot = screen.getSlotUnderMouse();
            if (Screen.hasShiftDown() && Screen.hasControlDown() && InventoryEssentialsConfig.CLIENT.enableBulkTransfer.get()) {
                if (hoverSlot != null && controls.bulkTransfer(screen, hoverSlot)) {
                    event.setCanceled(true);
                }
            } else if (Screen.hasControlDown() && InventoryEssentialsConfig.CLIENT.enableSingleTransfer.get()) {
                if (hoverSlot != null && controls.singleTransfer(screen, hoverSlot)) {
                    event.setCanceled(true);
                }
            }
        }
    }


}
