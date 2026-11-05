package fi.dy.masa.orecontrol.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.orecontrol.config.Configs;
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
        boolean all = config.enabled("Minable", "disableAllVanillaGeneration");
        if ((state.is(Blocks.DEEPSLATE_IRON_ORE) || state.is(Blocks.RAW_IRON_BLOCK))
                && (all || config.disabled("Minable", "IRON"))) return null;
        if ((state.is(Blocks.COPPER_ORE) || state.is(Blocks.RAW_COPPER_BLOCK))
                && (all || config.disabled("Minable", "COPPER"))) return null;
        if (state.is(Blocks.GRANITE) && (all || config.disabled("Minable", "GRANITE"))) return null;
        if (state.is(Blocks.TUFF) && all) return null;
        return state;
    }
}
