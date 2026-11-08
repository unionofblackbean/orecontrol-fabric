package fi.dy.masa.orecontrol;

import fi.dy.masa.orecontrol.config.Configs;
import net.minecraft.resources.Identifier;
import java.util.Locale;

public final class StructureRules {
    private StructureRules() {}

    public static String group(Identifier id) {
        if (id == null || !id.getNamespace().equals("minecraft")) return "CUSTOM";
        String name = id.getPath();
        if (name.startsWith("village_")) return "VILLAGE";
        if (name.equals("mineshaft_mesa")) return "MINESHAFT";
        if (name.startsWith("ruined_portal_")) return "RUINED_PORTAL";
        if (name.startsWith("ocean_ruin_")) return "OCEAN_RUIN";
        if (name.equals("shipwreck_beached")) return "SHIPWRECK";
        return name.toUpperCase(Locale.ROOT);
    }

    public static boolean disabled(Configs config, Identifier id) {
        return config.disabled("Structures", "ALL") || config.disabled("Structures", group(id));
    }
}
