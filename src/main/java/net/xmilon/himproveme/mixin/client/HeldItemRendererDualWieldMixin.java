package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.xmilon.himproveme.access.DualWieldAttackAccess;
import net.xmilon.himproveme.client.DaggerGripClientHelper;
import net.xmilon.himproveme.combat.DualWieldCombatHelper;
import net.xmilon.himproveme.item.custom.DaggerItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererDualWieldMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private ItemStack offHand;

    @Shadow
    private float equipProgressOffHand;

    @ModifyArgs(
            method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
            )
    )
    private void himproveme$renderIndependentDualWieldSwing(Args args) {
        AbstractClientPlayerEntity player = args.get(0);
        float tickDelta = args.get(1);
        Hand hand = args.get(3);

        if (player instanceof DualWieldAttackAccess access
                && hand == Hand.OFF_HAND
                && (DualWieldCombatHelper.canUseOffhandAttack(player) || DualWieldCombatHelper.isOffhandSwingActive(player))) {
            args.set(4, access.himproveme$getOffhandSwingProgress(tickDelta));
        }
    }

    @Inject(method = "updateHeldItems", at = @At("TAIL"))
    private void himproveme$mirrorMainHandOffhandCooldownAnimation(CallbackInfo ci) {
        if (this.client.player == null || this.client.player.isRiding() || !DualWieldCombatHelper.canUseOffhandAttack(this.client.player)) {
            return;
        }

        ItemStack offhandStack = this.client.player.getOffHandStack();
        float cooldownProgress = DualWieldCombatHelper.getOffhandAttackCooldownProgress(this.client.player, 1.0f);
        float targetEquipProgress = this.offHand == offhandStack ? cooldownProgress * cooldownProgress * cooldownProgress : 0.0f;
        this.equipProgressOffHand += MathHelper.clamp(targetEquipProgress - this.equipProgressOffHand, -0.4f, 0.4f);

        if (this.equipProgressOffHand < 0.1f) {
            this.offHand = offhandStack;
        }
    }

    @Inject(
            method = "renderFirstPersonItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void himproveme$applyDaggerGripAndSwing(
            AbstractClientPlayerEntity player,
            float tickDelta,
            float pitch,
            Hand hand,
            float swingProgress,
            ItemStack item,
            float equipProgress,
            net.minecraft.client.util.math.MatrixStack matrices,
            net.minecraft.client.render.VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        if (!DaggerItem.isDagger(item) || player.isUsingItem()) {
            return;
        }

        boolean mainHand = hand == Hand.MAIN_HAND;
        Arm arm = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
        int direction = arm == Arm.RIGHT ? 1 : -1;

        if (DaggerGripClientHelper.isWallGripping(player)) {
            float scrape = DaggerGripClientHelper.getScrapeWave(tickDelta);
            float brace = DaggerGripClientHelper.getBraceWave(tickDelta);
            float pull = DaggerGripClientHelper.getPullWave(tickDelta);

            matrices.translate(
                    direction * (0.11F + 0.02F * pull),
                    -0.03F + 0.02F * brace,
                    -0.34F - 0.03F * brace
            );
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (60.0F + 8.0F * pull)));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-66.0F + 8.0F * scrape));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * (-26.0F + 12.0F * scrape)));
            matrices.translate(direction * 0.014F * scrape, -0.018F * brace, -0.045F * brace);
            return;
        }

        float rootSwing = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        float fullSwing = MathHelper.sin(swingProgress * (float) Math.PI);

        matrices.translate(direction * 0.05F, -0.08F, 0.01F);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * -22.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * 14.0F));

        matrices.translate(-direction * 0.38F * rootSwing, 0.04F * fullSwing, -0.18F * rootSwing);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * 42.0F * rootSwing));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * 24.0F * fullSwing));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-20.0F * rootSwing));
    }
}
