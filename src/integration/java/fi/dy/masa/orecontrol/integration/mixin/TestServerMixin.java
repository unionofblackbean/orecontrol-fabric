package fi.dy.masa.orecontrol.integration.mixin;

import fi.dy.masa.orecontrol.integration.IntegrationTests;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
abstract class TestServerMixin {
    @Unique private boolean orecontrol$tested;

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void orecontrol$test(BooleanSupplier time, CallbackInfo ci) {
        if (!orecontrol$tested) {
            orecontrol$tested = true;
            IntegrationTests.run((MinecraftServer) (Object) this);
        }
    }
}
