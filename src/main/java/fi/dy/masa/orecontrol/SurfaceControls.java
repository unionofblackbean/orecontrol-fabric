package fi.dy.masa.orecontrol;

import fi.dy.masa.orecontrol.config.Configs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class SurfaceControls {
    private static final ThreadLocal<Configs> CURRENT = new ThreadLocal<>();
    private SurfaceControls() {}
    public static Configs current() { return CURRENT.get(); }
    public static Configs enter(Configs config) {
        Configs previous = CURRENT.get();
        CURRENT.set(config);
        return previous;
    }
    public static void restore(Configs previous) {
        if (previous == null) CURRENT.remove();
        else CURRENT.set(previous);
    }
    public static boolean isDirt(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM) || state.is(Blocks.MUD);
    }
}
