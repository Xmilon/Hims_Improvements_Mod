package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.xmilon.himproveme.combat.EnderSpearSlashHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BipedEntityModel.class, priority = 1500)
public abstract class BipedEntityModelEnderSpearMixin<T extends LivingEntity> extends AnimalModel<T> {
    @Shadow
    @Final
    public ModelPart rightArm;

    @Shadow
    @Final
    public ModelPart leftArm;

    @Shadow
    @Final
    public ModelPart body;

    @Shadow
    @Final
    public ModelPart head;

    @Shadow
    protected abstract ModelPart getArm(Arm arm);

    @Inject(method = "positionRightArm", at = @At("HEAD"), cancellable = true)
    private void himproveme$applyRightHandEnderSpearSlash(T entity, CallbackInfo ci) {
        if (this.himproveme$applySlashPose(entity, Arm.RIGHT)) {
            ci.cancel();
        }
    }

    @Inject(method = "positionLeftArm", at = @At("HEAD"), cancellable = true)
    private void himproveme$applyLeftHandEnderSpearSlash(T entity, CallbackInfo ci) {
        if (this.himproveme$applySlashPose(entity, Arm.LEFT)) {
            ci.cancel();
        }
    }

    @Inject(method = "animateArms", at = @At("HEAD"), cancellable = true)
    private void himproveme$cancelVanillaArmAnimationDuringEnderSpearSlash(T entity, float animationProgress, CallbackInfo ci) {
        if (EnderSpearSlashHelper.isSlashActive(entity, Hand.MAIN_HAND) || EnderSpearSlashHelper.isSlashActive(entity, Hand.OFF_HAND)) {
            ci.cancel();
        }
    }

    private boolean himproveme$applySlashPose(T entity, Arm arm) {
        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
        Hand hand = entity.getMainArm() == arm ? Hand.MAIN_HAND : Hand.OFF_HAND;
        ItemStack stack = hand == Hand.MAIN_HAND ? entity.getMainHandStack() : entity.getOffHandStack();
        EnderSpearSlashHelper.SlashAnimation animation = EnderSpearSlashHelper.getSlashAnimation(entity, hand, tickDelta);

        if (EnderSpearSlashHelper.isDoubleEnderSpear(stack) && animation.active()) {
            int direction = arm == Arm.RIGHT ? 1 : -1;
            float sweep = animation.strike() - animation.recovery() * 0.6F;
            ModelPart usedArm = this.getArm(arm);
            usedArm.pitch = -1.45F - 0.85F * animation.windup() + 1.65F * animation.strike() - 0.55F * animation.recovery() + this.head.pitch * 0.35F;
            usedArm.yaw = direction * (0.95F * animation.windup() - 2.15F * animation.overswing() + 0.45F * animation.recovery()) - this.body.yaw * 0.15F;
            usedArm.roll = direction * (-0.45F - 0.9F * animation.windup() + 1.8F * sweep - 0.25F * animation.recovery());
            return true;
        }

        Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        EnderSpearSlashHelper.SlashAnimation otherAnimation = EnderSpearSlashHelper.getSlashAnimation(entity, otherHand, tickDelta);
        if (!otherAnimation.active()) {
            return false;
        }

        ModelPart supportArm = this.getArm(arm);
        int direction = arm == Arm.RIGHT ? 1 : -1;
        supportArm.pitch = -0.25F + 0.35F * otherAnimation.windup() - 0.6F * otherAnimation.strike() + 0.2F * otherAnimation.recovery();
        supportArm.yaw = direction * (-0.18F - 0.25F * otherAnimation.windup() + 0.25F * otherAnimation.recovery()) - this.body.yaw * 0.1F;
        supportArm.roll = direction * 0.12F;
        return true;
    }
}
