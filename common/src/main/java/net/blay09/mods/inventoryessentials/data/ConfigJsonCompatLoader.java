package net.blay09.mods.inventoryessentials.data;

import com.google.gson.Gson;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.inventoryessentials.InventoryEssentialsIgnores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ConfigJsonCompatLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigJsonCompatLoader.class);
    private static final Gson gson = new Gson();

    public static void load() {
        final var configDir = new File(Balm.config().getConfigDir(), "inventoryessentials/ignores");
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
                final var ignoredData = gson.fromJson(reader, IgnoredData.class);
                if (ignoredData != null) {
                    InventoryEssentialsIgnores.addIgnoredData(ignoredData);
                }
            } catch (IOException e) {
                logger.error("Failed to load InventoryEssentials file {}", file, e);
            }
        }
    }

}
