package fi.dy.masa.orecontrol.integration;

import fi.dy.masa.orecontrol.GenerationRules;
import fi.dy.masa.orecontrol.config.Configs;
import fi.dy.masa.orecontrol.SurfaceControls;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public final class ModernGenerationChecks {
    private static final Path DIRECTORY = FabricLoader.getInstance().getConfigDir().resolve("orecontrol");
    private ModernGenerationChecks() {}

    public static List<String> run(MinecraftServer server) throws Exception {
        List<String> results = new ArrayList<>();
        auditRegistry(server.overworld(), results);
        testStages(results);
        profile(Map.of());
        ServerLevel jungle = fixture(server, "jungle");
        ServerLevel ocean = fixture(server, "ocean");
        ServerLevel coral = fixture(server, "coral");
        ServerLevel forest = fixture(server, "forest");
        Map<String, Integer> jungleBaseline = count(jungle, 10, 10);
        Map<String, Integer> oceanBaseline = count(ocean, 10, 10);
        Map<String, Integer> coralBaseline = count(coral, 10, 10);
        Map<String, Integer> forestBaseline = count(forest, 10, 10);
        check(jungleBaseline.get("bamboo") > 0, "bamboo baseline must be present");
        check(oceanBaseline.get("seagrass") > 0 && oceanBaseline.get("kelp") > 0, "both aquatic baseline plants must be present");
        check(coralBaseline.get("coral") > 0 && coralBaseline.get("pickle") > 0, "coral and pickle baselines must be present");
        check(forestBaseline.get("litter") > 0 && forestBaseline.get("grass") > 0 && forestBaseline.get("moss") > 0, "tree litter and mixed moss/grass baselines");
        check(forestBaseline.get("vines") > 0, "tree vine baseline " + forestBaseline);
        profile(Map.of("Decorate.disableBAMBOO", true, "Decorate.disableSEAGRASS", true,
                "Decorate.disableKELP", true, "Decorate.disableCORAL", true, "Decorate.disableSEA_PICKLE", true));
        Map<String, Integer> jungleBlocked = count(jungle, 30, 30);
        Map<String, Integer> oceanBlocked = count(ocean, 30, 30);
        Map<String, Integer> coralBlocked = count(coral, 30, 30);
        check(jungleBlocked.get("bamboo") == 0, "individual bamboo control");
        check(oceanBlocked.get("seagrass") == 0 && oceanBlocked.get("kelp") == 0, "individual seagrass and kelp controls");
        check(coralBlocked.get("coral") == 0 && coralBlocked.get("pickle") == 0, "individual coral and pickle controls, including inline features");
        profile(Map.of("Decorate.disableLEAF_LITTER", true, "Decorate.disableGRASS", true, "Decorate.disableVINES", true));
        Map<String, Integer> attachedForest = count(forest, 30, 30);
        check(attachedForest.get("litter") == 0 && attachedForest.get("grass") == 0 && attachedForest.get("moss") > 0, "tree-attached litter and grass inside mixed moss feature suppressed independently");
        check(attachedForest.get("vines") == 0, "tree-attached vines suppressed");
        profile(Map.of("Stages.disableVEGETAL_DECORATION", true));
        Map<String, Integer> stageJungle = count(jungle, 50, 50);
        Map<String, Integer> stageOcean = count(ocean, 50, 50);
        Map<String, Integer> stageCoral = count(coral, 50, 50);
        check(stageJungle.get("bamboo") == 0 && stageOcean.get("seagrass") == 0 && stageOcean.get("kelp") == 0
                && stageCoral.get("coral") == 0 && stageCoral.get("pickle") == 0, "vegetal stage blocks all the tested vegetation");
        profile(Map.of("Minable.disableDIRT", true));
        Map<String, Integer> surfaceBaseline = count(server.overworld(), 200, 200);
        check(surfaceBaseline.get("dirt") > 0, "underground dirt control must not remove the surface layer");
        profile(Map.of("Surface.disableDIRT", true, "Stages.disableALL", true));
        Map<String, Integer> surfaceBlocked = count(server.overworld(), 220, 220);
        check(surfaceBlocked.get("dirt") == 0 && surfaceBlocked.get("ore") == 0, "surface dirt replacement and global feature/vein suppression");
        profile(Map.of());
        results.add("PASS: live bamboo baseline=" + jungleBaseline.get("bamboo") + ", blocked=" + jungleBlocked.get("bamboo"));
        results.add("PASS: live ocean plants baseline=" + oceanBaseline + ", blocked=" + oceanBlocked);
        results.add("PASS: live coral baseline=" + coralBaseline + ", blocked=" + coralBlocked);
        results.add("PASS: tree decorators and mixed-provider vegetation baseline=" + forestBaseline + ", blocked=" + attachedForest);
        results.add("PASS: actual vegetal-stage suppression and surface dirt baseline=" + surfaceBaseline.get("dirt") + ", blocked=" + surfaceBlocked.get("dirt"));
        return results;
    }

    private static void auditRegistry(ServerLevel level, List<String> results) throws Exception {
        var registry = level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE);
        Map<String, Configs> switches = new LinkedHashMap<>();
        Map<String, Boolean> all = new LinkedHashMap<>();
        Path file = DIRECTORY.resolve("audit.cfg");
        for (var category : Configs.OPTIONS.entrySet()) {
            if (!List.of("Minable", "Decorate", "Populate").contains(category.getKey())) continue;
            for (String key : category.getValue().split(" ")) {
                String name = category.getKey() + "." + key;
                Configs.write(file, Map.of(name, true));
                switches.put(name, Configs.read(file));
                all.put(name, true);
            }
        }
        Configs.write(file, all);
        Configs complete = Configs.read(file);
        Configs.write(file, Map.of());
        Configs defaults = Configs.read(file);
        Map<String, TreeSet<String>> naturalStages = new LinkedHashMap<>();
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        for (var biome : biomes.entrySet()) {
            if (!biome.getKey().identifier().getNamespace().equals("minecraft")) continue;
            var steps = biome.getValue().getGenerationSettings().features();
            for (int step = 0; step < steps.size(); step++) {
                String stage = GenerationStep.Decoration.values()[step].name();
                for (var placed : steps.get(step)) placed.value().getFeatures().forEach(holder -> {
                    Identifier key = registry.getKey(holder.value());
                    if (key != null) naturalStages.computeIfAbsent(key.toString(), ignored -> new TreeSet<>()).add(stage);
                });
            }
        }
        List<String> missing = new ArrayList<>();
        List<String> report = new ArrayList<>(List.of("# Minecraft 26.1.2 地物覆盖审计", "",
                "从游戏实际 Registry 与原版生物群系的 features 列表读取，逐项执行开关判定。`Stages` 总开关未用于掩盖单项映射遗漏。", "",
                "| 配置地物 | 类型 | 对应开关 | 原版自然装饰阶段 |", "| --- | --- | --- | --- |"));
        var entries = registry.entrySet().stream().filter(e -> e.getKey().identifier().getNamespace().equals("minecraft"))
                .sorted(java.util.Comparator.comparing(e -> e.getKey().identifier().toString())).toList();
        for (var entry : entries) {
            var feature = entry.getValue();
            String id = entry.getKey().identifier().toString();
            check(!disabled(feature, defaults, registry.getKey(feature), level), "default generation preserved " + id);
            List<String> controls = new ArrayList<>();
            for (var setting : switches.entrySet()) {
                if (disabled(feature, setting.getValue(), registry.getKey(feature), level)) controls.add(setting.getKey());
            }
            boolean covered = fullyBlocked(feature, complete, level);
            if (!covered && naturalStages.containsKey(id)) missing.add(id);
            String description = !controls.isEmpty() ? String.join(" / ", controls)
                    : covered ? "组合入口，子地物分别受控" : "特殊入口，不在原版生物群系自然装饰表中";
            report.add("| `" + id + "` | `" + BuiltInRegistries.FEATURE.getKey(feature.feature()) + "` | "
                    + description + " | " + String.join(", ", naturalStages.getOrDefault(id, new TreeSet<>())) + " |");
        }
        check(missing.isEmpty(), "uncontrolled natural configured features " + missing);
        int placedCount = (int) level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE).keySet().stream().filter(id -> id.getNamespace().equals("minecraft")).count();
        report.add(2, "核查原版命名空间的 " + entries.size() + " 个配置地物、" + placedCount + " 个放置地物；自然装饰引用的命名配置地物均可由单项或组合控制关闭。");
        report.addAll(List.of("", "阶段开关控制 PlacedFeature，包含匿名内联地物。大型 Structure、基础地形密度与天然海水不属于这套地物开关。",
                "地表泥土来自独立的表层生成，新增 Surface.disableDIRT；地下泥土团块仍由 Minable.disableDIRT 控制。",
                "树木装饰器附带的藤蔓、落叶，以及苔藓混合提供器中的草，通过自然地物作用域内的最终方块写入控制补充覆盖。",
                "出生奖励箱、骨粉/树苗、指令放置与末影龙重生等 ServerLevel 直接入口不按首次自然装饰拦截。测试夹具复用原版地物，另增加珊瑚/海泡菜尝试次数以建立稳定阳性对照。"));
        Files.write(Path.of("worldgen-audit.md"), report);
        Files.delete(file);
        results.add("PASS: audited " + entries.size() + " vanilla configured and " + placedCount + " placed features; no uncontrolled natural entries; defaults unchanged");
    }

    private static boolean fullyBlocked(ConfiguredFeature<?, ?> feature, Configs config, ServerLevel level) {
        var id = level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).getKey(feature);
        if (disabled(feature, config, id, level)) return true;
        var children = feature.getSubFeatures().toList();
        return !children.isEmpty() && children.stream().allMatch(child -> fullyBlocked(child.value(), config, level));
    }

    private static boolean disabled(ConfiguredFeature<?, ?> feature, Configs config, Identifier id, ServerLevel level) {
        return GenerationRules.disabled(feature, config, id, BuiltInRegistries.FEATURE.getKey(feature.feature()), level, BlockPos.ZERO);
    }

    private static void testStages(List<String> results) throws Exception {
        Path file = DIRECTORY.resolve("stage-test.cfg");
        var stages = GenerationStep.Decoration.values();
        for (int selected = 0; selected < stages.length; selected++) {
            Configs.write(file, Map.of("Stages.disable" + stages[selected].name(), true));
            Configs config = Configs.read(file);
            for (int step = 0; step < stages.length; step++) check(GenerationRules.stageDisabled(config, step) == (step == selected), "independent stage " + step);
        }
        Files.delete(file);
        results.add("PASS: all " + stages.length + " modern decoration-stage switches are independent");
    }

    private static ServerLevel fixture(MinecraftServer server, String name) {
        var level = server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("fixtures", name)));
        check(level != null, "fixture dimension loaded " + name);
        return level;
    }

    private static void profile(Map<String, Boolean> settings) throws Exception {
        Configs.write(DIRECTORY.resolve("orecontrol.cfg"), settings);
        Configs.load(DIRECTORY);
    }

    private static Map<String, Integer> count(ServerLevel level, int startX, int startZ) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String key : List.of("bamboo", "seagrass", "kelp", "coral", "pickle", "dirt", "ore", "grass", "litter", "vines", "moss")) counts.put(key, 0);
        var position = new BlockPos.MutableBlockPos();
        for (int cx = startX; cx < startX + 2; cx++) for (int cz = startZ; cz < startZ + 2; cz++) {
            var chunk = level.getChunk(cx, cz);
            for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = level.getMinY(); y <= level.getMaxY(); y++) {
                position.set(cx * 16 + x, y, cz * 16 + z);
                var state = chunk.getBlockState(position);
                String block = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
                String key = block.equals("bamboo") ? "bamboo" : block.endsWith("seagrass") ? "seagrass"
                        : block.startsWith("kelp") ? "kelp" : block.contains("coral") ? "coral"
                        : block.equals("sea_pickle") ? "pickle" : block.equals("leaf_litter") ? "litter" : block.equals("vine") ? "vines"
                        : List.of("short_grass", "tall_grass", "fern", "large_fern", "short_dry_grass", "tall_dry_grass").contains(block) ? "grass"
                        : block.contains("moss") ? "moss" : SurfaceControls.isDirt(state) ? "dirt"
                        : block.endsWith("_ore") || block.equals("raw_iron_block") || block.equals("raw_copper_block") ? "ore" : null;
                if (key != null) counts.merge(key, 1, Integer::sum);
            }
        }
        return counts;
    }

    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
