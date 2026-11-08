package fi.dy.masa.orecontrol.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.orecontrol.GenerationRules;
import fi.dy.masa.orecontrol.config.Configs;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkGenerator.class)
abstract class ChunkGeneratorMixin {
    @WrapOperation(method = "applyBiomeDecoration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;placeWithBiomeCheck(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean orecontrol$filterStep(PlacedFeature feature, WorldGenLevel level, ChunkGenerator generator,
            RandomSource random, BlockPos position, Operation<Boolean> original, @Local(name = "stepIndex") int step) {
        Configs config = Configs.get(level.getLevel().dimension().identifier().toString());
        if (GenerationRules.stageDisabled(config, step)) return false;
        return original.call(feature, level, generator, random, position);
    }
}
