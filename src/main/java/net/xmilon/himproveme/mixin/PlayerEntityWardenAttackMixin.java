package net.xmilon.himproveme.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.xmilon.himproveme.combat.DualWieldAttackContext;
import net.xmilon.himproveme.item.custom.DaggerItem;
import net.xmilon.himproveme.perk.warden.WardenPerkHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures successful melee damage from the vanilla attack flow so the Warden affliction bar uses final post-armor damage.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityWardenAttackMixin {
    @Unique
    private static final float UNLOCKED_WARDEN_OUTGOING_DAMAGE_MULTIPLIER = 0.90F;
    @Unique
    private static final int DAGGER_SPAM_WINDOW_TICKS = 10;
    @Unique
    private static final int DAGGER_RECOVERY_STEP_TICKS = 18;
    @Unique
    private static final int MAX_DAGGER_FATIGUE = 5;
    @Unique
    private static final float DAGGER_MIN_DAMAGE_CAP = 2.0F;

    @Unique
    private float himproveme$currentAttackDamageMultiplier = 1.0F;
    @Unique
    private boolean himproveme$currentAttackUsesDagger;
    @Unique
    private boolean himproveme$currentAttackConsumedDaggerFatigue;
    @Unique
    private int himproveme$daggerFatigue;
    @Unique
    private long himproveme$lastDaggerAttackTick = -200L;

    /**
     * Prepares the per-attack damage multiplier so every damage call inside one swing uses the same balancing state.
     */
    @Inject(method = "attack", at = @At("HEAD"))
    private void himproveme$prepareAttackBalance(Entity target, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        ItemStack attackingStack = DualWieldAttackContext.isOffhandAttack(self) ? self.getOffHandStack() : self.getMainHandStack();
        this.himproveme$currentAttackUsesDagger = DaggerItem.isDagger(attackingStack);
        this.himproveme$currentAttackConsumedDaggerFatigue = false;
        this.himproveme$currentAttackDamageMultiplier = WardenPerkHelper.hasAnyUnlockedWardenPerk(self)
                ? UNLOCKED_WARDEN_OUTGOING_DAMAGE_MULTIPLIER
                : 1.0F;
    }

    /**
     * Wraps the actual damage call so the affliction system can observe the final health loss without rewriting vanilla combat.
     */
    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private boolean himproveme$trackWardenAfflictionDamage(Entity target, DamageSource source, float amount) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        float adjustedAmount = amount * this.himproveme$currentAttackDamageMultiplier;
        if (this.himproveme$currentAttackUsesDagger && !this.himproveme$currentAttackConsumedDaggerFatigue) {
            adjustedAmount *= himproveme$consumeDaggerFatigue(self);
            this.himproveme$currentAttackConsumedDaggerFatigue = true;
            if (this.himproveme$daggerFatigue >= MAX_DAGGER_FATIGUE) {
                adjustedAmount = Math.min(adjustedAmount, DAGGER_MIN_DAMAGE_CAP);
            }
        }

        float previousHealth = target instanceof LivingEntity livingTarget ? livingTarget.getHealth() : 0.0F;
        boolean damaged = target.damage(source, adjustedAmount);

        if (damaged
                && target instanceof LivingEntity livingTarget
                && (Object) this instanceof ServerPlayerEntity serverPlayer) {
            float dealtDamage = Math.max(0.0F, previousHealth - livingTarget.getHealth());
            WardenPerkHelper.onSuccessfulAttack(serverPlayer, livingTarget, dealtDamage, himproveme$isCriticalStrike(target));
        }

        return damaged;
    }

    /**
     * Mirrors vanilla critical-hit conditions closely enough for affliction gain without depending on fragile local capture.
     */
    @Unique
    private boolean himproveme$isCriticalStrike(Entity target) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        return self.getAttackCooldownProgress(0.5F) > 0.9F
                && self.fallDistance > 0.0F
                && !self.isOnGround()
                && !self.isClimbing()
                && !self.isTouchingWater()
                && !self.hasStatusEffect(StatusEffects.BLINDNESS)
                && !self.hasVehicle()
                && target instanceof LivingEntity
                && !self.isSprinting();
    }

    /**
     * Builds fatigue from rapid dagger hits, adds a visible item cooldown pulse, and returns the resulting damage multiplier.
     */
    @Unique
    private float himproveme$consumeDaggerFatigue(PlayerEntity player) {
        long now = player.getWorld().getTime();
        long elapsed = now - this.himproveme$lastDaggerAttackTick;

        if (elapsed > DAGGER_RECOVERY_STEP_TICKS) {
            int recoveredFatigue = (int) (elapsed / DAGGER_RECOVERY_STEP_TICKS);
            this.himproveme$daggerFatigue = Math.max(0, this.himproveme$daggerFatigue - recoveredFatigue);
        }

        if (elapsed <= DAGGER_SPAM_WINDOW_TICKS) {
            this.himproveme$daggerFatigue = Math.min(MAX_DAGGER_FATIGUE, this.himproveme$daggerFatigue + 1);
        } else if (this.himproveme$daggerFatigue > 0 && elapsed >= DAGGER_RECOVERY_STEP_TICKS * 2L) {
            this.himproveme$daggerFatigue--;
        }

        this.himproveme$lastDaggerAttackTick = now;
        himproveme$applyDaggerCooldownPulse(player, 4 + this.himproveme$daggerFatigue * 3);
        return switch (this.himproveme$daggerFatigue) {
            case 0 -> 1.0F;
            case 1 -> 0.82F;
            case 2 -> 0.68F;
            case 3 -> 0.54F;
            case 4 -> 0.42F;
            default -> 0.30F;
        };
    }

    /**
     * Applies a short cooldown flash to every equipped dagger so the player gets the same feedback loop shields use.
     */
    @Unique
    private void himproveme$applyDaggerCooldownPulse(PlayerEntity player, int cooldownTicks) {
        if (DaggerItem.isDagger(player.getMainHandStack())) {
            player.getItemCooldownManager().set(player.getMainHandStack().getItem(), cooldownTicks);
        }
        if (DaggerItem.isDagger(player.getOffHandStack())) {
            player.getItemCooldownManager().set(player.getOffHandStack().getItem(), cooldownTicks);
        }
    }
}
