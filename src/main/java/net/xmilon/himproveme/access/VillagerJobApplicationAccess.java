package net.xmilon.himproveme.access;

import net.minecraft.util.math.Vec3d;

public interface VillagerJobApplicationAccess {
    void himproveme$startJobApplicationPanic(Vec3d sourcePos);

    int himproveme$getJobApplicationPanicTicks();

    void himproveme$setJobApplicationPanicTicks(int ticks);

    Vec3d himproveme$getJobApplicationPanicSource();
}
