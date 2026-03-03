package net.xmilon.himproveme.chest;

import java.util.UUID;

public interface ChestLockDataAccess {
    boolean himproveme$isLocked();

    UUID himproveme$getOwnerUuid();

    String himproveme$getOwnerName();

    void himproveme$setLockState(boolean locked, UUID ownerUuid, String ownerName);
}
