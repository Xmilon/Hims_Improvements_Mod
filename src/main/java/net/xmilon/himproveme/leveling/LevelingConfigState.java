package net.xmilon.himproveme.leveling;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

public final class LevelingConfigState extends PersistentState {
    private static final String STATE_ID = "himproveme_leveling";
    private static final String ENABLED_KEY = "Enabled";
    private static final String MAX_LEVEL_KEY = "MaxLevel";

    private static final Type<LevelingConfigState> TYPE = new Type<>(
            LevelingConfigState::new,
            LevelingConfigState::fromNbt,
            DataFixTypes.LEVEL
    );

    private boolean enabled = true;
    private int maxLevel = LevelingManager.DEFAULT_MAX_LEVEL;

    public static LevelingConfigState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, STATE_ID);
    }

    private static LevelingConfigState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        LevelingConfigState state = new LevelingConfigState();
        state.enabled = !nbt.contains(ENABLED_KEY) || nbt.getBoolean(ENABLED_KEY);
        state.maxLevel = LevelingManager.clampMaxLevel(nbt.contains(MAX_LEVEL_KEY) ? nbt.getInt(MAX_LEVEL_KEY) : LevelingManager.DEFAULT_MAX_LEVEL);
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putBoolean(ENABLED_KEY, this.enabled);
        nbt.putInt(MAX_LEVEL_KEY, this.maxLevel);
        return nbt;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        this.markDirty();
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        int clamped = LevelingManager.clampMaxLevel(maxLevel);
        if (this.maxLevel == clamped) {
            return;
        }
        this.maxLevel = clamped;
        this.markDirty();
    }
}
