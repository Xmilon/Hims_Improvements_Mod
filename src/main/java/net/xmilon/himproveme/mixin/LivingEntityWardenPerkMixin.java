package net.xmilon.himproveme.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.effect.ModStatusEffects;
import net.xmilon.himproveme.perk.warden.WardenPerkHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityWardenPerkMixin {
    @Unique
    private float himproveme$preDamageHealth;

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void himproveme$capturePreDamageHealth(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        Entity attacker = source.getAttacker();
        if (attacker instanceof LivingEntity living && living.hasStatusEffect(ModStatusEffects.FRENZY)) {
            if (!WardenPerkHelper.himproveme$spiritShackleGuard) {
                cir.setReturnValue(false);
                WardenPerkHelper.himproveme$spiritShackleGuard = true;
                float reflectAmount = amount * 0.4F;
                living.damage(living.getDamageSources().magic(), reflectAmount);
                WardenPerkHelper.himproveme$spiritShackleGuard = false;
            }
            return;
        }
        Entity sourceEntity = source.getSource();
        if (sourceEntity instanceof LivingEntity living && living.hasStatusEffect(ModStatusEffects.FRENZY)) {
            if (!WardenPerkHelper.himproveme$spiritShackleGuard) {
                cir.setReturnValue(false);
                WardenPerkHelper.himproveme$spiritShackleGuard = true;
                float reflectAmount = amount * 0.4F;
                living.damage(living.getDamageSources().magic(), reflectAmount);
                WardenPerkHelper.himproveme$spiritShackleGuard = false;
            }
            return;
        }

        this.himproveme$preDamageHealth = self.getHealth();
        HimProveMe.LOGGER.info("HEAD damage: entity={}, health before={}", self.getName().getString(), this.himproveme$preDamageHealth);
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void himproveme$onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (WardenPerkHelper.himproveme$frenzyDamageGuard) return;

        LivingEntity self = (LivingEntity) (Object) this;
        HimProveMe.LOGGER.info("RETURN damage: entity={}, ret={}, attacker={}, health now={}",
            self.getName().getString(), cir.getReturnValue(),
            source.getAttacker() != null ? source.getAttacker().getName().getString() : "null",
            self.getHealth());

        if (!cir.getReturnValue()) return;
        if (!(source.getAttacker() instanceof ServerPlayerEntity attacker)) return;

        float dealtDamage = Math.max(0.0F, this.himproveme$preDamageHealth - self.getHealth());
        HimProveMe.LOGGER.info("dealtDamage computed: pre={}, cur={}, dealt={}", this.himproveme$preDamageHealth, self.getHealth(), dealtDamage);
        WardenPerkHelper.onSuccessfulAttack(attacker, self, dealtDamage);
    }
}
