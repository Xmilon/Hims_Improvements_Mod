package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.xmilon.himproveme.client.WardenPerkClientHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the stunned FOV clamp after vanilla finishes calculating the normal camera field of view.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererWardenEffectMixin {
    /**
     * Narrows the final FOV only when the synced local affliction state says stunned vision pressure is active.
     */
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void himproveme$applyStunnedFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(WardenPerkClientHelper.modifyFov(cir.getReturnValueD()));
    }
}
