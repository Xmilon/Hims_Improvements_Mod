package net.xmilon.himproveme.world.ancientcity;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores the Ancient Cities that have already handed out their guaranteed Warden Token chest reward.
 */
public final class AncientCityPersistentState extends PersistentState {
    private static final String STATE_ID = "himproveme_ancient_city";
    private static final String GUARANTEED_TOKEN_KEY = "GuaranteedTokenCities";
    private static final Type<AncientCityPersistentState> TYPE = new Type<>(
            AncientCityPersistentState::new,
            AncientCityPersistentState::fromNbt,
            DataFixTypes.LEVEL
    );

    private final Set<String> guaranteedTokenCities = new HashSet<>();

    /**
     * Reads or creates the per-dimension state that keeps Ancient City token guarantees stable.
     */
    public static AncientCityPersistentState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE, STATE_ID);
    }

    /**
     * Rehydrates the token-grant bookkeeping from world save data.
     */
    private static AncientCityPersistentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        AncientCityPersistentState state = new AncientCityPersistentState();
        if (!nbt.contains(GUARANTEED_TOKEN_KEY)) {
            return state;
        }

        NbtList list = nbt.getList(GUARANTEED_TOKEN_KEY, NbtElement.STRING_TYPE);
        for (int index = 0; index < list.size(); index++) {
            state.guaranteedTokenCities.add(list.getString(index));
        }
        return state;
    }

    /**
     * Persists the set of Ancient Cities that have already granted their guaranteed token.
     */
    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList list = new NbtList();
        for (String value : this.guaranteedTokenCities) {
            list.add(NbtString.of(value));
        }
        nbt.put(GUARANTEED_TOKEN_KEY, list);
        return nbt;
    }

    /**
     * Returns true when the city has already used its single guaranteed chest token.
     */
    public boolean hasGrantedToken(AncientCityKey cityKey) {
        return this.guaranteedTokenCities.contains(cityKey.asStorageKey());
    }

    /**
     * Marks the city as having consumed its guaranteed token reward and schedules a save.
     */
    public void markTokenGranted(AncientCityKey cityKey) {
        if (this.guaranteedTokenCities.add(cityKey.asStorageKey())) {
            this.markDirty();
        }
    }
}
