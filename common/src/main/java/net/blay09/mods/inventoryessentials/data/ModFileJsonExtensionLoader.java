package net.blay09.mods.inventoryessentials.data;

import com.google.gson.Gson;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.inventoryessentials.InventoryEssentialsExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ModFileJsonExtensionLoader {

    private static final Logger logger = LoggerFactory.getLogger(ModFileJsonExtensionLoader.class);
    private static final Gson gson = new Gson();

    public static void load() {
        Balm.platform().loadedPrimaryModIds().forEach(modId -> Balm.platform().visitModResources(modId, "inventoryessentials/extensions", (resource) -> {
            if (resource.extension().equals("json")) {
                try (final var reader = resource.bufferedReader()) {
                    final var extensionData = gson.fromJson(reader, ExtensionData.class);
                    if (extensionData != null) {
                        InventoryEssentialsExtensions.addExtensionData(extensionData);
                    }
                } catch (IOException e) {
                    logger.error("Failed to load InventoryEssentials extension file {}", resource.name(), e);
                }
            }
        }));
    }
}
