package net.xmilon.himproveme.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public final class BleedingStatusEffect extends StatusEffect {
    public BleedingStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xA51F24);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.getWorld().isClient() && entity.age % 20 == 0) {
            float damageAmount = entity.getMaxHealth() * 0.05F;
            if (damageAmount > 0.0F) {
                entity.damage(entity.getDamageSources().magic(), damageAmount);
            }
        }
        return true;
    }
}
