package net.xmilon.himproveme.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.xmilon.himproveme.perk.PerkBookState;
import net.xmilon.himproveme.perk.PerkBookStateHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityPerkDataMixin implements PerkBookStateHolder {
    @Unique
    private static final String HIMPROVEME_PERKS_KEY = "HimProveMePerks";

    @Unique
    private PerkBookState himproveme$perkBookState = new PerkBookState();

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void himproveme$writePerkData(NbtCompound nbt, CallbackInfo ci) {
        nbt.put(HIMPROVEME_PERKS_KEY, himproveme$perkBookState.toNbt());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void himproveme$readPerkData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(HIMPROVEME_PERKS_KEY, NbtElement.COMPOUND_TYPE)) {
            himproveme$perkBookState = PerkBookState.fromNbt(nbt.getCompound(HIMPROVEME_PERKS_KEY));
            return;
        }
        himproveme$perkBookState = new PerkBookState();
    }

    @Override
    public PerkBookState himproveme$getPerkBookState() {
        return himproveme$perkBookState;
    }

    @Override
    public void himproveme$setPerkBookState(PerkBookState state) {
        this.himproveme$perkBookState = state == null ? new PerkBookState() : state;
    }
}
