package fi.dy.masa.orecontrol;

import fi.dy.masa.orecontrol.config.Configs;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class OreControl implements ModInitializer {
    @Override
    public void onInitialize() {
        reload();
    }

    public static void reload() {
        try {
            Configs.load(FabricLoader.getInstance().getConfigDir().resolve("orecontrol"));
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Unable to read Ore Control configuration", e);
        }
    }
}
