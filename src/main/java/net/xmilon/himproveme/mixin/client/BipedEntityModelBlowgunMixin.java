package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.xmilon.himproveme.item.custom.BlowgunItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BipedEntityModel.class, priority = 1500)
public abstract class BipedEntityModelBlowgunMixin<T extends LivingEntity> extends AnimalModel<T> {
    @Shadow
    @Final
    public ModelPart body;

    @Shadow
    @Final
    public ModelPart head;

    @Shadow
    protected abstract ModelPart getArm(Arm arm);

    @Inject(method = "positionRightArm", at = @At("HEAD"), cancellable = true)
    private void himproveme$applyRightBlowgunPose(T entity, CallbackInfo ci) {
        if (this.himproveme$applyBlowgunPose(entity, Arm.RIGHT)) {
            ci.cancel();
        }
    }

    @Inject(method = "positionLeftArm", at = @At("HEAD"), cancellable = true)
    private void himproveme$applyLeftBlowgunPose(T entity, CallbackInfo ci) {
        if (this.himproveme$applyBlowgunPose(entity, Arm.LEFT)) {
            ci.cancel();
        }
    }

    @Inject(method = "animateArms", at = @At("HEAD"), cancellable = true)
    private void himproveme$cancelVanillaArmAnimationWhileAiming(T entity, float animationProgress, CallbackInfo ci) {
        if (BlowgunItem.isAiming(entity, Hand.MAIN_HAND) || BlowgunItem.isAiming(entity, Hand.OFF_HAND)) {
            ci.cancel();
        }
    }

    private boolean himproveme$applyBlowgunPose(T entity, Arm arm) {
        Hand hand = entity.getMainArm() == arm ? Hand.MAIN_HAND : Hand.OFF_HAND;
        if (!BlowgunItem.isAiming(entity, hand)) {
            return false;
        }

        int direction = arm == Arm.RIGHT ? 1 : -1;
        ModelPart usedArm = this.getArm(arm);
        usedArm.pitch = -1.96F + this.head.pitch * 0.92F;
        usedArm.yaw = direction * -0.10F + this.head.yaw * 0.82F - this.body.yaw * 0.16F;
        usedArm.roll = direction * -0.05F;
        return true;
    }
}
