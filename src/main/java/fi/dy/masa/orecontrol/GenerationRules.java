package fi.dy.masa.orecontrol;

import fi.dy.masa.orecontrol.config.Configs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;

import java.util.List;
import java.util.Map;

public final class GenerationRules {
    private static final Map<String, String> DECORATIONS = Map.ofEntries(
        Map.entry("brown_mushroom", "SHROOM"), Map.entry("red_mushroom", "SHROOM"),
        Map.entry("cactus", "CACTUS"), Map.entry("disk_clay", "CLAY"),
        Map.entry("dead_bush", "DEAD_BUSH"), Map.entry("waterlily", "LILYPAD"),
        Map.entry("grass", "GRASS"), Map.entry("grass_jungle", "GRASS"),
        Map.entry("tall_grass", "GRASS"), Map.entry("taiga_grass", "GRASS"),
        Map.entry("large_fern", "GRASS"), Map.entry("dry_grass", "GRASS"),
        Map.entry("pumpkin", "PUMPKIN"), Map.entry("sugar_cane", "REED"),
        Map.entry("forest_rock", "ROCK"), Map.entry("disk_sand", "SAND"),
        Map.entry("disk_gravel", "SAND_PASS2"), Map.entry("ice_patch", "ICE"),
        Map.entry("ice_spike", "ICE"), Map.entry("sunflower", "FLOWERS"),
        Map.entry("wildflower", "FLOWERS"), Map.entry("pale_forest_flower", "FLOWERS")
    );

    private GenerationRules() {}

    public static boolean shouldCancel(ConfiguredFeature<?, ?> configured, WorldGenLevel level, BlockPos pos) {
        // Natural generation uses a region. Commands and sapling/bone-meal growth use ServerLevel.
        if (!(level instanceof WorldGenRegion)) return false;
        Configs config = Configs.get(level.getLevel().dimension().identifier().toString());
        Identifier id = level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).getKey(configured);
        Identifier type = BuiltInRegistries.FEATURE.getKey(configured.feature());
        return disabled(configured, config, id, type, level, pos);
    }

    public static boolean disabled(ConfiguredFeature<?, ?> configured, Configs config, Identifier id,
                                   Identifier type, WorldGenLevel level, BlockPos pos) {
        List<OreConfiguration.TargetBlockState> targets = switch (configured.config()) {
            case OreConfiguration ore -> ore.targetStates;
            case ReplaceBlockConfiguration replacement -> replacement.targetStates;
            default -> List.of();
        };
        if (!targets.isEmpty()) {
            // A mod-owned configured ore is CUSTOM even when it places vanilla blocks.
            boolean custom = id != null && !id.getNamespace().equals("minecraft");
            custom |= targets.stream().anyMatch(t -> !BuiltInRegistries.BLOCK.getKey(t.state.getBlock()).getNamespace().equals("minecraft"));
            if (custom) return config.disabled("Minable", "CUSTOM");
            // Magma historically belonged to Populate, not the Minable event.
            if (id != null && id.getPath().equals("ore_magma")) return config.disabled("Populate", "NETHER_MAGMA");
            if (config.enabled("Minable", "disableAllVanillaGeneration")) return true;
            return targets.stream().anyMatch(target -> config.disabled("Minable", oreType(BuiltInRegistries.BLOCK.getKey(target.state.getBlock()).getPath())));
        }
        String path = id == null ? "" : id.getPath();
        String feature = type.getPath();
        String decoration = switch (feature) {
            case "tree", "fallen_tree", "huge_fungus" -> "TREE";
            case "huge_brown_mushroom", "huge_red_mushroom" -> "BIG_SHROOM";
            case "desert_well" -> "DESERT_WELL";
            case "fossil" -> "FOSSIL";
            default -> DECORATIONS.get(path);
        };
        if (path.startsWith("flower_")) decoration = "FLOWERS";
        if (decoration != null && config.disabled("Decorate", decoration)) return true;
        if (feature.equals("freeze_top_layer")) return config.disabled("Populate", "ICE");
        if (feature.equals("monster_room")) return config.disabled("Populate", "DUNGEON");
        if (feature.equals("glowstone_blob")) return config.disabled("Populate", "GLOWSTONE");
        if (path.equals("patch_fire") || path.equals("patch_soul_fire")) return config.disabled("Populate", "FIRE");
        if (configured.config() instanceof SpringConfiguration spring) {
            if (spring.state.is(FluidTags.WATER)) return config.disabled("Decorate", "LAKE_WATER");
            if (spring.state.is(FluidTags.LAVA)) {
                if (level.getLevel().dimension().identifier().toString().equals("minecraft:the_nether")) {
                    return config.disabled("Populate", spring.holeCount == 0 ? "NETHER_LAVA2" : "NETHER_LAVA");
                }
                return config.disabled("Decorate", "LAKE_LAVA");
            }
        }
        if (configured.config() instanceof LakeFeature.Configuration lake && lake.fluid() instanceof SimpleStateProvider fluid) {
            // A separate random source keeps natural generation's RNG sequence untouched.
            var state = fluid.getState(level, RandomSource.create(0), pos).getFluidState();
            if (state.is(FluidTags.LAVA)) {
                if (config.disabled("Populate", "LAVA")) return true;
                if (config.enabled("Custom", "disableSurfaceLavaLakes")) {
                    return pos.getY() >= level.getSeaLevel()
                        || pos.getY() >= level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()) - 8;
                }
            } else if (state.is(FluidTags.WATER) && config.disabled("Populate", "LAKE")) return true;
        }
        // Fabric has no Forge CUSTOM event. Apply these switches to mod-owned non-ore features.
        return id != null && !id.getNamespace().equals("minecraft")
            && (config.disabled("Decorate", "CUSTOM") || config.disabled("Populate", "CUSTOM"));
    }

    private static String oreType(String block) {
        if (block.startsWith("infested_")) return "SILVERFISH";
        if (block.equals("ancient_debris")) return "ANCIENT_DEBRIS";
        String name = block.replace("deepslate_", "").replace("nether_", "").replace("_ore", "");
        return name.toUpperCase(java.util.Locale.ROOT);
    }
}
