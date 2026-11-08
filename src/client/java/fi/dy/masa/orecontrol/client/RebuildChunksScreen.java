package fi.dy.masa.orecontrol.client;

import fi.dy.masa.orecontrol.reset.RegionReset;
import fi.dy.masa.orecontrol.reset.WorldReset;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

public final class RebuildChunksScreen extends Screen {
    private final Screen parent;
    private final String[] coordinates = new String[4];
    private String error = "";
    private boolean busy;

    public RebuildChunksScreen(Screen parent) {
        super(Component.translatable("orecontrol.rebuild.title"));
        this.parent = parent;
        var player = Minecraft.getInstance().player;
        var chunk = player.chunkPosition();
        coordinates[0] = coordinates[2] = Integer.toString(chunk.x());
        coordinates[1] = coordinates[3] = Integer.toString(chunk.z());
    }

    @Override
    protected void init() {
        int left = width / 2 - 150;
        for (int i = 0; i < coordinates.length; i++) {
            int index = i;
            EditBox field = addRenderableWidget(new EditBox(font, left + i % 2 * 155, 77 + i / 2 * 42, 145, 20,
                    Component.translatable("orecontrol.coordinate." + i)));
            field.setMaxLength(9);
            field.setValue(coordinates[i]);
            field.setResponder(value -> coordinates[index] = value);
        }
        addRenderableWidget(Button.builder(Component.translatable("orecontrol.rebuild.confirm"), b -> confirm())
                .bounds(left, height - 28, 195, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                .bounds(left + 200, height - 28, 100, 20).build());
    }

    private RegionReset.Selection selection() {
        try {
            return new RegionReset.Selection(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]),
                    Integer.parseInt(coordinates[2]), Integer.parseInt(coordinates[3]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("请填写完整的区块整数坐标");
        }
    }

    private void confirm() {
        if (busy) return;
        try {
            var selected = selection();
            var server = minecraft.getSingleplayerServer();
            if (server == null || minecraft.level == null) throw new IllegalStateException("仅支持单人世界或本机联机的主机");
            var dimension = minecraft.level.dimension();
            Component message = Component.translatable("orecontrol.rebuild.warning", selected.minX(), selected.minZ(),
                    selected.maxX(), selected.maxZ(), selected.count(), dimension.identifier().toString());
            minecraft.setScreen(new ConfirmScreen(accepted -> {
                if (!accepted) { minecraft.setScreen(this); return; }
                busy = true;
                server.execute(() -> {
                    try {
                        var result = WorldReset.queue(server, dimension, selected);
                        minecraft.execute(() -> {
                            minecraft.disconnectFromWorld(Component.translatable("orecontrol.rebuild.exiting"));
                            WorldReset.cancelIncomplete(server);
                            try {
                                var completed = result.join();
                                String backup = completed.backup() == null ? "—" : completed.backup().toString();
                                minecraft.setScreen(new AlertScreen(() -> minecraft.setScreen(new TitleScreen()),
                                        Component.translatable("orecontrol.rebuild.done"),
                                        Component.translatable("orecontrol.rebuild.result", completed.chunks(), backup)));
                            } catch (Exception e) {
                                minecraft.setScreen(new AlertScreen(() -> minecraft.setScreen(new TitleScreen()),
                                        Component.translatable("orecontrol.rebuild.failed"), Component.literal(e.toString())));
                            }
                        });
                    } catch (Exception e) {
                        minecraft.execute(() -> { busy = false; error = e.getMessage(); minecraft.setScreen(this); });
                    }
                });
            }, Component.translatable("orecontrol.rebuild.title"), message,
                    Component.translatable("orecontrol.rebuild.execute"), Component.translatable("gui.cancel")));
        } catch (Exception e) {
            error = e.getMessage();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float delta) {
        super.extractRenderState(graphics, x, y, delta);
        graphics.centeredText(font, title, width / 2, 15, 0xFFFFFFFF);
        graphics.centeredText(font, Component.translatable("orecontrol.rebuild.coordinates"), width / 2, 35, 0xFFAAAAAA);
        int left = width / 2 - 150;
        for (int i = 0; i < 4; i++) {
            graphics.text(font, Component.translatable("orecontrol.coordinate." + i), left + i % 2 * 155, 65 + i / 2 * 42, 0xFFFFFFFF);
        }
        try {
            var selected = selection();
            graphics.centeredText(font, Component.translatable("orecontrol.rebuild.count", selected.count()), width / 2, 146, 0xFFFFFF55);
        } catch (IllegalArgumentException ignored) {}
        graphics.centeredText(font, Component.translatable("orecontrol.rebuild.limit"), width / 2, 162, 0xFFAAAAAA);
        if (!error.isEmpty()) graphics.centeredText(font, error, width / 2, height - 43, 0xFFFF7777);
    }

    @Override public void onClose() { if (!busy) minecraft.setScreen(parent); }
}
