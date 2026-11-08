package fi.dy.masa.orecontrol.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.orecontrol.GenerationRules;
import fi.dy.masa.orecontrol.StructureRules;
import fi.dy.masa.orecontrol.config.Configs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerator.class)
abstract class ChunkGeneratorMixin {
    @WrapOperation(method = "applyBiomeDecoration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;placeWithBiomeCheck(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean orecontrol$filterStep(PlacedFeature feature, WorldGenLevel level, ChunkGenerator generator,
            RandomSource random, BlockPos position, Operation<Boolean> original, @Local(name = "stepIndex") int step) {
        Configs config = Configs.get(level.getLevel().dimension().identifier().toString());
        if (GenerationRules.stageDisabled(config, step)) return false;
        return original.call(feature, level, generator, random, position);
    }

    @Inject(method = "tryGenerateStructure", at = @At("HEAD"), cancellable = true)
    private void orecontrol$preventStructureStart(StructureSet.StructureSelectionEntry selected, StructureManager manager,
            RegistryAccess registries, RandomState random, StructureTemplateManager templates, long seed, ChunkAccess chunk,
            ChunkPos position, SectionPos section, ResourceKey<Level> dimension, CallbackInfoReturnable<Boolean> cir) {
        var id = registries.lookupOrThrow(Registries.STRUCTURE).getKey(selected.structure().value());
        if (StructureRules.disabled(Configs.get(dimension.identifier().toString()), id)) cir.setReturnValue(false);
    }

    @WrapOperation(method = "createReferences", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/StructureStart;isValid()Z"))
    private boolean orecontrol$preventStructureReference(StructureStart start, Operation<Boolean> original,
                                                         @Local(argsOnly = true) WorldGenLevel level) {
        if (!original.call(start)) return false;
        var id = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(start.getStructure());
        return !StructureRules.disabled(Configs.get(level.getLevel().dimension().identifier().toString()), id);
    }

    @ModifyVariable(method = "findNearestMapStructure", at = @At("HEAD"), argsOnly = true)
    private HolderSet<Structure> orecontrol$filterStructureSearch(HolderSet<Structure> targets,
                                                               @Local(argsOnly = true) ServerLevel level) {
        var config = Configs.get(level.dimension().identifier().toString());
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var allowed = targets.stream().filter(holder -> !StructureRules.disabled(config, registry.getKey(holder.value()))).toList();
        return allowed.size() == targets.size() ? targets : HolderSet.direct(allowed);
    }
}
