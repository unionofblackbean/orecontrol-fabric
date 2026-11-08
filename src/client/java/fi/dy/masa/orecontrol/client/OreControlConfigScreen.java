package fi.dy.masa.orecontrol.client;

import fi.dy.masa.orecontrol.config.Configs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class OreControlConfigScreen extends Screen {
    private static final List<String> CATEGORIES = List.of("Decorate", "Minable", "Populate", "Custom", "Generic");
    private final Screen parent;
    private final Map<String, Boolean> master = new LinkedHashMap<>(Configs.master().settings());
    private final Map<String, Boolean> dimensionSettings;
    private final ResourceKey<Level> dimension;
    private boolean dimensionTarget;
    private boolean dimensionDirty;
    private int category;
    private int page;
    private String status = "";

    public OreControlConfigScreen(Screen parent) {
        super(Component.translatable("orecontrol.title"));
        this.parent = parent;
        var level = Minecraft.getInstance().level;
        dimension = level == null ? null : level.dimension();
        dimensionSettings = dimension == null ? new LinkedHashMap<>() : new LinkedHashMap<>(Configs.get(dimension.identifier().toString()).settings());
        dimensionTarget = dimension != null && Configs.get(dimension.identifier().toString()) != Configs.master();
    }

    private Map<String, Boolean> draft() { return dimensionTarget ? dimensionSettings : master; }

    @Override
    protected void init() {
        int left = width / 2 - 150;
        Button scope = addRenderableWidget(Button.builder(Component.translatable(dimensionTarget ? "orecontrol.scope.dimension" : "orecontrol.scope.global"), b -> {
            dimensionTarget = !dimensionTarget;
            page = 0;
            rebuildWidgets();
        }).bounds(left, 29, 300, 20).build());
        scope.active = dimension != null && minecraft.getSingleplayerServer() != null;
        for (int i = 0; i < CATEGORIES.size(); i++) {
            int index = i;
            Button tab = addRenderableWidget(Button.builder(Component.translatable("orecontrol.category." + CATEGORIES.get(i)), b -> {
                category = index;
                page = 0;
                rebuildWidgets();
            }).bounds(left + i * 61, 53, 56, 20).build());
            tab.active = category != i;
        }
        String section = CATEGORIES.get(category);
        List<String> keys = new ArrayList<>(List.of(Configs.OPTIONS.get(section).split(" ")));
        keys.remove("allowExistingChunkRebuild");
        if (dimensionTarget && section.equals("Generic")) keys.clear();
        int rows = Math.max(1, (height - 188) / 24);
        int capacity = rows * 2;
        page = Math.min(page, Math.max(0, (keys.size() - 1) / capacity));
        for (int i = page * capacity; i < Math.min(keys.size(), (page + 1) * capacity); i++) {
            String key = keys.get(i);
            String full = section + "." + key;
            int slot = i - page * capacity;
            Button toggle = addRenderableWidget(Button.builder(optionLabel(section, key), b -> {
                draft().put(full, !draft().getOrDefault(full, false));
                if (dimensionTarget) dimensionDirty = true;
                b.setMessage(optionLabel(section, key));
            }).bounds(left + slot % 2 * 152, 80 + slot / 2 * 24, 148, 20).build());
            toggle.setTooltip(Tooltip.create(Component.literal(full)));
        }
        if (keys.size() > capacity) {
            Button previous = addRenderableWidget(Button.builder(Component.literal("◀"), b -> { page--; rebuildWidgets(); }).bounds(left, height - 102, 35, 18).build());
            previous.active = page > 0;
            Button next = addRenderableWidget(Button.builder(Component.literal("▶"), b -> { page++; rebuildWidgets(); }).bounds(left + 265, height - 102, 35, 18).build());
            next.active = (page + 1) * capacity < keys.size();
        }
        addRenderableWidget(Button.builder(overwriteLabel(), b -> {
            master.put("Generic.allowExistingChunkRebuild", !master.getOrDefault("Generic.allowExistingChunkRebuild", false));
            rebuildWidgets();
        }).bounds(left, height - 78, 180, 20).build());
        Button rebuild = addRenderableWidget(Button.builder(Component.translatable("orecontrol.rebuild.select"), b -> {
            if (save()) minecraft.setScreen(new RebuildChunksScreen(this));
        }).bounds(left + 184, height - 78, 116, 20).build());
        rebuild.active = master.getOrDefault("Generic.allowExistingChunkRebuild", false)
                && minecraft.getSingleplayerServer() != null && minecraft.level != null;
        addRenderableWidget(Button.builder(Component.translatable("orecontrol.save"), b -> { if (save()) onClose(); }).bounds(left, height - 27, 148, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose()).bounds(left + 152, height - 27, 148, 20).build());
    }

    private Component optionLabel(String section, String key) {
        boolean value = draft().getOrDefault(section + "." + key, false);
        Component state = Component.translatable((section.equals("Generic") || section.equals("Custom"))
                ? (value ? "orecontrol.on" : "orecontrol.off") : (value ? "orecontrol.disabled" : "orecontrol.allowed"));
        return Component.translatable("orecontrol.option", Component.translatable("orecontrol.setting." + key), state);
    }

    private Component overwriteLabel() {
        return Component.translatable(master.getOrDefault("Generic.allowExistingChunkRebuild", false)
                ? "orecontrol.overwrite.on" : "orecontrol.overwrite.off");
    }

    private boolean save() {
        try {
            Path directory = FabricLoader.getInstance().getConfigDir().resolve("orecontrol");
            Configs.saveSettings(directory, master,
                    dimensionDirty ? dimension.identifier().toString() : null, dimensionSettings);
            status = "";
            return true;
        } catch (Exception e) {
            status = e.getMessage() == null ? e.toString() : e.getMessage();
            return false;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float delta) {
        super.extractRenderState(graphics, x, y, delta);
        graphics.centeredText(font, title, width / 2, 12, 0xFFFFFFFF);
        graphics.centeredText(font, Component.translatable(minecraft.level != null && minecraft.getSingleplayerServer() == null
                ? "orecontrol.remote.help" : "orecontrol.help"), width / 2, height - 53, 0xFFAAAAAA);
        if (!status.isEmpty()) graphics.centeredText(font, status, width / 2, height - 40, 0xFFFF7777);
        if (dimensionTarget && CATEGORIES.get(category).equals("Generic")) {
            graphics.centeredText(font, Component.translatable("orecontrol.global_only"), width / 2, 90, 0xFFAAAAAA);
        }
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
}
