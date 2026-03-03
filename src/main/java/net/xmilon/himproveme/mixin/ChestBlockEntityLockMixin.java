package net.xmilon.himproveme.mixin;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.xmilon.himproveme.chest.ChestLockDataAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityLockMixin implements ChestLockDataAccess {
    @Unique
    private static final String HIMPROVEME_LOCKED_KEY = "HimProveMeChestLocked";
    @Unique
    private static final String HIMPROVEME_OWNER_UUID_KEY = "HimProveMeChestOwnerUuid";
    @Unique
    private static final String HIMPROVEME_OWNER_NAME_KEY = "HimProveMeChestOwnerName";

    @Unique
    private boolean himproveme$locked;
    @Unique
    private UUID himproveme$ownerUuid;
    @Unique
    private String himproveme$ownerName;

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void himproveme$readLockData(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        himproveme$locked = nbt.getBoolean(HIMPROVEME_LOCKED_KEY);
        if (nbt.contains(HIMPROVEME_OWNER_UUID_KEY)) {
            himproveme$ownerUuid = nbt.getUuid(HIMPROVEME_OWNER_UUID_KEY);
        } else {
            himproveme$ownerUuid = null;
        }
        himproveme$ownerName = nbt.contains(HIMPROVEME_OWNER_NAME_KEY) ? nbt.getString(HIMPROVEME_OWNER_NAME_KEY) : null;
        if (!himproveme$locked) {
            himproveme$ownerUuid = null;
            himproveme$ownerName = null;
        }
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void himproveme$writeLockData(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        nbt.putBoolean(HIMPROVEME_LOCKED_KEY, himproveme$locked);
        if (himproveme$ownerUuid != null) {
            nbt.putUuid(HIMPROVEME_OWNER_UUID_KEY, himproveme$ownerUuid);
        }
        if (himproveme$ownerName != null && !himproveme$ownerName.isBlank()) {
            nbt.putString(HIMPROVEME_OWNER_NAME_KEY, himproveme$ownerName);
        }
    }

    @Override
    public boolean himproveme$isLocked() {
        return himproveme$locked;
    }

    @Override
    public UUID himproveme$getOwnerUuid() {
        return himproveme$ownerUuid;
    }

    @Override
    public String himproveme$getOwnerName() {
        return himproveme$ownerName;
    }

    @Override
    public void himproveme$setLockState(boolean locked, UUID ownerUuid, String ownerName) {
        himproveme$locked = locked;
        himproveme$ownerUuid = locked ? ownerUuid : null;
        himproveme$ownerName = locked ? ownerName : null;
    }
}
