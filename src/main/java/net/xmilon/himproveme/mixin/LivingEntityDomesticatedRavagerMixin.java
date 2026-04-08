package net.xmilon.himproveme.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.xmilon.himproveme.access.DomesticatedRavagerAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDomesticatedRavagerMixin {
    @Inject(method = "tickControlled", at = @At("HEAD"))
    private void himproveme$alignDomesticatedRavagerToRider(PlayerEntity controllingPlayer, Vec3d movementInput, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof RavagerEntity ravager)
                || !((DomesticatedRavagerAccess) ravager).himproveme$isOwnedBy(controllingPlayer)) {
            return;
        }

        self.setYaw(controllingPlayer.getYaw());
        self.prevYaw = self.getYaw();
        self.setPitch(controllingPlayer.getPitch() * 0.5f);
        self.setBodyYaw(self.getYaw());
        self.setHeadYaw(self.getYaw());
    }

    @Inject(method = "getControlledMovementInput", at = @At("HEAD"), cancellable = true)
    private void himproveme$usePlayerInputForDomesticatedRavager(PlayerEntity controllingPlayer, Vec3d movementInput, CallbackInfoReturnable<Vec3d> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof RavagerEntity ravager)
                || !((DomesticatedRavagerAccess) ravager).himproveme$isOwnedBy(controllingPlayer)) {
            return;
        }

        double forward = controllingPlayer.forwardSpeed;
        if (forward <= 0.0) {
            forward *= 0.25;
        }

        cir.setReturnValue(new Vec3d(controllingPlayer.sidewaysSpeed * 0.5f, movementInput.y, forward));
    }

    @Inject(method = "getSaddledSpeed", at = @At("HEAD"), cancellable = true)
    private void himproveme$setDomesticatedRavagerRideSpeed(PlayerEntity controllingPlayer, CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof RavagerEntity ravager)
                || !((DomesticatedRavagerAccess) ravager).himproveme$isOwnedBy(controllingPlayer)) {
            return;
        }

        cir.setReturnValue((float) ravager.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 1.2f);
    }
}
