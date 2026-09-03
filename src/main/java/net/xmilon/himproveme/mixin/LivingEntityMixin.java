package net.xmilon.himproveme.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.xmilon.himproveme.access.DualWieldTargetAccess;
import net.xmilon.himproveme.item.custom.GodlyElytraItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements DualWieldTargetAccess {
    @Shadow
    public int hurtTime;

    @Shadow
    public int maxHurtTime;

    @Shadow
    protected float lastDamageTaken;

    @Unique
    private boolean himproveme$godlyElytraShouldFly;

    @Inject(method = "tickFallFlying", at = @At("HEAD"))
    private void himproveme$captureGodlyElytraFallFlying(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        ItemStack chest = self.getEquippedStack(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof GodlyElytraItem) {
            himproveme$godlyElytraShouldFly = self.isFallFlying()
                && !self.isOnGround()
                && !self.hasVehicle()
                && !self.hasStatusEffect(StatusEffects.LEVITATION);
        } else {
            himproveme$godlyElytraShouldFly = false;
        }
    }

    @Inject(method = "tickFallFlying", at = @At("TAIL"))
    private void himproveme$maintainGodlyElytraFallFlying(CallbackInfo ci) {
        if (!himproveme$godlyElytraShouldFly) return;

        LivingEntity self = (LivingEntity)(Object)this;
        if (self.getWorld().isClient) return;

        if (self instanceof PlayerEntity player) {
            player.startFallFlying();
        }

        ItemStack chest = self.getEquippedStack(EquipmentSlot.CHEST);
        if (chest.isDamaged()) {
            chest.setDamage(0);
        }
    }

    @Override
    public void himproveme$resetDualWieldDamageImmunity() {
        this.hurtTime = 0;
        this.maxHurtTime = 0;
        this.lastDamageTaken = 0.0f;
    }
}
