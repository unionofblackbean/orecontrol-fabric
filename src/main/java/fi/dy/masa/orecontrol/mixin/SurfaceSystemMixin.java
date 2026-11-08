package fi.dy.masa.orecontrol.mixin;

import fi.dy.masa.orecontrol.SurfaceControls;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SurfaceSystem.class)
abstract class SurfaceSystemMixin {
    @Inject(method = "frozenOceanExtension", at = @At("HEAD"), cancellable = true)
    private void orecontrol$frozenOcean(CallbackInfo ci) {
        var config = SurfaceControls.current();
        if (config != null && (config.disabled("Surface", "FROZEN_OCEAN") || config.disabled("Decorate", "ICEBERG"))) ci.cancel();
    }

    @Inject(method = "erodedBadlandsExtension", at = @At("HEAD"), cancellable = true)
    private void orecontrol$badlands(CallbackInfo ci) {
        var config = SurfaceControls.current();
        if (config != null && config.disabled("Surface", "BADLANDS_PILLARS")) ci.cancel();
    }
}
