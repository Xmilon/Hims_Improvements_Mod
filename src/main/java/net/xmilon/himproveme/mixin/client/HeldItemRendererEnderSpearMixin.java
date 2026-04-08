package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import net.xmilon.himproveme.combat.EnderSpearSlashHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererEnderSpearMixin {
    @Inject(
            method = "renderFirstPersonItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void himproveme$animateFirstPersonEnderSpearSlash(
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
        if (!EnderSpearSlashHelper.isDoubleEnderSpear(item)) {
            return;
        }

        EnderSpearSlashHelper.SlashAnimation animation = EnderSpearSlashHelper.getSlashAnimation(player, hand, tickDelta);
        if (!animation.active()) {
            return;
        }

        Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        int direction = arm == Arm.RIGHT ? 1 : -1;

        matrices.translate(
                direction * (-0.08F * animation.windup() + 0.58F * animation.strike() - 0.22F * animation.recovery()),
                0.08F * animation.windup() - 0.10F * animation.strike() + 0.03F * animation.recovery(),
                0.18F * animation.windup() - 0.46F * animation.overswing() + 0.16F * animation.recovery()
        );
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (32.0F * animation.windup() - 114.0F * animation.overswing() + 28.0F * animation.recovery())));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * (-28.0F * animation.windup() + 90.0F * animation.overswing() - 16.0F * animation.recovery())));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-46.0F * animation.windup() + 34.0F * animation.strike() - 18.0F * animation.recovery()));
    }
}
