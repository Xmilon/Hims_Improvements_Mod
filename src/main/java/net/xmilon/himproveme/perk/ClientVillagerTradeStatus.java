package net.xmilon.himproveme.perk;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class ClientVillagerTradeStatus {
    private static final List<OfferStatus> OFFER_STATUSES = new ArrayList<>();
    private static int syncId = -1;
    private static long totalCooldownTicks;

    private ClientVillagerTradeStatus() {
    }

    public static synchronized void setFromNbt(@Nullable NbtCompound data) {
        OFFER_STATUSES.clear();
        syncId = -1;
        totalCooldownTicks = 0L;
        if (data == null) {
            return;
        }

        syncId = data.getInt("SyncId");
        totalCooldownTicks = data.getLong("TotalCooldownTicks");

        NbtList offers = data.getList("Offers", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < offers.size(); i++) {
            NbtCompound offerData = offers.getCompound(i);
            OFFER_STATUSES.add(new OfferStatus(
                    offerData.getInt("RemainingTrades"),
                    offerData.getLong("CooldownLeftTicks")
            ));
        }
    }

    public static synchronized @Nullable OfferStatus getStatus(int expectedSyncId, int offerIndex) {
        if (syncId != expectedSyncId || offerIndex < 0 || offerIndex >= OFFER_STATUSES.size()) {
            return null;
        }
        return OFFER_STATUSES.get(offerIndex);
    }

    public static synchronized long getTotalCooldownTicks(int expectedSyncId) {
        return syncId == expectedSyncId ? totalCooldownTicks : 0L;
    }

    public record OfferStatus(int remainingTrades, long cooldownLeftTicks) {
    }
}
