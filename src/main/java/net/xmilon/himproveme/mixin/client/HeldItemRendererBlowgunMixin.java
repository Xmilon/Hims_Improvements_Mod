package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.xmilon.himproveme.client.BlowgunClientHelper;
import net.xmilon.himproveme.item.custom.BlowgunItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererBlowgunMixin {
    @Inject(
            method = "renderFirstPersonItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void himproveme$animateFirstPersonBlowgunAim(
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
        if (!BlowgunItem.isBlowgun(item) || !BlowgunClientHelper.isAiming(player, hand)) {
            return;
        }

        float aimProgress = BlowgunClientHelper.getAimProgress(player, hand, tickDelta);
        float easedAim = MathHelper.clamp(aimProgress * aimProgress * (3.0F - 2.0F * aimProgress), 0.0F, 1.0F);
        float recoil = BlowgunClientHelper.getRecoil(hand, tickDelta);
        Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        int direction = arm == Arm.RIGHT ? 1 : -1;

        matrices.translate(
                direction * (-0.34F * easedAim + 0.015F * recoil),
                0.18F * easedAim + 0.01F * recoil,
                0.12F * easedAim - 0.04F * recoil
        );
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (10.0F * easedAim)));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * (-45.0F * easedAim)));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-16.0F * easedAim + 10.0F * recoil));
    }
}
