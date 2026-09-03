package net.xmilon.himproveme.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public final class FrenzyStatusEffect extends StatusEffect {
    public FrenzyStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xFFD700);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }
}
