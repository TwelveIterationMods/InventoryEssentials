package net.blay09.mods.inventoryessentials.data;

import com.google.gson.Gson;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.inventoryessentials.InventoryEssentialsExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ConfigJsonExtensionLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigJsonExtensionLoader.class);
    private static final Gson gson = new Gson();

    public static void load() {
        final var configDir = new File(Balm.config().getConfigDir(), "inventoryessentials/extensions");
        if (!configDir.exists() && !configDir.mkdirs()) {
            logger.error("Failed to create InventoryEssentials config directory {}", configDir);
            return;
        }

        final var files = configDir.listFiles(it -> it.getName().endsWith(".json"));
        if (files == null) {
            return;
        }

        for (final var file : files) {
            try (final var reader = Files.newBufferedReader(file.toPath())) {
                final var extensionData = gson.fromJson(reader, ExtensionData.class);
                if (extensionData != null) {
                    InventoryEssentialsExtensions.addExtensionData(extensionData);
                }
            } catch (IOException e) {
                logger.error("Failed to load InventoryEssentials extension file {}", file, e);
            }
        }
    }
}
