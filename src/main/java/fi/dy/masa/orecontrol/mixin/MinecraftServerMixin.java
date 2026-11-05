package fi.dy.masa.orecontrol.mixin;

import fi.dy.masa.orecontrol.OreControl;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerMixin {
    @Inject(method = "createLevels", at = @At("HEAD"))
    private void orecontrol$reload(CallbackInfo ci) {
        OreControl.reload();
    }
}
