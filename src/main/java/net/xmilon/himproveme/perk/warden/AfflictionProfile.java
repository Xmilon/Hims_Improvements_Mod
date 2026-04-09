package net.xmilon.himproveme.perk.warden;

/**
 * Shared tuning values for the three Warden affliction profiles.
 */
public enum AfflictionProfile {
    BLEEDING(40.0F, 28.0F, 0.010F, 0.010F),
    STUNNED(35.0F, 24.0F, 0.010F, 0.010F),
    FRENZY(60.0F, 5.0F, 0.010F, 0.010F);

    private static final float MIN_TARGET_HEALTH_FOR_FILL_SCALING = 20.0F;
    private static final float MAX_TARGET_HEALTH_FOR_FILL_SCALING = 250.0F;
    private static final float MIN_CRITICAL_HITS_TO_FILL = 3.0F;
    private static final float MAX_CRITICAL_HITS_TO_FILL = 6.0F;
    private static final float REFERENCE_COMBAT_DAMAGE = 8.0F;
    private static final float MIN_DAMAGE_FACTOR = 0.25F;
    private static final float MAX_DAMAGE_FACTOR = 1.25F;
    private static final float NON_CRITICAL_HIT_FACTOR = 0.75F;

    private final float triggerThreshold;
    private final float releaseThreshold;
    private final float idleDecayPerTick;
    private final float activeDecayPerTick;

    AfflictionProfile(float triggerThreshold, float releaseThreshold, float idleDecayPerTick, float activeDecayPerTick) {
        this.triggerThreshold = triggerThreshold;
        this.releaseThreshold = releaseThreshold;
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

        float clampedHealth = Math.max(MIN_TARGET_HEALTH_FOR_FILL_SCALING, Math.min(targetMaxHealth, MAX_TARGET_HEALTH_FOR_FILL_SCALING));
        float healthProgress = (clampedHealth - MIN_TARGET_HEALTH_FOR_FILL_SCALING)
                / (MAX_TARGET_HEALTH_FOR_FILL_SCALING - MIN_TARGET_HEALTH_FOR_FILL_SCALING);
        float criticalHitsToFill = MIN_CRITICAL_HITS_TO_FILL
                + (MAX_CRITICAL_HITS_TO_FILL - MIN_CRITICAL_HITS_TO_FILL) * healthProgress;
        float baseGain = 100.0F / criticalHitsToFill;
        float damageFactor = Math.max(MIN_DAMAGE_FACTOR, Math.min(dealtDamage / REFERENCE_COMBAT_DAMAGE, MAX_DAMAGE_FACTOR));
        float hitTypeFactor = critical ? 1.0F : NON_CRITICAL_HIT_FACTOR;

        return baseGain * damageFactor * hitTypeFactor;
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
