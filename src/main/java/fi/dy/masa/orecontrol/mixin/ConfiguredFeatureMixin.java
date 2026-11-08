package fi.dy.masa.orecontrol.mixin;

import fi.dy.masa.orecontrol.GenerationRules;
import fi.dy.masa.orecontrol.FeatureWrites;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ConfiguredFeature.class)
abstract class ConfiguredFeatureMixin {
    @WrapMethod(method = "place")
    private boolean orecontrol$filter(WorldGenLevel level, ChunkGenerator generator, RandomSource random,
                                      BlockPos pos, Operation<Boolean> original) {
        if (GenerationRules.shouldCancel((ConfiguredFeature<?, ?>) (Object) this, level, pos)) return false;
        var previous = FeatureWrites.enter(level);
        try { return original.call(level, generator, random, pos); }
        finally { FeatureWrites.restore(previous); }
    }
}
