package net.xmilon.himproveme.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public final class StunnedStatusEffect extends StatusEffect {
    public StunnedStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xB9C27D);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.getWorld().isClient()) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 30, 0, true, false, true));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 30, 0, true, false, true));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 30, 0, true, false, true));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 30, 0, true, false, true));
        }
        return true;
    }
}
