package fi.dy.masa.orecontrol.mixin;

import fi.dy.masa.orecontrol.GenerationRules;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ConfiguredFeature.class)
abstract class ConfiguredFeatureMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void orecontrol$filter(WorldGenLevel level, ChunkGenerator generator, RandomSource random,
                                   BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (GenerationRules.shouldCancel((ConfiguredFeature<?, ?>) (Object) this, level, pos)) cir.setReturnValue(false);
    }
}
