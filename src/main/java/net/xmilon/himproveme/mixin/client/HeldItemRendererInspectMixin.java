package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
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

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererInspectMixin {
    @Inject(
            method = "renderFirstPersonItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void himproveme$animateInspectSpin(
            AbstractClientPlayerEntity player,
            float tickDelta,
            float pitch,
            Hand hand,
            float swingProgress,
            ItemStack item,
            float equipProgress,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        InspectAnimationHelper.InspectAnimation animation = InspectAnimationHelper.getAnimation(player, hand, tickDelta);
        if (!animation.active()) {
            return;
        }

        Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        int direction = arm == Arm.RIGHT ? 1 : -1;
        if (animation.style() == InspectAnimationPresets.MotionStyle.HALF_ROLL) {
            matrices.translate(
                    direction * (0.01F + 0.02F * animation.sway()),
                    -0.05F + 0.12F * animation.lift(),
                    -0.12F - 0.05F * animation.blur()
            );
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (8.0F + 8.0F * animation.sway())));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-32.0F + 16.0F * animation.snap()));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * animation.spinDegrees()));
            if (animation.looping()) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F * animation.snap()));
                matrices.translate(0.0F, 0.016F * animation.snap(), -0.02F);
            }
            return;
        }

        if (animation.style() == InspectAnimationPresets.MotionStyle.KARAMBIT_RING) {
            matrices.translate(
                    direction * (0.002F + 0.010F * animation.drift()),
                    -0.050F + 0.050F * animation.lift(),
                    -0.018F - 0.020F * animation.blur()
            );
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (120.0F + 8.0F * animation.flourish())));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(96.0F + 4.0F * animation.drift()));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * (90.0F + animation.spinDegrees() + 18.0F * animation.drift())));
            if (animation.looping()) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (8.0F * animation.flourish())));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F * animation.drift()));
                matrices.translate(direction * 0.008F * animation.flourish(), 0.006F * animation.drift(), -0.008F);
            }
            return;
        }

        if (animation.style() == InspectAnimationPresets.MotionStyle.Y_TWIRL) {
            matrices.translate(
                    direction * (0.02F + 0.03F * animation.lift()),
                    -0.12F + 0.11F * animation.lift(),
                    -0.04F - 0.06F * animation.blur()
            );
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (16.0F + 8.0F * animation.sway() + 6.0F * animation.drift())));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * (-12.0F + 10.0F * animation.snap() + 6.0F * animation.flourish())));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-106.0F - animation.spinDegrees() - 6.0F * animation.flourish()));
            if (animation.looping()) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (6.0F * animation.snap() + 4.0F * animation.drift())));
                matrices.translate(direction * 0.016F * animation.sway(), 0.020F * animation.snap(), -0.03F - 0.01F * animation.flourish());
            }
            return;
        }

        if (animation.style() == InspectAnimationPresets.MotionStyle.HEAVY_ARC) {
            matrices.translate(
                    direction * (0.08F + 0.04F * animation.lift()),
                    -0.10F + 0.15F * animation.lift(),
                    -0.12F - 0.06F * animation.blur()
            );
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (24.0F + 10.0F * animation.sway() + 6.0F * animation.drift())));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-64.0F + 26.0F * animation.lift() + 10.0F * animation.flourish()));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * (animation.spinDegrees() * 0.82F + 18.0F * animation.snap())));
            if (animation.looping()) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10.0F * animation.drift()));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (8.0F * animation.flourish())));
                matrices.translate(direction * 0.018F * animation.drift(), 0.012F * animation.flourish(), -0.02F);
            }
            return;
        }

        if (animation.style() == InspectAnimationPresets.MotionStyle.STAFF_FLIP) {
            matrices.translate(
                    direction * (0.03F + 0.02F * animation.sway()),
                    -0.03F + 0.18F * animation.lift(),
                    -0.16F - 0.04F * animation.blur()
            );
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (10.0F + 10.0F * animation.drift())));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-20.0F + 12.0F * animation.flourish()));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * animation.spinDegrees()));
            if (animation.looping()) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10.0F * animation.snap()));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (10.0F * animation.flourish())));
                matrices.translate(direction * 0.012F * animation.drift(), 0.018F * animation.sway(), -0.022F);
            }
            return;
        }

        if (animation.style() == InspectAnimationPresets.MotionStyle.BAMBOO_FLOW) {
            matrices.translate(
                    direction * (0.015F + 0.025F * animation.lift() + 0.008F * animation.drift()),
                    -0.070F + 0.105F * animation.lift() + 0.010F * animation.flourish(),
                    -0.165F - 0.030F * animation.blur()
            );
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (26.0F + 8.0F * animation.sway() + 6.0F * animation.drift())));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-82.0F + 12.0F * animation.snap() - 8.0F * animation.flourish()));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * (36.0F + animation.spinDegrees() * 0.55F + 8.0F * animation.flourish())));
            if (animation.looping()) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (4.0F * animation.snap() + 6.0F * animation.flourish())));
                matrices.translate(direction * 0.012F * animation.sway(), 0.012F * animation.flourish(), -0.016F);
            }
            return;
        }

        matrices.translate(
                direction * (0.07F + 0.04F * animation.lift()),
                -0.08F + 0.16F * animation.lift(),
                -0.10F * animation.lift() - 0.05F * animation.blur()
        );
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (18.0F + 10.0F * animation.sway() + 4.0F * animation.drift())));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-42.0F + 28.0F * animation.lift() - 7.0F * animation.sway() - 8.0F * animation.blur() + 5.0F * animation.flourish()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * animation.spinDegrees()));
        if (animation.looping()) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (8.0F * animation.snap() + 3.0F * animation.drift())));
            matrices.translate(direction * 0.018F * animation.sway(), 0.016F * animation.snap(), -0.024F - 0.008F * animation.flourish());
        }
    }
}
