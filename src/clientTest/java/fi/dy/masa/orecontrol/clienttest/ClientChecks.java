package fi.dy.masa.orecontrol.clienttest;

import fi.dy.masa.orecontrol.client.OreControlConfigScreen;
import com.terraformersmc.modmenu.ModMenu;
import fi.dy.masa.orecontrol.client.RebuildChunksScreen;
import fi.dy.masa.orecontrol.config.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientChecks {
    private static int stage;
    private static int wait;
    private static long started = System.nanoTime();
    private static final StringBuilder RESULTS = new StringBuilder();

    public static void tick(Minecraft client) {
        try {
            if (stage == 99) return;
            if ((System.nanoTime() - started) > 180_000_000_000L) throw new AssertionError("Client checks timed out at stage " + stage);
            if (wait-- > 0 || client.getOverlay() != null) return;
            switch (stage) {
                case 0 -> {
                    if (!(client.screen instanceof TitleScreen)) return;
                    client.createWorldOpenFlows().openWorld(Files.readString(Path.of("test-world-name.txt")).strip(), () -> {});
                    stage = 1;
                }
                case 1 -> {
                    if (client.player == null || client.getSingleplayerServer() == null) return;
                    check(ModMenu.hasConfigScreen("orecontrol"), "real Mod Menu registered the config entrypoint");
                    client.setScreen(ModMenu.getConfigScreen("orecontrol", new TitleScreen()));
                    wait = 25; stage = 2;
                }
                case 2 -> {
                    check(client.screen instanceof OreControlConfigScreen, "Mod Menu factory opens settings");
                    capture(client, "settings-zh.png");
                    press(client.screen, "▶");
                    press(client.screen, "树木与巨型菌");
                    check(!Configs.master().disabled("Decorate", "TREE"), "draft is not applied before save");
                    press(client.screen, "取消");
                    check(!Configs.master().disabled("Decorate", "TREE"), "cancel preserves config");
                    client.setScreen(new OreControlConfigScreen(new TitleScreen()));
                    press(client.screen, "旧区域");
                    press(client.screen, "保存并应用");
                    check(Configs.master().enabled("Generic", "allowExistingChunkRebuild"), "overwrite switch saved");
                    client.setScreen(new OreControlConfigScreen(new TitleScreen()));
                    press(client.screen, "编辑全局");
                    press(client.screen, "草和蕨");
                    press(client.screen, "保存并应用");
                    check(Configs.get(client.level.dimension().identifier().toString()).disabled("Decorate", "GRASS"), "dimension changes applied immediately");
                    client.setScreen(new OreControlConfigScreen(new TitleScreen()));
                    press(client.screen, "选择重建范围");
                    check(client.screen instanceof RebuildChunksScreen, "rebuild range screen opens");
                    wait = 25; stage = 3;
                }
                case 3 -> {
                    capture(client, "rebuild-range-zh.png");
                    press(client.screen, "备份并重建");
                    check(client.screen instanceof ConfirmScreen, "destructive action has explicit confirmation");
                    wait = 25; stage = 4;
                }
                case 4 -> {
                    capture(client, "rebuild-confirm-zh.png");
                    press(client.screen, "取消");
                    check(client.screen instanceof RebuildChunksScreen && client.level != null, "cancel confirmation keeps world open");
                    var fields = client.screen.children().stream().filter(c -> c instanceof EditBox).map(c -> (EditBox) c).toList();
                    fields.get(0).setValue("41"); fields.get(1).setValue("40"); fields.get(2).setValue("41"); fields.get(3).setValue("40");
                    press(client.screen, "备份并重建");
                    press(client.screen, "确认重建并退出");
                    stage = 5; wait = 35;
                }
                case 5 -> {
                    if (!(client.screen instanceof AlertScreen) || client.level != null) return;
                    check(client.screen.getTitle().getString().contains("区块已准备重建"), "shutdown reset finished successfully");
                    capture(client, "rebuild-result-zh.png");
                    RESULTS.append("ALL_CLIENT_CHECKS_PASSED\n");
                    Files.writeString(Path.of("client-test-results.txt"), RESULTS);
                    stage = 6; wait = 40;
                }
                case 6 -> { stage = 99; client.stop(); }
            }
        } catch (Throwable error) {
            try { Files.writeString(Path.of("client-test-results.txt"), RESULTS + "FAIL " + error); } catch (Exception ignored) {}
            error.printStackTrace();
            stage = 99;
            client.stop();
        }
    }

    private static void capture(Minecraft client, String name) {
        Screenshot.grab(client.gameDirectory, name, client.getMainRenderTarget(), 1, message -> {});
    }

    private static void press(Screen screen, String contains) {
        Button button = screen.children().stream().filter(child -> child instanceof Button)
                .map(child -> (Button) child).filter(b -> b.getMessage().getString().contains(contains)).findFirst()
                .orElseThrow(() -> new AssertionError("Missing button " + contains));
        check(button.active, "button active " + contains);
        button.onPress(null);
    }

    private static void check(boolean value, String description) {
        if (!value) throw new AssertionError(description);
        RESULTS.append("PASS ").append(description).append('\n');
    }
}
