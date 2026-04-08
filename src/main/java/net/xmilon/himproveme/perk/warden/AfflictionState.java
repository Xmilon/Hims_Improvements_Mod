package net.xmilon.himproveme.perk.warden;

import net.minecraft.util.math.random.Random;

import java.util.UUID;

/**
 * Mutable ephemeral server-side state for one afflicted entity.
 */
public final class AfflictionState {
    private final UUID ownerUuid;
    private final UUID targetUuid;
    private final AfflictionProfile profile;
    private final byte[] movementMapping = new byte[]{0, 1, 2, 3};
    private float barPercent;
    private long lastHitTick;
    private long nextDotTick;
    private long nextControlShuffleTick;
    private boolean effectActive;
    private boolean invertForwardAxis;
    private boolean invertSidewaysAxis;
    private boolean sepukuTriggered;
    private int sepukuTicksRemaining;

    /**
     * Initializes the state for a newly afflicted target.
     */
    public AfflictionState(UUID ownerUuid, UUID targetUuid, AfflictionProfile profile, long currentTick) {
        this.ownerUuid = ownerUuid;
        this.targetUuid = targetUuid;
        this.profile = profile;
        this.lastHitTick = currentTick;
        this.nextDotTick = currentTick + 100L;
        this.nextControlShuffleTick = currentTick + 20L;
    }

    /**
     * Returns true when another attacker/profile combination should be allowed to take ownership of this bar.
     */
    public boolean canBeReplaced(UUID newOwnerUuid, AfflictionProfile newProfile, long currentTick) {
        if (this.sepukuTriggered) {
            return false;
        }
        if (this.ownerUuid.equals(newOwnerUuid) && this.profile == newProfile) {
            return true;
        }
        return this.barPercent < 15.0F || currentTick - this.lastHitTick > 60L;
    }

    /**
     * Adds new progress to the affliction bar and refreshes the last-hit timestamp.
     */
    public void addBar(float amount, long currentTick) {
        this.barPercent = Math.min(100.0F, this.barPercent + amount);
        this.lastHitTick = currentTick;
    }

    /**
     * Passively decays the affliction bar between hits.
     */
    public void decay(float amount) {
        this.barPercent = Math.max(0.0F, this.barPercent - amount);
    }

    /**
     * Rolls a fresh WASD permutation for the stunned full-bar chaos state.
     */
    public void shuffleMovementMapping(Random random) {
        for (int index = this.movementMapping.length - 1; index > 0; index--) {
            int swapIndex = random.nextInt(index + 1);
            byte current = this.movementMapping[index];
            this.movementMapping[index] = this.movementMapping[swapIndex];
            this.movementMapping[swapIndex] = current;
        }
    }

    /**
     * Chooses a single movement axis to invert for the Frenzy pre-sepuku phase.
     */
    public void rollFrenzyAxis(Random random) {
        this.invertForwardAxis = false;
        this.invertSidewaysAxis = false;
        if (random.nextBoolean()) {
            this.invertForwardAxis = true;
        } else {
            this.invertSidewaysAxis = true;
        }
    }

    /**
     * Resets player-side movement disruption when the state no longer needs it.
     */
    public void clearControlDisruption() {
        this.movementMapping[0] = 0;
        this.movementMapping[1] = 1;
        this.movementMapping[2] = 2;
        this.movementMapping[3] = 3;
        this.invertForwardAxis = false;
        this.invertSidewaysAxis = false;
    }

    /**
     * Starts the sepuku countdown once Frenzy reaches 100%.
     */
    public void beginSepuku(int durationTicks) {
        this.sepukuTriggered = true;
        this.sepukuTicksRemaining = durationTicks;
        this.effectActive = true;
        this.barPercent = 100.0F;
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public UUID getTargetUuid() {
        return this.targetUuid;
    }

    public AfflictionProfile getProfile() {
        return this.profile;
    }

    public float getBarPercent() {
        return this.barPercent;
    }

    public long getLastHitTick() {
        return this.lastHitTick;
    }

    public long getNextDotTick() {
        return this.nextDotTick;
    }

    public void setNextDotTick(long nextDotTick) {
        this.nextDotTick = nextDotTick;
    }

    public long getNextControlShuffleTick() {
        return this.nextControlShuffleTick;
    }

    public void setNextControlShuffleTick(long nextControlShuffleTick) {
        this.nextControlShuffleTick = nextControlShuffleTick;
    }

    public boolean isEffectActive() {
        return this.effectActive;
    }

    public void setEffectActive(boolean effectActive) {
        this.effectActive = effectActive;
    }

    public boolean isInvertForwardAxis() {
        return this.invertForwardAxis;
    }

    public boolean isInvertSidewaysAxis() {
        return this.invertSidewaysAxis;
    }

    public boolean isSepukuTriggered() {
        return this.sepukuTriggered;
    }

    public int getSepukuTicksRemaining() {
        return this.sepukuTicksRemaining;
    }

    public void decrementSepukuTicks() {
        this.sepukuTicksRemaining = Math.max(0, this.sepukuTicksRemaining - 1);
    }

    public byte[] getMovementMapping() {
        return this.movementMapping;
    }
}
