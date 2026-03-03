package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityPose;
import net.xmilon.himproveme.TutorialModClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityProneMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void himproveme$lockClientPronePose(CallbackInfo ci) {
        ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
        if (self.isMainPlayer() && TutorialModClient.isProne()) {
            self.setSwimming(true);
            self.setPose(EntityPose.SWIMMING);
            self.setSprinting(false);
        }
    }
}
