package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.xmilon.himproveme.client.DaggerGripClientHelper;
import net.xmilon.himproveme.item.custom.DaggerItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelDaggerGripMixin<T extends LivingEntity> extends AnimalModel<T> {
    @Shadow
    @Final
    public ModelPart body;

    @Shadow
    @Final
    public ModelPart head;

    @Shadow
    protected abstract ModelPart getArm(Arm arm);

    @Inject(method = "positionRightArm", at = @At("HEAD"), cancellable = true)
    private void himproveme$applyGripPoseRight(T entity, CallbackInfo ci) {
        if (himproveme$applyGripPose(entity, Arm.RIGHT)) {
            ci.cancel();
        }
    }

    @Inject(method = "positionLeftArm", at = @At("HEAD"), cancellable = true)
    private void himproveme$applyGripPoseLeft(T entity, CallbackInfo ci) {
        if (himproveme$applyGripPose(entity, Arm.LEFT)) {
            ci.cancel();
        }
    }

    private boolean himproveme$applyGripPose(T entity, Arm arm) {
        if (!DaggerGripClientHelper.isWallGripping(entity)) {
            return false;
        }

        Hand hand = entity.getMainArm() == arm ? Hand.MAIN_HAND : Hand.OFF_HAND;
        ItemStack stack = hand == Hand.MAIN_HAND ? entity.getMainHandStack() : entity.getOffHandStack();
        if (!DaggerItem.isDagger(stack)) {
            return false;
        }

        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
        float scrape = DaggerGripClientHelper.getScrapeWave(tickDelta);
        float brace = DaggerGripClientHelper.getBraceWave(tickDelta);
        float pull = DaggerGripClientHelper.getPullWave(tickDelta);

        ModelPart usedArm = this.getArm(arm);
        int direction = arm == Arm.RIGHT ? 1 : -1;
        usedArm.pitch = -1.78F + 0.08F * brace + 0.08F * scrape + this.head.pitch * 0.12F;
        usedArm.yaw = direction * (0.16F + 0.08F * pull) - this.body.yaw * 0.06F;
        usedArm.roll = direction * (-0.20F + 0.06F * scrape);
        return true;
    }
}
