package net.xmilon.himproveme.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * Marker effect for the bleeding affliction.
 * The damage-over-time logic lives in the affliction manager so the icon can stay purely cosmetic.
 */
public final class BleedingStatusEffect extends StatusEffect {
    public BleedingStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xA51F24);
    }

    /**
     * Disables vanilla per-tick effect handling because the custom perk manager owns the timing.
     */
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }
}
