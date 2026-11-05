package fi.dy.masa.orecontrol.mixin;

import fi.dy.masa.orecontrol.config.Configs;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.class)
abstract class NaturalSpawnerMixin {
    @Inject(method = "spawnMobsForChunkGeneration", at = @At("HEAD"), cancellable = true)
    private static void orecontrol$animals(ServerLevelAccessor level, Holder<Biome> biome, ChunkPos pos,
                                           RandomSource random, CallbackInfo ci) {
        if (Configs.get(level.getLevel().dimension().identifier().toString()).disabled("Populate", "ANIMALS")) ci.cancel();
    }
}
