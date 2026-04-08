package net.xmilon.himproveme.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.xmilon.himproveme.access.LevelingStateHolder;
import net.xmilon.himproveme.leveling.LevelingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityLevelingDataMixin implements LevelingStateHolder {
    @Unique
    private static final String HIMPROVEME_LEVELING_KEY = "HimProveMeLeveling";

    @Unique
    private LevelingState himproveme$levelingState = new LevelingState();

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void himproveme$writeLevelingData(NbtCompound nbt, CallbackInfo ci) {
        nbt.put(HIMPROVEME_LEVELING_KEY, this.himproveme$levelingState.toNbt());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void himproveme$readLevelingData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(HIMPROVEME_LEVELING_KEY, NbtElement.COMPOUND_TYPE)) {
            this.himproveme$levelingState = LevelingState.fromNbt(nbt.getCompound(HIMPROVEME_LEVELING_KEY));
            return;
        }
        this.himproveme$levelingState = new LevelingState();
    }

    @Override
    public LevelingState himproveme$getLevelingState() {
        return this.himproveme$levelingState;
    }

    @Override
    public void himproveme$setLevelingState(LevelingState state) {
        this.himproveme$levelingState = state == null ? new LevelingState() : state;
    }
}
