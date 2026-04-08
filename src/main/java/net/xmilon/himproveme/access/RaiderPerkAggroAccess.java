package net.xmilon.himproveme.access;

import net.minecraft.entity.player.PlayerEntity;

public interface RaiderPerkAggroAccess {
    void himproveme$angerAt(PlayerEntity player, int durationTicks);

    boolean himproveme$isAngryAt(PlayerEntity player);

    boolean himproveme$hasActivePillagerAnger();

    void himproveme$clearPillagerAnger();
}
