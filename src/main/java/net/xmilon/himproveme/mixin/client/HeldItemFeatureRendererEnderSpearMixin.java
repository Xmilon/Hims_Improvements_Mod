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
import net.xmilon.himproveme.combat.EnderSpearSlashHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemFeatureRenderer.class)
public abstract class HeldItemFeatureRendererEnderSpearMixin {
    @Inject(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void himproveme$animateThirdPersonEnderSpearSlash(LivingEntity entity, ItemStack stack, ModelTransformationMode transformationMode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!EnderSpearSlashHelper.isDoubleEnderSpear(stack)) {
            return;
        }

        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
        Hand hand = entity.getMainArm() == arm ? Hand.MAIN_HAND : Hand.OFF_HAND;
        EnderSpearSlashHelper.SlashAnimation animation = EnderSpearSlashHelper.getSlashAnimation(entity, hand, tickDelta);
        if (!animation.active()) {
            return;
        }

        int direction = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate(
                direction * (-0.05F * animation.windup() + 0.28F * animation.strike() - 0.12F * animation.recovery()),
                0.02F * animation.windup() - 0.05F * animation.strike(),
                -0.08F * animation.windup() + 0.16F * animation.strike() - 0.02F * animation.recovery()
        );
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (18.0F * animation.windup() - 104.0F * animation.overswing() + 24.0F * animation.recovery())));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * (-12.0F * animation.windup() + 72.0F * animation.strike() - 18.0F * animation.recovery())));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-24.0F * animation.windup() + 26.0F * animation.strike() - 10.0F * animation.recovery()));
    }
}
