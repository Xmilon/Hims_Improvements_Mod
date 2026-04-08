package net.xmilon.himproveme.access;

public interface MerchantPerkAccess {
    long himproveme$getTradeCooldownEndTick(int offerIndex);

    void himproveme$setTradeCooldownEndTick(int offerIndex, long endTick);

    void himproveme$clearTradeCooldownEndTick(int offerIndex);

    void himproveme$resizeTradeCooldowns(int offerCount);

    int himproveme$getBoostedOfferCount();

    void himproveme$setBoostedOfferCount(int count);

    boolean himproveme$hasEnhancedTraderOffers();

    void himproveme$setEnhancedTraderOffers(boolean enhanced);
}
