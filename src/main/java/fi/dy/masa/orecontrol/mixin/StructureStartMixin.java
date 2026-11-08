package fi.dy.masa.orecontrol.mixin;

import fi.dy.masa.orecontrol.StructureRules;
import fi.dy.masa.orecontrol.config.Configs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureStart.class)
abstract class StructureStartMixin {
    @Inject(method = "placeInChunk", at = @At("HEAD"), cancellable = true)
    private void orecontrol$preventStructurePieces(WorldGenLevel level, StructureManager manager,
            ChunkGenerator generator, RandomSource random, BoundingBox bounds, ChunkPos position, CallbackInfo ci) {
        if (!(level instanceof WorldGenRegion)) return;
        var start = (StructureStart) (Object) this;
        if (!start.isValid()) return;
        var id = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(start.getStructure());
        if (StructureRules.disabled(Configs.get(level.getLevel().dimension().identifier().toString()), id)) ci.cancel();
    }
}
