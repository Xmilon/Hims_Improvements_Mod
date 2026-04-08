package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import net.xmilon.himproveme.client.InspectAnimationHelper;
import net.xmilon.himproveme.client.InspectAnimationPresets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemFeatureRenderer.class)
public abstract class HeldItemFeatureRendererInspectMixin {
    @Inject(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void himproveme$animateThirdPersonInspectSpin(
            LivingEntity entity,
            ItemStack stack,
            ModelTransformationMode transformationMode,
            Arm arm,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
        Hand hand = entity.getMainArm() == arm ? Hand.MAIN_HAND : Hand.OFF_HAND;
        InspectAnimationHelper.InspectAnimation animation = InspectAnimationHelper.getAnimation(entity, hand, tickDelta);
        if (!animation.active()) {
            return;
        }

        int direction = arm == Arm.RIGHT ? 1 : -1;
        if (animation.style() == InspectAnimationPresets.MotionStyle.HALF_ROLL) {
            matrices.translate(
                    direction * 0.01F,
                    0.05F * animation.lift(),
                    -0.04F - 0.02F * animation.blur()
            );
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (6.0F + 5.0F * animation.sway())));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-10.0F + 10.0F * animation.snap()));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * animation.spinDegrees()));
            if (animation.looping()) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(5.0F * animation.snap()));
            }
            return;
        }

        if (animation.style() == InspectAnimationPresets.MotionStyle.KARAMBIT_RING) {
            matrices.translate(
                    direction * 0.004F,
                    0.022F * animation.lift(),
                    -0.014F - 0.014F * animation.blur()
            );
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (106.0F + 6.0F * animation.flourish())));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(78.0F + 4.0F * animation.drift()));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * (90.0F + animation.spinDegrees() + 14.0F * animation.drift())));
            if (animation.looping()) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (6.0F * animation.flourish())));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(4.0F * animation.drift()));
            }
            return;
        }

        if (animation.style() == InspectAnimationPresets.MotionStyle.Y_TWIRL) {
            matrices.translate(
                    direction * 0.02F,
                    0.04F * animation.lift(),
                    -0.02F - 0.03F * animation.blur()
            );
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (8.0F + 6.0F * animation.sway() + 4.0F * animation.drift())));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * (-6.0F + 8.0F * animation.snap() + 4.0F * animation.flourish())));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-96.0F - animation.spinDegrees() - 4.0F * animation.flourish()));
            if (animation.looping()) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (5.0F * animation.snap() + 3.0F * animation.drift())));
            }
            return;
        }

        if (animation.style() == InspectAnimationPresets.MotionStyle.HEAVY_ARC) {
            matrices.translate(
                    direction * 0.03F,
                    0.05F * animation.lift(),
                    -0.05F - 0.03F * animation.blur()
            );
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (12.0F + 6.0F * animation.sway() + 4.0F * animation.drift())));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-22.0F + 16.0F * animation.lift() + 8.0F * animation.flourish()));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * (animation.spinDegrees() * 0.82F + 12.0F * animation.snap())));
            if (animation.looping()) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F * animation.drift()));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (5.0F * animation.flourish())));
            }
            return;
        }

        if (animation.style() == InspectAnimationPresets.MotionStyle.STAFF_FLIP) {
            matrices.translate(
                    direction * 0.02F,
                    0.07F * animation.lift(),
                    -0.06F - 0.02F * animation.blur()
            );
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (6.0F + 6.0F * animation.drift())));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-8.0F + 10.0F * animation.flourish()));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * animation.spinDegrees()));
            if (animation.looping()) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(7.0F * animation.snap()));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (6.0F * animation.flourish())));
            }
            return;
        }

        if (animation.style() == InspectAnimationPresets.MotionStyle.BAMBOO_FLOW) {
            matrices.translate(
                    direction * (0.014F + 0.010F * animation.lift()),
                    0.030F * animation.lift() + 0.008F * animation.flourish(),
                    -0.032F - 0.018F * animation.blur()
            );
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (12.0F + 6.0F * animation.sway() + 4.0F * animation.drift())));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-54.0F + 12.0F * animation.snap() - 4.0F * animation.flourish()));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * (24.0F + animation.spinDegrees() * 0.55F + 6.0F * animation.flourish())));
            if (animation.looping()) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (3.0F * animation.snap() + 4.0F * animation.flourish())));
            }
            return;
        }

        matrices.translate(
                direction * 0.03F,
                0.06F * animation.lift(),
                -0.05F * animation.lift() - 0.02F * animation.blur()
        );
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (10.0F + 6.0F * animation.sway() + 3.0F * animation.drift())));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-12.0F + 22.0F * animation.lift() + 4.0F * animation.flourish()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * animation.spinDegrees()));
        if (animation.looping()) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (6.0F * animation.snap() + 2.0F * animation.drift())));
        }
    }
}
