package net.xmilon.himproveme.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * Marker effect for the frenzy affliction.
 * Hallucinations, input disruption and the sepuku sequence are managed outside vanilla ticking.
 */
public final class FrenzyStatusEffect extends StatusEffect {
    public FrenzyStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xB72D2A);
    }

    /**
     * Disables vanilla per-tick effect handling because the custom perk manager owns the timing.
     */
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }
}
