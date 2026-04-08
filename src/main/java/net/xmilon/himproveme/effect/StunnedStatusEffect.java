package net.xmilon.himproveme.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * Marker effect for the stunned affliction.
 * The server and client hooks consume this state to alter sprinting, FOV and controls.
 */
public final class StunnedStatusEffect extends StatusEffect {
    public StunnedStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xB9C27D);
    }

    /**
     * Disables vanilla per-tick effect handling because the custom perk manager owns the timing.
     */
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }
}
