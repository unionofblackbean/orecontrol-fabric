package fi.dy.masa.orecontrol.integration;

import fi.dy.masa.orecontrol.StructureRules;
import fi.dy.masa.orecontrol.config.Configs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class StructureChecks {
    private static final Path DIRECTORY = FabricLoader.getInstance().getConfigDir().resolve("orecontrol");
    private StructureChecks() {}

    public static void run(MinecraftServer server) {
        List<String> results = new ArrayList<>();
        try {
            audit(server, results);
            for (String[] fixture : List.of(new String[] {"village", "village_plains", "VILLAGE"},
                    new String[] {"mineshaft", "mineshaft", "MINESHAFT"}, new String[] {"trial", "trial_chambers", "TRIAL_CHAMBERS"})) {
                testGeneration(server, fixture, results);
            }
            profile(Map.of());
            results.add("ALL_STRUCTURE_CHECKS_PASSED");
            Files.write(Path.of("structure-test-results.txt"), results);
            System.out.println("ORECONTROL_STRUCTURE_TESTS " + results);
        } catch (Throwable error) {
            results.add("FAIL " + error);
            try { Files.write(Path.of("structure-test-results.txt"), results); } catch (Exception ignored) {}
            throw new IllegalStateException(error);
        } finally { server.halt(false); }
    }

    private static void audit(MinecraftServer server, List<String> results) throws Exception {
        Path file = DIRECTORY.resolve("structure-rule-test.cfg");
        Configs.write(file, Map.of());
        Configs defaults = Configs.read(file);
        Configs.write(file, Map.of("Structures.disableALL", true));
        Configs all = Configs.read(file);
        List<String> report = new ArrayList<>(List.of("# Minecraft 26.1.2 大型结构覆盖审计", "",
                "从实际结构注册表逐项读取并测试，含每组开关、总开关、维度覆盖及实际区块生成验证。", "",
                "| 原版结构 ID | 配置键 |", "| --- | --- |"));
        var registry = server.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var entries = registry.keySet().stream().filter(id -> id.getNamespace().equals("minecraft")).sorted().toList();
        for (Identifier id : entries) {
            String group = StructureRules.group(id);
            check(List.of(Configs.OPTIONS.get("Structures").split(" ")).contains("disable" + group), "structure has a UI switch " + id);
            check(!StructureRules.disabled(defaults, id), "defaults allow " + id);
            check(StructureRules.disabled(all, id), "all blocks " + id);
            Configs.write(file, Map.of("Structures.disable" + group, true));
            Configs selected = Configs.read(file);
            for (Identifier other : entries) {
                check(StructureRules.disabled(selected, other) == StructureRules.group(other).equals(group), "independent structure group " + id + " / " + other);
            }
            report.add("| `" + id + "` | `Structures.disable" + group + "` |");
        }
        Configs.write(file, Map.of("Structures.disableCUSTOM", true));
        check(StructureRules.disabled(Configs.read(file), Identifier.fromNamespaceAndPath("example", "structure")), "custom namespace");
        Files.delete(file);
        Files.write(Path.of("structure-audit.md"), report);
        results.add("PASS: audited all " + entries.size() + " vanilla structure IDs, grouped variants, default allows, independent switches, ALL and CUSTOM");
    }

    private static void testGeneration(MinecraftServer server, String[] fixture, List<String> results) throws Exception {
        var level = server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("fixtures", fixture[0])));
        check(level != null, "structure fixture loaded " + fixture[0]);
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var holder = registry.getOrThrow(ResourceKey.create(Registries.STRUCTURE, Identifier.withDefaultNamespace(fixture[1])));
        Structure structure = holder.value();
        var placement = (RandomSpreadStructurePlacement) level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET)
                .getValue(Identifier.fromNamespaceAndPath("fixtures", fixture[0])).placement();
        profile(Map.of());
        ChunkPos baselinePosition = placement.getPotentialStructureChunk(level.getSeed(), 128, 128);
        var baseline = level.getChunk(baselinePosition.x(), baselinePosition.z());
        var start = baseline.getStartForStructure(structure);
        check(start != null && start.isValid() && !start.getPieces().isEmpty(), "baseline structure and pieces " + fixture[1]);
        profile(Map.of("Structures.disable" + fixture[2], true));
        ChunkPos blockedPosition = placement.getPotentialStructureChunk(level.getSeed(), 256, 256);
        var blocked = level.getChunk(blockedPosition.x(), blockedPosition.z());
        var blockedStart = blocked.getStartForStructure(structure);
        check(blockedStart == null || !blockedStart.isValid(), "disabled structure start " + fixture[1]);
        check(blocked.getReferencesForStructure(structure).isEmpty(), "disabled structure references " + fixture[1]);
        var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(holder),
                baselinePosition.getWorldPosition(), 1, false);
        check(found == null, "disabled structure search excludes existing and phantom destinations " + fixture[1]);
        // An unavailable region cache makes any attempted placement fail, including reads before block writes.
        start.placeInChunk(new WorldGenRegion(level, null, null, baseline), level.structureManager(),
                level.getChunkSource().getGenerator(), RandomSource.create(1), start.getBoundingBox(), baselinePosition);
        var bounds = start.getBoundingBox();
        int edgeX = Math.floorDiv(bounds.maxX(), 16);
        int edgeZ = Math.floorDiv(bounds.maxZ(), 16);
        if (Math.max(Math.abs(edgeX - baselinePosition.x()), Math.abs(edgeZ - baselinePosition.z())) > 2) {
            var edge = level.getChunk(edgeX, edgeZ);
            // Initial chunk loading may already have reached this neighbor's reference stage.
            edge.getReferencesForStructure(structure).clear();
            level.getChunkSource().getGenerator().createReferences(level, level.structureManager(), edge);
            check(edge.getReferencesForStructure(structure).isEmpty(), "disabled existing neighbor does not add references " + fixture[1]);
        }
        var settings = Configs.master().settings();
        Configs.saveSettings(DIRECTORY, settings, level.dimension().identifier().toString(), Map.of());
        ChunkPos overridePosition = placement.getPotentialStructureChunk(level.getSeed(), 384, 384);
        var override = level.getChunk(overridePosition.x(), overridePosition.z()).getStartForStructure(structure);
        check(override != null && override.isValid(), "dimension override allows structure despite global deny " + fixture[1]);
        profile(Map.of("Structures.disableALL", true));
        ChunkPos allPosition = placement.getPotentialStructureChunk(level.getSeed(), 512, 512);
        check(level.getChunk(allPosition.x(), allPosition.z()).getAllStarts().values().stream().noneMatch(s -> s.isValid()), "all switch prevents all fixture starts");
        results.add("PASS: " + fixture[1] + " baseline pieces=" + start.getPieces().size() + ", disabled starts=0, references=0, existing pieces blocked, locate excluded, dimension override works, ALL blocked");
    }

    private static void profile(Map<String, Boolean> values) throws Exception {
        Configs.write(DIRECTORY.resolve("orecontrol.cfg"), values);
        Configs.load(DIRECTORY);
    }

    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
