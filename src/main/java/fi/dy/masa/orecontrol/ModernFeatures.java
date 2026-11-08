package fi.dy.masa.orecontrol;

import java.util.HashMap;
import java.util.Map;

/** Groups from the actual Minecraft 26.1.2 configured-feature registry. */
public final class ModernFeatures {
    private static final Map<String, String> NAMED = new HashMap<>();
    static {
        register("BERRIES", "berry_bush");
        register("BUSH", "bush");
        register("FIREFLY_BUSH", "firefly_bush");
        register("LEAF_LITTER", "leaf_litter");
        register("MELON", "melon");
        register("MOSS", "moss_patch moss_patch_bonemeal moss_patch_ceiling moss_vegetation pale_moss_patch pale_moss_patch_bonemeal pale_moss_vegetation");
        register("DRIPLEAF", "dripleaf");
        register("CAVE_CLAY", "lush_caves_clay clay_pool_with_dripleaves clay_with_dripleaves");
        register("SPORE_BLOSSOM", "spore_blossom");
        register("NETHER_VEGETATION", "crimson_roots");
        register("FLOWERS", "forest_flowers");
        register("CORAL", "warm_ocean_vegetation");
        register("CAVE_VINES", "cave_vine cave_vine_in_moss");
        register("LICHEN", "glow_lichen");
        register("SCULK", "sculk_vein sculk_patch_deep_dark sculk_patch_ancient_city");
        register("DRIPSTONE", "pointed_dripstone");
        register("DIRT_DISK", "disk_grass");
        register("TREE", "birch_tall dark_forest_vegetation pale_garden_vegetation mangrove_vegetation meadow_trees");
    }

    private ModernFeatures() {}

    private static void register(String name, String features) {
        for (String feature : features.split(" ")) NAMED.put(feature, name);
    }

    public static String group(String path, String type) {
        String named = NAMED.get(path);
        if (named != null) return named;
        if (path.startsWith("trees_")) return "TREE";
        return switch (type) {
            case "bamboo" -> "BAMBOO";
            case "seagrass" -> "SEAGRASS";
            case "kelp" -> "KELP";
            case "sea_pickle" -> "SEA_PICKLE";
            case "coral_tree", "coral_claw", "coral_mushroom" -> "CORAL";
            case "vines" -> "VINES";
            case "twisting_vines", "weeping_vines" -> "NETHER_VINES";
            case "nether_forest_vegetation" -> "NETHER_VEGETATION";
            case "chorus_plant" -> "CHORUS";
            case "root_system" -> "ROOTS";
            case "sculk_patch" -> "SCULK";
            case "dripstone_cluster", "large_dripstone", "pointed_dripstone" -> "DRIPSTONE";
            case "geode" -> "GEODE";
            case "basalt_columns", "basalt_pillar" -> "BASALT";
            case "netherrack_replace_blobs" -> path.equals("blackstone_blobs") ? "BLACKSTONE_BLOBS" : "BASALT";
            case "delta_feature" -> "DELTA";
            case "iceberg" -> "ICEBERG";
            case "blue_ice" -> "BLUE_ICE";
            case "underwater_magma" -> "UNDERWATER_MAGMA";
            case "end_island" -> "END_ISLAND";
            case "end_spike" -> "END_SPIKE";
            case "end_gateway" -> "END_GATEWAY";
            case "end_platform", "void_start_platform" -> "END_PLATFORM";
            case "block_pile" -> "BLOCK_PILES";
            default -> null;
        };
    }
}
