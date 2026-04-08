package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;
import net.xmilon.himproveme.client.DaggerGripClientHelper;
import net.xmilon.himproveme.item.custom.DaggerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemFeatureRenderer.class)
public abstract class HeldItemFeatureRendererDualWieldMixin {
    @Inject(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void himproveme$applyThirdPersonDaggerPose(
            LivingEntity entity,
            ItemStack stack,
            ModelTransformationMode transformationMode,
            Arm arm,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        if (!DaggerItem.isDagger(stack)) {
            return;
        }

        int direction = arm == Arm.RIGHT ? 1 : -1;
        if (DaggerGripClientHelper.isWallGripping(entity)) {
            float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
            float scrape = DaggerGripClientHelper.getScrapeWave(tickDelta);
            float brace = DaggerGripClientHelper.getBraceWave(tickDelta);
            float pull = DaggerGripClientHelper.getPullWave(tickDelta);

            matrices.translate(
                    direction * (0.03F + 0.012F * pull),
                    0.03F * brace,
                    -0.09F - 0.03F * brace
            );
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * (28.0F + 6.0F * pull)));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-34.0F + 7.0F * scrape));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * (-18.0F + 10.0F * scrape)));
            return;
        }

        matrices.translate(direction * 0.02F, 0.01F, 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * -18.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * 12.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-8.0F));
    }
}
