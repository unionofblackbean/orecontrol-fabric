package fi.dy.masa.orecontrol.mixin;

import fi.dy.masa.orecontrol.reset.WorldReset;
import fi.dy.masa.orecontrol.FeatureWrites;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
abstract class WorldGenRegionMixin {
    @Inject(method = "ensureCanWrite", at = @At("HEAD"), cancellable = true)
    private void orecontrol$preserveOutsideSelection(BlockPos position, CallbackInfoReturnable<Boolean> cir) {
        if (!WorldReset.canWrite((WorldGenRegion) (Object) this, position)) cir.setReturnValue(false);
    }

    @Inject(method = "setBlock", at = @At("HEAD"), cancellable = true)
    private void orecontrol$filterAttachedVegetation(BlockPos position, BlockState state, int flags, int limit,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (FeatureWrites.blocked((WorldGenRegion) (Object) this, state)) cir.setReturnValue(false);
    }
}
