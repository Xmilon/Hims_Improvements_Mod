package net.xmilon.himproveme.mixin;

import net.xmilon.himproveme.compat.SpearBackportCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.notunanancyowen.spears.fabric.SpearsFabric")
public abstract class SpearsFabricCompatMixin {
    @Inject(method = "onInitialize", at = @At("TAIL"))
    private void himproveme$registerEnderSpearAfterSpearsInit(CallbackInfo ci) {
        SpearBackportCompat.registerEnderSpearItem();
    }
}
