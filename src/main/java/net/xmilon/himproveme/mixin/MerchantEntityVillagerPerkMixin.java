package net.xmilon.himproveme.mixin;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtLongArray;
import net.xmilon.himproveme.access.MerchantPerkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantEntity.class)
public abstract class MerchantEntityVillagerPerkMixin implements MerchantPerkAccess {
    @Unique
    private static final String HIMPROVEME_TRADE_COOLDOWNS_KEY = "HimProveMeTradeCooldowns";
    @Unique
    private static final String HIMPROVEME_BOOSTED_OFFERS_KEY = "HimProveMeBoostedOffers";
    @Unique
    private static final String HIMPROVEME_ENHANCED_TRADER_OFFERS_KEY = "HimProveMeEnhancedTraderOffers";

    @Unique
    private final LongArrayList himproveme$tradeCooldownEndTicks = new LongArrayList();
    @Unique
    private int himproveme$boostedOfferCount;
    @Unique
    private boolean himproveme$enhancedTraderOffers;

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void himproveme$writeVillagerPerkData(NbtCompound nbt, CallbackInfo ci) {
        nbt.put(HIMPROVEME_TRADE_COOLDOWNS_KEY, new NbtLongArray(himproveme$tradeCooldownEndTicks.toLongArray()));
        nbt.putInt(HIMPROVEME_BOOSTED_OFFERS_KEY, himproveme$boostedOfferCount);
        nbt.putBoolean(HIMPROVEME_ENHANCED_TRADER_OFFERS_KEY, himproveme$enhancedTraderOffers);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void himproveme$readVillagerPerkData(NbtCompound nbt, CallbackInfo ci) {
        himproveme$tradeCooldownEndTicks.clear();
        if (nbt.contains(HIMPROVEME_TRADE_COOLDOWNS_KEY, NbtElement.LONG_ARRAY_TYPE)) {
            long[] cooldowns = nbt.getLongArray(HIMPROVEME_TRADE_COOLDOWNS_KEY);
            for (long cooldown : cooldowns) {
                himproveme$tradeCooldownEndTicks.add(cooldown);
            }
        }
        himproveme$boostedOfferCount = nbt.getInt(HIMPROVEME_BOOSTED_OFFERS_KEY);
        himproveme$enhancedTraderOffers = nbt.getBoolean(HIMPROVEME_ENHANCED_TRADER_OFFERS_KEY);
    }

    @Override
    public long himproveme$getTradeCooldownEndTick(int offerIndex) {
        return offerIndex >= 0 && offerIndex < himproveme$tradeCooldownEndTicks.size()
                ? himproveme$tradeCooldownEndTicks.getLong(offerIndex)
                : 0L;
    }

    @Override
    public void himproveme$setTradeCooldownEndTick(int offerIndex, long endTick) {
        himproveme$resizeTradeCooldowns(offerIndex + 1);
        himproveme$tradeCooldownEndTicks.set(offerIndex, endTick);
    }

    @Override
    public void himproveme$clearTradeCooldownEndTick(int offerIndex) {
        if (offerIndex >= 0 && offerIndex < himproveme$tradeCooldownEndTicks.size()) {
            himproveme$tradeCooldownEndTicks.set(offerIndex, 0L);
        }
    }

    @Override
    public void himproveme$resizeTradeCooldowns(int offerCount) {
        while (himproveme$tradeCooldownEndTicks.size() < offerCount) {
            himproveme$tradeCooldownEndTicks.add(0L);
        }
        while (himproveme$tradeCooldownEndTicks.size() > offerCount) {
            himproveme$tradeCooldownEndTicks.removeLong(himproveme$tradeCooldownEndTicks.size() - 1);
        }
    }

    @Override
    public int himproveme$getBoostedOfferCount() {
        return himproveme$boostedOfferCount;
    }

    @Override
    public void himproveme$setBoostedOfferCount(int count) {
        himproveme$boostedOfferCount = Math.max(0, count);
    }

    @Override
    public boolean himproveme$hasEnhancedTraderOffers() {
        return himproveme$enhancedTraderOffers;
    }

    @Override
    public void himproveme$setEnhancedTraderOffers(boolean enhanced) {
        himproveme$enhancedTraderOffers = enhanced;
    }
}
