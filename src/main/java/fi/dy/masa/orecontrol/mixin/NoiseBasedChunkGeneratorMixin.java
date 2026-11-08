package fi.dy.masa.orecontrol.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.orecontrol.config.Configs;
import fi.dy.masa.orecontrol.SurfaceControls;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseBasedChunkGenerator.class)
abstract class NoiseBasedChunkGeneratorMixin {
    @WrapOperation(method = "doFill", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/NoiseChunk;getInterpolatedState()Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState orecontrol$veins(NoiseChunk noise, Operation<BlockState> original,
                                        @Local(argsOnly = true) StructureManager structures) {
        BlockState state = original.call(noise);
        if (state == null) return null;
        var level = ((StructureManagerAccessor) structures).orecontrol$level();
        if (!(level instanceof ServerLevelAccessor server)) return state;
        Configs config = Configs.get(server.getLevel().dimension().identifier().toString());
        boolean all = config.enabled("Minable", "disableAllVanillaGeneration")
                || config.disabled("Stages", "ALL") || config.disabled("Stages", "UNDERGROUND_ORES");
        if ((state.is(Blocks.DEEPSLATE_IRON_ORE) || state.is(Blocks.RAW_IRON_BLOCK))
                && (all || config.disabled("Minable", "IRON"))) return null;
        if ((state.is(Blocks.COPPER_ORE) || state.is(Blocks.RAW_COPPER_BLOCK))
                && (all || config.disabled("Minable", "COPPER"))) return null;
        if (state.is(Blocks.GRANITE) && (all || config.disabled("Minable", "GRANITE"))) return null;
        if (state.is(Blocks.TUFF) && (all || config.disabled("Minable", "TUFF"))) return null;
        return state;
    }

    @WrapMethod(method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V")
    private void orecontrol$surface(WorldGenRegion region, StructureManager structures, RandomState random,
                                    ChunkAccess chunk, Operation<Void> original) {
        Configs config = Configs.get(region.getLevel().dimension().identifier().toString());
        Configs previous = SurfaceControls.enter(config);
        try {
            original.call(region, structures, random, chunk);
            if (!config.disabled("Surface", "DIRT")) return;
            var base = ((NoiseBasedChunkGenerator) (Object) this).generatorSettings().value().defaultBlock();
            var position = new BlockPos.MutableBlockPos();
            for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
                int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                for (int y = chunk.getMinY(); y <= top; y++) {
                    position.set(chunk.getPos().getMinBlockX() + x, y, chunk.getPos().getMinBlockZ() + z);
                    if (SurfaceControls.isDirt(chunk.getBlockState(position))) chunk.setBlockState(position, base);
                }
            }
        } finally { SurfaceControls.restore(previous); }
    }
}
