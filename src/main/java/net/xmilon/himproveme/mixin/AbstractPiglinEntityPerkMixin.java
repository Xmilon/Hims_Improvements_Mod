package net.xmilon.himproveme.mixin;

import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.village.TradeOfferList;
import net.xmilon.himproveme.access.PiglinTradeAccess;
import net.xmilon.himproveme.perk.NetherPerkHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractPiglinEntity.class)
public abstract class AbstractPiglinEntityPerkMixin implements PiglinTradeAccess {
    @Unique
    private TradeOfferList himproveme$tradeOffers = new TradeOfferList();

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void himproveme$writePiglinTradeOffers(NbtCompound nbt, CallbackInfo ci) {
        NetherPerkHelper.writeTradeOffers((AbstractPiglinEntity) (Object) this, nbt, this);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void himproveme$readPiglinTradeOffers(NbtCompound nbt, CallbackInfo ci) {
        NetherPerkHelper.readTradeOffers((AbstractPiglinEntity) (Object) this, nbt, this);
    }

    @Override
    public TradeOfferList himproveme$getTradeOffers() {
        return himproveme$tradeOffers;
    }

    @Override
    public void himproveme$setTradeOffers(TradeOfferList offers) {
        this.himproveme$tradeOffers = offers == null ? new TradeOfferList() : offers;
    }
}
