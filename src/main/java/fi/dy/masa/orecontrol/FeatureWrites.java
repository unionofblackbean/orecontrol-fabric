package fi.dy.masa.orecontrol;

import fi.dy.masa.orecontrol.config.Configs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

/** Covers vegetation emitted by tree decorators and mixed providers inside a natural feature. */
public final class FeatureWrites {
    public record Context(WorldGenLevel level, Configs config) {}
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();
    private FeatureWrites() {}

    public static Context enter(WorldGenLevel level) {
        Context previous = CURRENT.get();
        if (level instanceof WorldGenRegion) {
            Configs config = Configs.get(level.getLevel().dimension().identifier().toString());
            if (config.hasDisabledDecorations()) CURRENT.set(new Context(level, config));
            else CURRENT.remove();
        } else CURRENT.remove();
        return previous;
    }

    public static void restore(Context previous) {
        if (previous == null) CURRENT.remove();
        else CURRENT.set(previous);
    }

    public static boolean blocked(WorldGenRegion level, BlockState state) {
        Context context = CURRENT.get();
        if (context == null || context.level != level) return false;
        String block = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        String group = switch (block) {
            case "bamboo", "bamboo_sapling" -> "BAMBOO";
            case "seagrass", "tall_seagrass" -> "SEAGRASS";
            case "kelp", "kelp_plant" -> "KELP";
            case "sea_pickle" -> "SEA_PICKLE";
            case "vine" -> "VINES";
            case "cave_vines", "cave_vines_plant" -> "CAVE_VINES";
            case "twisting_vines", "twisting_vines_plant", "weeping_vines", "weeping_vines_plant" -> "NETHER_VINES";
            case "leaf_litter" -> "LEAF_LITTER";
            case "short_grass", "tall_grass", "fern", "large_fern", "short_dry_grass", "tall_dry_grass" -> "GRASS";
            case "moss_block", "moss_carpet", "pale_moss_block", "pale_moss_carpet", "pale_hanging_moss", "azalea", "flowering_azalea" -> "MOSS";
            case "sweet_berry_bush" -> "BERRIES";
            case "bush" -> "BUSH";
            case "firefly_bush" -> "FIREFLY_BUSH";
            case "spore_blossom" -> "SPORE_BLOSSOM";
            case "small_dripleaf", "big_dripleaf", "big_dripleaf_stem" -> "DRIPLEAF";
            case "crimson_roots", "warped_roots", "nether_sprouts", "crimson_fungus", "warped_fungus" -> "NETHER_VEGETATION";
            case "hanging_roots", "rooted_dirt" -> "ROOTS";
            case "glow_lichen" -> "LICHEN";
            case "brown_mushroom", "red_mushroom" -> "SHROOM";
            case "sunflower", "lilac", "rose_bush", "peony", "pitcher_plant", "wildflowers", "pink_petals", "cactus_flower" -> "FLOWERS";
            default -> null;
        };
        if (block.contains("coral")) group = "CORAL";
        if (state.is(BlockTags.SMALL_FLOWERS)) group = "FLOWERS";
        return group != null && context.config.disabled("Decorate", group);
    }
}
