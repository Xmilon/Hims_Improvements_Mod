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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.xmilon.himproveme.client.BlowgunClientHelper;
import net.xmilon.himproveme.item.custom.BlowgunItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemFeatureRenderer.class)
public abstract class HeldItemFeatureRendererBlowgunMixin {
    @Inject(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void himproveme$animateThirdPersonBlowgunAim(LivingEntity entity, ItemStack stack, ModelTransformationMode transformationMode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        Hand hand = entity.getMainArm() == arm ? Hand.MAIN_HAND : Hand.OFF_HAND;
        if (!BlowgunItem.isBlowgun(stack) || !BlowgunItem.isAiming(entity, hand)) {
            return;
        }

        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
        float aimProgress = BlowgunClientHelper.getAimProgress(entity, hand, tickDelta);
        float easedAim = MathHelper.clamp(aimProgress * aimProgress * (3.0F - 2.0F * aimProgress), 0.0F, 1.0F);
        float recoil = BlowgunClientHelper.isAiming(entity, hand) ? BlowgunClientHelper.getRecoil(hand, tickDelta) : 0.0F;
        int direction = arm == Arm.RIGHT ? 1 : -1;

        matrices.translate(
                direction * (-0.06F * easedAim),
                0.07F * easedAim,
                0.05F * easedAim - 0.03F * recoil
        );
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (6.0F * easedAim)));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * (-45.0F * easedAim)));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10.0F * easedAim + 7.0F * recoil));
    }
}
