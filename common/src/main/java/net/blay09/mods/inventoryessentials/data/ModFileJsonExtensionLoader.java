package net.blay09.mods.inventoryessentials.data;

import com.google.gson.Gson;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.inventoryessentials.InventoryEssentialsExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;

public class ModFileJsonExtensionLoader {

    private static final Logger logger = LoggerFactory.getLogger(ModFileJsonExtensionLoader.class);
    private static final Gson gson = new Gson();

    public static void load() {
        final var modPaths = Balm.lookupAllModPaths("inventoryessentials/extensions");
        modPaths.forEach((key, value) -> {
            try {
                try (final var walker = Files.walk(value)) {
                    walker.forEach(file -> {
                        if (file.toString().endsWith(".json")) {
                            try (final var reader = Files.newBufferedReader(file)) {
                                final var extensionData = gson.fromJson(reader, ExtensionData.class);
                                if (extensionData != null) {
                                    InventoryEssentialsExtensions.addExtensionData(extensionData);
                                }
                            } catch (IOException e) {
                                logger.error("Failed to load InventoryEssentials extension file {}", file, e);
                            }
                        }
                    });
                }
            } catch (IOException e) {
                logger.error("Failed to load InventoryEssentials extension files from mod {}", key, e);
            }
        });
    }
}
