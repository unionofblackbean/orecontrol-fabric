package fi.dy.masa.orecontrol.clienttest.mixin;

import fi.dy.masa.orecontrol.clienttest.ClientChecks;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class ClientTestMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void orecontrol$checkUi(CallbackInfo ci) { ClientChecks.tick((Minecraft) (Object) this); }
}
