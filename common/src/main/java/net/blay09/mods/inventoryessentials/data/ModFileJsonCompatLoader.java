package net.blay09.mods.inventoryessentials.data;

import com.google.gson.Gson;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.inventoryessentials.InventoryEssentialsIgnores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;

public class ModFileJsonCompatLoader {

    private static final Logger logger = LoggerFactory.getLogger(ModFileJsonCompatLoader.class);
    private static final Gson gson = new Gson();

    public static void load() {
        final var modPaths = Balm.lookupAllModPaths("inventoryessentials/ignores");
        modPaths.forEach((key, value) -> {
            try {
                try (final var walker = Files.walk(value)) {
                    walker.forEach(file -> {
                        if (file.toString().endsWith(".json")) {
                            try (final var reader = Files.newBufferedReader(file)) {
                                final var ignoredData = gson.fromJson(reader, IgnoredData.class);
                                if (ignoredData != null) {
                                    InventoryEssentialsIgnores.addIgnoredData(ignoredData);
                                }
                            } catch (IOException e) {
                                logger.error("Failed to load InventoryEssentials file {}", file, e);
                            }

                        }
                    });
                }
            } catch (IOException e) {
                logger.error("Failed to load InventoryEssentials files from mod {}", key, e);
            }
        });
    }

}
