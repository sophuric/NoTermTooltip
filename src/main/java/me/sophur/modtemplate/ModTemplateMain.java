package me.sophur.modtemplate;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class ModTemplateMain implements ModInitializer {
    public static final String MOD_ID = "ModTemplate";
    public static final String MOD_ID_LOWER = "modtemplate";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
    }

    public static Identifier identifier(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID_LOWER, name);
    }

    public static Path getModDirectory() throws RuntimeException {
        var directory = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
        //noinspection ResultOfMethodCallIgnored
        directory.toFile().mkdirs();
        return directory;
    }
}
