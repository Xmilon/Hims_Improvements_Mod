package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.xmilon.himproveme.client.InspectAnimationHelper;
import net.xmilon.himproveme.client.InspectAnimationPresets;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelInspectMixin<T extends LivingEntity> extends AnimalModel<T> {
    @Shadow
    @Final
    public ModelPart body;

    @Shadow
    @Final
    public ModelPart head;

    @Shadow
    protected abstract ModelPart getArm(Arm arm);

    @Inject(method = "positionRightArm", at = @At("HEAD"), cancellable = true)
    private void himproveme$applyInspectPoseRight(T entity, CallbackInfo ci) {
        if (himproveme$applyInspectPose(entity, Arm.RIGHT)) {
            ci.cancel();
        }
    }

    @Inject(method = "positionLeftArm", at = @At("HEAD"), cancellable = true)
    private void himproveme$applyInspectPoseLeft(T entity, CallbackInfo ci) {
        if (himproveme$applyInspectPose(entity, Arm.LEFT)) {
            ci.cancel();
        }
    }

    private boolean himproveme$applyInspectPose(T entity, Arm arm) {
        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
        Hand hand = entity.getMainArm() == arm ? Hand.MAIN_HAND : Hand.OFF_HAND;
        InspectAnimationHelper.InspectAnimation animation = InspectAnimationHelper.getAnimation(entity, hand, tickDelta);
        if (!animation.active()) {
            return false;
        }

        ItemStack stack = hand == Hand.MAIN_HAND ? entity.getMainHandStack() : entity.getOffHandStack();
        if (!InspectAnimationHelper.isInspectable(stack)) {
            return false;
        }

        ModelPart usedArm = this.getArm(arm);
        int direction = arm == Arm.RIGHT ? 1 : -1;
        if (animation.style() == InspectAnimationPresets.MotionStyle.HALF_ROLL) {
            usedArm.pitch = -1.06F + 0.18F * animation.lift() + 0.08F * animation.snap() + this.head.pitch * 0.18F;
            usedArm.yaw = direction * (0.10F + 0.06F * animation.sway()) - this.body.yaw * 0.05F;
            usedArm.roll = direction * (-0.08F + 0.12F * animation.lift());
            return true;
        }

        if (animation.style() == InspectAnimationPresets.MotionStyle.KARAMBIT_RING) {
            usedArm.pitch = -0.74F + 0.06F * animation.lift() + 0.08F * animation.drift() + this.head.pitch * 0.10F;
            usedArm.yaw = direction * (0.22F + 0.08F * animation.flourish()) - this.body.yaw * 0.04F;
            usedArm.roll = direction * (0.14F + 0.14F * animation.sway() + 0.08F * animation.drift());
            return true;
        }

        if (animation.style() == InspectAnimationPresets.MotionStyle.Y_TWIRL) {
            usedArm.pitch = -1.34F + 0.12F * animation.lift() + 0.10F * animation.snap() + 0.06F * animation.flourish() + this.head.pitch * 0.14F;
            usedArm.yaw = direction * (0.26F + 0.10F * animation.sway() + 0.06F * animation.drift()) - this.body.yaw * 0.08F;
            usedArm.roll = direction * (-0.06F + 0.12F * animation.snap() + 0.06F * animation.flourish());
            return true;
        }

        if (animation.style() == InspectAnimationPresets.MotionStyle.HEAVY_ARC) {
            usedArm.pitch = -1.24F + 0.20F * animation.lift() + 0.08F * animation.flourish() + this.head.pitch * 0.18F;
            usedArm.yaw = direction * (0.18F + 0.08F * animation.sway() + 0.06F * animation.drift()) - this.body.yaw * 0.06F;
            usedArm.roll = direction * (-0.28F + 0.16F * animation.lift() + 0.08F * animation.flourish());
            return true;
        }

        if (animation.style() == InspectAnimationPresets.MotionStyle.STAFF_FLIP) {
            usedArm.pitch = -1.02F + 0.18F * animation.lift() + 0.06F * animation.flourish() + this.head.pitch * 0.16F;
            usedArm.yaw = direction * (0.14F + 0.10F * animation.drift()) - this.body.yaw * 0.05F;
            usedArm.roll = direction * (-0.14F + 0.16F * animation.sway() + 0.06F * animation.flourish());
            return true;
        }

        if (animation.style() == InspectAnimationPresets.MotionStyle.BAMBOO_FLOW) {
            usedArm.pitch = -1.10F + 0.10F * animation.lift() + 0.06F * animation.snap() + this.head.pitch * 0.18F;
            usedArm.yaw = direction * (0.20F + 0.08F * animation.sway() + 0.04F * animation.drift()) - this.body.yaw * 0.06F;
            usedArm.roll = direction * (-0.10F + 0.10F * animation.flourish() + 0.08F * animation.snap());
            return true;
        }

        usedArm.pitch = -1.18F + 0.24F * animation.lift() + 0.06F * animation.snap() + 0.05F * animation.flourish() + this.head.pitch * 0.2F;
        usedArm.yaw = direction * (0.24F + 0.10F * animation.sway() + 0.05F * animation.drift()) - this.body.yaw * 0.08F;
        usedArm.roll = direction * (-0.20F + 0.12F * animation.lift() + 0.08F * animation.snap() + 0.05F * animation.flourish());
        return true;
    }
}
