package fi.dy.masa.orecontrol.mixin;

import fi.dy.masa.orecontrol.OreControl;
import fi.dy.masa.orecontrol.reset.RegionReset;
import fi.dy.masa.orecontrol.reset.WorldReset;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.server.level.ServerLevel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerMixin {
    @Inject(method = "createLevels", at = @At("HEAD"))
    private void orecontrol$reload(CallbackInfo ci) {
        try {
            RegionReset.recoverInterrupted(((MinecraftServer) (Object) this).getWorldPath(LevelResource.ROOT));
            WorldReset.loadBoundaries((MinecraftServer) (Object) this);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Ore Control backup recovery failed; world loading stopped", e);
        }
        OreControl.reload();
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;close()V"))
    private void orecontrol$resetClosedRegions(CallbackInfo ci) {
        WorldReset.finishOnShutdown((MinecraftServer) (Object) this);
    }

    @WrapOperation(method = "stopServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;close()V"))
    private void orecontrol$checkClose(ServerLevel level, Operation<Void> original) throws Throwable {
        try { original.call(level); }
        catch (Throwable error) {
            WorldReset.markCloseFailed((MinecraftServer) (Object) this);
            throw error;
        }
    }
}
