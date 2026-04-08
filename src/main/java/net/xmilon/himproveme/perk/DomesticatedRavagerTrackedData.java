package net.xmilon.himproveme.perk;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.raid.RaiderEntity;

import java.util.Optional;
import java.util.UUID;

public final class DomesticatedRavagerTrackedData {
    public static final TrackedData<Boolean> DOMESTICATED =
            DataTracker.registerData(RaiderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Optional<UUID>> OWNER_UUID =
            DataTracker.registerData(RaiderEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    private DomesticatedRavagerTrackedData() {
    }
}
