package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.xmilon.himproveme.client.WardenPerkClientHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the ritual self-strike pose for humanoid entities during the Frenzy sepuku countdown.
 */
@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelWardenSepukuMixin {
    @Shadow public ModelPart head;
    @Shadow public ModelPart body;
    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart rightLeg;
    @Shadow public ModelPart leftLeg;

    /**
     * Applies the custom pose after vanilla arm animation so the sepuku silhouette stays readable on top of normal motion.
     */
    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void himproveme$applySepukuPose(LivingEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        WardenPerkClientHelper.applySepukuPose(entity, this.head, this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg);
    }
}
