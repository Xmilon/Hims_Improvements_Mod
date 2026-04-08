package net.xmilon.himproveme.access;

import net.minecraft.util.Hand;

public interface EnderSpearSlashAccess {
    void himproveme$startEnderSpearSlash(Hand hand);

    boolean himproveme$isEnderSpearSlashActive(Hand hand);

    float himproveme$getEnderSpearSlashProgress(Hand hand, float tickDelta);
}
