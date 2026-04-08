package net.xmilon.himproveme.perk.warden;

/**
 * Shared tuning values for the three Warden affliction profiles.
 */
public enum AfflictionProfile {
    BLEEDING(65.0F, 58.0F, 30.0F, 0.018F, 0.045F),
    STUNNED(60.0F, 45.0F, 30.0F, 0.022F, 0.060F),
    FRENZY(100.0F, 5.0F, 15.0F, 0.012F, 0.020F);

    private final float triggerThreshold;
    private final float releaseThreshold;
    private final float gainScale;
    private final float idleDecayPerTick;
    private final float activeDecayPerTick;

    AfflictionProfile(float triggerThreshold, float releaseThreshold, float gainScale, float idleDecayPerTick, float activeDecayPerTick) {
        this.triggerThreshold = triggerThreshold;
        this.releaseThreshold = releaseThreshold;
        this.gainScale = gainScale;
        this.idleDecayPerTick = idleDecayPerTick;
        this.activeDecayPerTick = activeDecayPerTick;
    }

    /**
     * Converts actual combat damage into a percentage increase on the hidden affliction bar.
     */
    public float computeGain(float dealtDamage, float targetMaxHealth, boolean critical) {
        if (dealtDamage <= 0.0F || targetMaxHealth <= 0.0F) {
            return 0.0F;
        }

        float criticalMultiplier = critical ? 1.35F : 1.0F;
        return (dealtDamage * criticalMultiplier / targetMaxHealth) * this.gainScale;
    }

    /**
     * Returns true once the bar should start applying the profile's main effect.
     */
    public boolean shouldTrigger(float bar) {
        return bar >= this.triggerThreshold;
    }

    /**
     * Returns true while the effect should stay active after it has already triggered.
     */
    public boolean shouldRemainActive(float bar) {
        return this == FRENZY ? bar > this.releaseThreshold : bar >= this.releaseThreshold;
    }

    /**
     * Returns the passive decay rate for the current bar state.
     */
    public float getDecayPerTick(boolean effectActive) {
        return effectActive ? this.activeDecayPerTick : this.idleDecayPerTick;
    }

    public float getTriggerThreshold() {
        return this.triggerThreshold;
    }
}
