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
        Balm.getLoadedPrimaryModIds().forEach(modId -> Balm.visitModResources("inventoryessentials/ignores", modId, (resource) -> {
            if (resource.extension().equals("json")) {
                try (final var reader = resource.bufferedReader()) {
                    final var ignoredData = gson.fromJson(reader, IgnoredData.class);
                    if (ignoredData != null) {
                        InventoryEssentialsIgnores.addIgnoredData(ignoredData);
                    }
                } catch (IOException e) {
                    logger.error("Failed to load InventoryEssentials file {}", resource.name(), e);
                }
            }
        }));
    }

}
