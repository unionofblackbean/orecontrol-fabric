package fi.dy.masa.orecontrol.integration;

import fi.dy.masa.orecontrol.GenerationRules;
import fi.dy.masa.orecontrol.OreControl;
import fi.dy.masa.orecontrol.config.Configs;
import fi.dy.masa.orecontrol.reset.RegionReset;
import fi.dy.masa.orecontrol.reset.WorldReset;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Map;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class IntegrationTests {
    private static final List<String> RESULTS = new ArrayList<>();
    private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("orecontrol");

    public static void run(MinecraftServer server) {
        if (Boolean.getBoolean("orecontrol.reopenTest")) { testReopenedWorld(server); return; }
        try {
            Files.deleteIfExists(Path.of("test-results.txt"));
            testConfig();
            testSettingsSave();
            testRegionReset();
            testRules(server.overworld());
            testOtherRules(server);
            testAnimalHook(server.overworld());
            testGeneration(server);
            testShutdownReset(server);
            RESULTS.add("PASS: all checks completed");
            Files.write(Path.of("test-results.txt"), RESULTS);
            System.out.println("ORECONTROL_INTEGRATION_PASS " + RESULTS);
        } catch (Throwable error) {
            RESULTS.add("FAIL: " + error);
            try { Files.write(Path.of("test-results.txt"), RESULTS); } catch (Exception ignored) {}
            throw new IllegalStateException("Ore Control integration test failed", error);
        } finally {
            server.halt(false);
        }
    }

    private static void testConfig() throws Exception {
        Path file = CONFIG.resolve("test.cfg");
        Files.writeString(file, "# original Forge format\n\"Minable\" {\n B:disableIRON=true\n}\n");
        Configs config = Configs.read(file);
        check(config.disabled("Minable", "IRON"), "legacy boolean parsing");
        check(!config.disabled("Minable", "GOLD"), "missing key defaults");
        check(Configs.read(file).disabled("Minable", "IRON"), "round-trip preserves enabled values");
        Files.writeString(file, "Minable {\n B:disableIRON=maybe\n}\n");
        boolean rejected = false;
        try { Configs.read(file); } catch (java.io.IOException expected) { rejected = true; }
        check(rejected, "invalid boolean rejected");
        Files.delete(file);
        RESULTS.add("PASS: original CFG parsing, defaults, round-trip and malformed-value checks");
    }

    private static void testSettingsSave() throws Exception {
        Path directory = Files.createTempDirectory(Path.of("."), "config-save-test-");
        Map<String, Boolean> master = new java.util.LinkedHashMap<>(Configs.master().settings());
        master.put("Generic.allowExistingChunkRebuild", true);
        master.put("Decorate.disableTREE", true);
        Configs.saveSettings(directory, master, null, Map.of());
        check(Configs.master().enabled("Generic", "allowExistingChunkRebuild"), "overwrite preference persisted");
        check(Configs.get("minecraft:overworld").disabled("Decorate", "TREE"), "live global settings applied");
        Configs.saveSettings(directory, master, "minecraft:overworld", Map.of("Decorate.disableGRASS", true));
        check(Configs.master().enabled("Generic", "enablePerDimensionConfigs"), "editing dimension enables overrides");
        check(Configs.get("minecraft:overworld").disabled("Decorate", "GRASS"), "live dimension settings applied");
        check(!Configs.get("minecraft:overworld").disabled("Decorate", "TREE"), "dimension fully overrides global");
        OreControl.reload();
        RESULTS.add("PASS: atomic live settings save, overwrite preference and dimension editing");
    }

    private static void testRegionReset() throws Exception {
        Path world = Files.createTempDirectory(Path.of("."), "reset-file-test-").toAbsolutePath();
        Path dimension = world.resolve("dimensions/minecraft/overworld");
        Map<Path, byte[]> originals = new java.util.LinkedHashMap<>();
        for (String folder : List.of("region", "entities", "poi")) {
            Path file = dimension.resolve(folder).resolve("r.-1.-1.mca");
            Files.createDirectories(file.getParent());
            byte[] content = new byte[16384];
            Arrays.fill(content, 8192, content.length, (byte) 77);
            Files.write(file, content);
            try (RandomAccessFile region = new RandomAccessFile(file.toFile(), "rw")) {
                region.seek(4L * (31 + 31 * 32)); region.writeInt(513);
                region.seek(4L * (30 + 31 * 32)); region.writeInt(769);
            }
            originals.put(file, Files.readAllBytes(file));
        }
        Path external = dimension.resolve("region/c.-1.-1.mcc");
        Files.writeString(external, "external chunk data");
        originals.put(external, Files.readAllBytes(external));
        Path untouched = dimension.resolve("region/r.0.0.mca");
        Files.write(untouched, new byte[8192]);
        Files.createDirectories(dimension.resolve("entities"));
        Files.write(dimension.resolve("entities/r.0.0.mca"), new byte[0]);
        var result = RegionReset.reset(world, dimension, new RegionReset.Selection(-1, -1, -1, -1));
        check(result.chunks() == 1 && result.backup() != null, "one negative-coordinate chunk selected");
        check(RegionReset.latestSelections(world).get("minecraft:overworld").contains(-1, -1), "rebuild boundary recorded");
        for (var original : originals.entrySet()) {
            Path backupFile = result.backup().resolve(world.relativize(original.getKey()));
            check(Arrays.equals(original.getValue(), Files.readAllBytes(backupFile)), "backup bytes match " + original.getKey());
            if (original.getKey().equals(external)) continue;
            check(!RegionReset.hasChunk(original.getKey(), -1, -1), "selected chunk cleared");
            check(RegionReset.hasChunk(original.getKey(), -2, -1), "neighbor retained");
            byte[] expected = original.getValue().clone();
            int offset = 4 * (31 + 31 * 32);
            Arrays.fill(expected, offset, offset + 4, (byte) 0);
            Arrays.fill(expected, 4096 + offset, 4100 + offset, (byte) 0);
            check(Arrays.equals(expected, Files.readAllBytes(original.getKey())), "only selected region headers changed");
        }
        check(!Files.exists(external), "selected external chunk removed after backup");
        check(Arrays.equals(Files.readAllBytes(untouched), new byte[8192]), "other region untouched");
        Path manifest = result.backup().resolve("manifest.json");
        var journal = JsonParser.parseString(Files.readString(manifest)).getAsJsonObject();
        journal.addProperty("state", "PREPARED");
        Files.writeString(manifest, journal.toString());
        RegionReset.recoverInterrupted(world);
        for (var original : originals.entrySet()) {
            check(Arrays.equals(original.getValue(), Files.readAllBytes(original.getKey())), "interrupted reset restored " + original.getKey());
        }
        check(RegionReset.reset(world, dimension, new RegionReset.Selection(4, 4, 4, 4)).chunks() == 0, "unbuilt selection is a no-op");
        boolean limited = false;
        try { new RegionReset.Selection(0, 0, 32, 32); } catch (IllegalArgumentException expected) { limited = true; }
        check(limited, "oversized selection rejected");
        RESULTS.add("PASS: selective terrain/entity/POI reset, negative coordinates, external chunks, exact backups, neighbor preservation and interrupted-operation recovery");
    }

    private static void testShutdownReset(MinecraftServer server) throws Exception {
        var world = server.getWorldPath(LevelResource.ROOT);
        var dimension = DimensionType.getStorageFolder(Level.OVERWORLD, world);
        server.overworld().setBlock(new BlockPos(640, 200, 640), Blocks.DIAMOND_BLOCK.defaultBlockState(), 3);
        server.overworld().setBlock(new BlockPos(656, 200, 640), Blocks.EMERALD_BLOCK.defaultBlockState(), 3);
        Files.writeString(CONFIG.resolve("orecontrol.cfg"), "Generic {\n B:allowExistingChunkRebuild=true\n}\nMinable {\n B:disableAllVanillaGeneration=true\n}\nDecorate {\n B:disableTREE=true\n B:disableGRASS=true\n}\n");
        OreControl.reload();
        WorldReset.queue(server, Level.OVERWORLD, new RegionReset.Selection(40, 40, 40, 40));
        Files.writeString(Path.of("shutdown-reset-target.txt"), dimension.resolve("region/r.1.1.mca").toAbsolutePath().toString());
        RESULTS.add("PASS: real loaded chunk queued for reset during server shutdown");
    }

    private static void testReopenedWorld(MinecraftServer server) {
        try {
            var level = server.overworld();
            var chunk = level.getChunk(40, 40);
            check(!chunk.getBlockState(new BlockPos(640, 200, 640)).is(Blocks.DIAMOND_BLOCK), "selected player construction removed after regeneration");
            check(level.getBlockState(new BlockPos(656, 200, 640)).is(Blocks.EMERALD_BLOCK), "neighbor construction preserved after regeneration");
            int ores = 0;
            var pos = new BlockPos.MutableBlockPos();
            for (int x = 640; x < 656; x++) for (int z = 640; z < 656; z++) {
                for (int y = level.getMinY(); y <= level.getMaxY(); y++) {
                    pos.set(x, y, z);
                    String block = BuiltInRegistries.BLOCK.getKey(chunk.getBlockState(pos).getBlock()).getPath();
                    if (block.endsWith("_ore") || block.equals("raw_iron_block") || block.equals("raw_copper_block")) ores++;
                }
            }
            check(ores == 0, "reopened chunk regenerated with the new ore rules");
            RESULTS.add("PASS: selected construction removed, old chunk regenerated with zero ores, neighboring construction preserved after reopening the world");
            Files.writeString(Path.of("reopen-test-results.txt"), String.join("\n", RESULTS) + "\nREGENERATION_PASSED\n");
        } catch (Throwable error) {
            try { Files.writeString(Path.of("reopen-test-results.txt"), RESULTS + "FAIL " + error); } catch (Exception ignored) {}
            throw new IllegalStateException(error);
        } finally { server.halt(false); }
    }

    private static void testRules(ServerLevel level) throws Exception {
        String[][] cases = {
            {"Minable", "COAL", "ore_coal"}, {"Minable", "DIAMOND", "ore_diamond_buried"},
            {"Minable", "DIRT", "ore_dirt"}, {"Minable", "GOLD", "ore_nether_gold"},
            {"Minable", "GRAVEL", "ore_gravel"}, {"Minable", "IRON", "ore_iron"},
            {"Minable", "LAPIS", "ore_lapis"}, {"Minable", "REDSTONE", "ore_redstone"},
            {"Minable", "QUARTZ", "ore_quartz"}, {"Minable", "DIORITE", "ore_diorite"},
            {"Minable", "GRANITE", "ore_granite"}, {"Minable", "ANDESITE", "ore_andesite"},
            {"Minable", "EMERALD", "ore_emerald"}, {"Minable", "SILVERFISH", "ore_infested"},
            {"Minable", "COPPER", "ore_copper_large"}, {"Minable", "ANCIENT_DEBRIS", "ore_ancient_debris_large"},
            {"Decorate", "BIG_SHROOM", "huge_brown_mushroom"}, {"Decorate", "CACTUS", "cactus"},
            {"Decorate", "CLAY", "disk_clay"}, {"Decorate", "DEAD_BUSH", "dead_bush"},
            {"Decorate", "DESERT_WELL", "desert_well"}, {"Decorate", "LILYPAD", "waterlily"},
            {"Decorate", "FLOWERS", "flower_default"}, {"Decorate", "FOSSIL", "fossil_coal"},
            {"Decorate", "GRASS", "grass"}, {"Decorate", "ICE", "ice_spike"},
            {"Decorate", "LAKE_WATER", "spring_water"}, {"Decorate", "LAKE_LAVA", "spring_lava_overworld"},
            {"Decorate", "PUMPKIN", "pumpkin"}, {"Decorate", "REED", "sugar_cane"},
            {"Decorate", "ROCK", "forest_rock"}, {"Decorate", "SAND", "disk_sand"},
            {"Decorate", "SAND_PASS2", "disk_gravel"}, {"Decorate", "SHROOM", "brown_mushroom"},
            {"Decorate", "TREE", "oak"}, {"Populate", "DUNGEON", "monster_room"},
            {"Populate", "FIRE", "patch_fire"}, {"Populate", "GLOWSTONE", "glowstone_extra"},
            {"Populate", "ICE", "freeze_top_layer"}, {"Populate", "LAVA", "lake_lava"},
            {"Populate", "NETHER_MAGMA", "ore_magma"}
        };
        Configs defaults = config("", "");
        for (var entry : level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).entrySet()) {
            check(!cancel(entry.getValue(), defaults, entry.getKey().identifier(), level), "defaults permit " + entry.getKey());
        }
        for (String[] test : cases) {
            var id = Identifier.withDefaultNamespace(test[2]);
            var feature = level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).getValue(id);
            check(feature != null, "registry contains " + id);
            check(cancel(feature, config(test[0], "disable" + test[1]), id, level), String.join("/", test));
        }
        var oreId = Identifier.withDefaultNamespace("ore_iron");
        var ore = level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).getValue(oreId);
        var custom = Identifier.fromNamespaceAndPath("test", "custom_ore");
        check(!cancel(ore, config("Minable", "disableAllVanillaGeneration"), custom, level), "all vanilla leaves custom ores enabled");
        check(cancel(ore, config("Minable", "disableCUSTOM"), custom, level), "custom ore switch");
        check(cancel(ore, config("Minable", "disableAllVanillaGeneration"), oreId, level), "all vanilla ore switch");
        check(!GenerationRules.shouldCancel(ore, level, BlockPos.ZERO), "manual /place and growth outside WorldGenRegion remain available");
        RESULTS.add("PASS: all vanilla configured features allowed by default, " + cases.length + " individual switch mappings, CUSTOM and global ore semantics");
    }

    private static Configs config(String category, String key) throws Exception {
        Path path = CONFIG.resolve("rule-test.cfg");
        Files.writeString(path, category.isEmpty() ? "" : category + " {\n B:" + key + "=true\n}\n");
        Configs result = Configs.read(path);
        Files.delete(path);
        return result;
    }

    private static void testOtherRules(MinecraftServer server) throws Exception {
        ServerLevel nether = server.getLevel(Level.NETHER);
        for (String[] test : new String[][] {{"NETHER_LAVA", "spring_nether_open"}, {"NETHER_LAVA2", "spring_nether_closed"}}) {
            Identifier id = Identifier.withDefaultNamespace(test[1]);
            var feature = nether.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).getValue(id);
            check(cancel(feature, config("Populate", "disable" + test[0]), id, nether), "nether spring " + test[0]);
            String other = test[0].equals("NETHER_LAVA") ? "NETHER_LAVA2" : "NETHER_LAVA";
            check(!cancel(feature, config("Populate", "disable" + other), id, nether), "nether spring switches independent");
        }
        var waterLake = new ConfiguredFeature<>(Feature.LAKE, new LakeFeature.Configuration(
            BlockStateProvider.simple(Blocks.WATER), BlockStateProvider.simple(Blocks.STONE)));
        check(cancel(waterLake, config("Populate", "disableLAKE"), Identifier.withDefaultNamespace("water_lake"), server.overworld()), "legacy water lake control on equivalent feature");
        var id = Identifier.withDefaultNamespace("oak");
        var tree = server.overworld().registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).getValue(id);
        for (String category : List.of("Decorate", "Populate")) {
            check(cancel(tree, config(category, "disableCUSTOM"), Identifier.fromNamespaceAndPath("test", "tree"), server.overworld()), category + " custom non-ore control");
        }
        var lavaId = Identifier.withDefaultNamespace("lake_lava");
        var lava = server.overworld().registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).getValue(lavaId);
        Configs surface = config("Custom", "disableSurfaceLavaLakes");
        var type = BuiltInRegistries.FEATURE.getKey(lava.feature());
        var level = server.overworld();
        check(GenerationRules.disabled(lava, surface, lavaId, type, level, new BlockPos(0, 200, 0)), "surface lava lake blocked");
        int undergroundY = Math.min(level.getSeaLevel() - 1, level.getHeight(Heightmap.Types.WORLD_SURFACE, 0, 0) - 9);
        check(!GenerationRules.disabled(lava, surface, lavaId, type, level, new BlockPos(0, undergroundY, 0)), "underground lava lake preserved");
        RESULTS.add("PASS: two independent nether spring controls, water lakes, both CUSTOM non-ore switches and surface/underground lava distinction");
    }

    private static boolean cancel(ConfiguredFeature<?, ?> feature, Configs config, Identifier id, ServerLevel level) {
        return GenerationRules.disabled(feature, config, id, BuiltInRegistries.FEATURE.getKey(feature.feature()), level, new BlockPos(0, 0, 0));
    }

    private static void testGeneration(MinecraftServer server) throws Exception {
        // Baseline and filtered chunks are generated in previously untouched, distant areas.
        Files.writeString(CONFIG.resolve("orecontrol.cfg"), "");
        OreControl.reload();
        int baseline = countOres(server.overworld(), 40, 40);
        check(baseline > 0, "baseline actually contains ores");
        Files.writeString(CONFIG.resolve("orecontrol.cfg"), "Generic {\n B:enablePerDimensionConfigs=true\n}\nMinable {\n B:disableAllVanillaGeneration=true\n}\nDecorate {\n B:disableTREE=true\n B:disableGRASS=true\n}\nPopulate {\n B:disableANIMALS=true\n}\n");
        Files.writeString(CONFIG.resolve("orecontrol_dim-1.cfg"), "");
        OreControl.reload();
        check(Configs.get("minecraft:overworld").enabled("Minable", "disableAllVanillaGeneration"), "master enabled");
        check(!Configs.get("minecraft:the_nether").enabled("Minable", "disableAllVanillaGeneration"), "legacy dimension file fully overrides master");
        int filtered = countOres(server.overworld(), 80, 80);
        check(filtered == 0, "disabled overworld has zero ores, found " + filtered);
        int vegetation = countVegetation(server.overworld(), 80, 80);
        check(vegetation == 0, "disabled overworld has zero tree/grass blocks, found " + vegetation);
        int nether = countOres(server.getLevel(Level.NETHER), 40, 40);
        check(nether > 0, "nether override permits ore generation");
        Path modern = CONFIG.resolve("dimensions/minecraft/the_nether.cfg");
        Files.createDirectories(modern.getParent());
        Files.writeString(modern, "Minable {\n B:disableAllVanillaGeneration=true\n}\n");
        OreControl.reload();
        int netherDisabled = countOres(server.getLevel(Level.NETHER), 80, 80);
        check(netherDisabled == 0, "namespaced dimension override disables nether ores");
        RESULTS.add("PASS: live chunk generation baseline=" + baseline + ", filtered=" + filtered
                + ", filtered tree/grass=" + vegetation + ", nether override=" + nether + ", nether disabled=" + netherDisabled);
        Files.delete(modern);
        Files.delete(CONFIG.resolve("orecontrol_dim-1.cfg"));
        Files.writeString(CONFIG.resolve("orecontrol.cfg"), "");
        OreControl.reload();
    }

    private static void testAnimalHook(ServerLevel level) throws Exception {
        var biome = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
        var random = new LegacyRandomSource(1L) {
            @Override public int next(int bits) { throw new AssertionError("original animal spawning body entered"); }
        };
        Files.writeString(CONFIG.resolve("orecontrol.cfg"), "Populate {\n B:disableANIMALS=true\n}\n");
        OreControl.reload();
        NaturalSpawner.spawnMobsForChunkGeneration(level, biome, new ChunkPos(0, 0), random);
        Files.writeString(CONFIG.resolve("orecontrol.cfg"), "");
        OreControl.reload();
        boolean originalRan = false;
        try { NaturalSpawner.spawnMobsForChunkGeneration(level, biome, new ChunkPos(0, 0), random); }
        catch (AssertionError expected) { originalRan = true; }
        check(originalRan, "animal spawning hook permits original body when enabled");
        RESULTS.add("PASS: actual initial-animal-spawning Mixin cancels only when configured");
    }

    private static int countOres(ServerLevel level, int startX, int startZ) {
        int count = 0;
        var pos = new BlockPos.MutableBlockPos();
        for (int cx = startX; cx < startX + 2; cx++) {
            for (int cz = startZ; cz < startZ + 2; cz++) {
                var chunk = level.getChunk(cx, cz);
                for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
                    for (int y = level.getMinY(); y <= level.getMaxY(); y++) {
                        pos.set(cx * 16 + x, y, cz * 16 + z);
                        String block = BuiltInRegistries.BLOCK.getKey(chunk.getBlockState(pos).getBlock()).getPath();
                        if (block.endsWith("_ore") || block.equals("ancient_debris") || block.equals("raw_iron_block") || block.equals("raw_copper_block")) count++;
                    }
                }
            }
        }
        return count;
    }

    private static int countVegetation(ServerLevel level, int startX, int startZ) {
        int count = 0;
        var pos = new BlockPos.MutableBlockPos();
        for (int cx = startX; cx < startX + 2; cx++) for (int cz = startZ; cz < startZ + 2; cz++) {
            var chunk = level.getChunk(cx, cz);
            for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
                for (int y = level.getMinY(); y <= level.getMaxY(); y++) {
                    pos.set(cx * 16 + x, y, cz * 16 + z);
                    String block = BuiltInRegistries.BLOCK.getKey(chunk.getBlockState(pos).getBlock()).getPath();
                    if (block.endsWith("_log") || block.endsWith("_leaves") || block.equals("short_grass")
                            || block.equals("tall_grass") || block.equals("fern") || block.equals("large_fern")
                            || block.equals("short_dry_grass") || block.equals("tall_dry_grass")) count++;
                }
            }
        }
        return count;
    }

    private static void check(boolean value, String description) {
        if (!value) throw new AssertionError(description);
    }
}
