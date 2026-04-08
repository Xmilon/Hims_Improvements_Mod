package net.xmilon.himproveme.access;

public interface DualWieldAttackAccess {
    int himproveme$getOffhandLastAttackedTicks();

    void himproveme$resetOffhandLastAttackedTicks();

    void himproveme$startOffhandSwing();

    boolean himproveme$isOffhandSwingActive();

    float himproveme$getOffhandSwingProgress(float tickDelta);

    void himproveme$setBashAttackLockTicks(int ticks);

    boolean himproveme$hasBashAttackLock();
}
