package net.xmilon.himproveme.leveling;

import net.minecraft.nbt.NbtCompound;

public final class ClientLevelingState {
    private static Snapshot snapshot = Snapshot.empty();

    private ClientLevelingState() {
    }

    public static synchronized Snapshot getSnapshot() {
        return snapshot;
    }

    public static synchronized void setFromNbt(NbtCompound nbt) {
        snapshot = Snapshot.fromNbt(nbt);
    }

    public record Snapshot(
            int level,
            long currentLevelXp,
            long nextLevelXp,
            long totalLevelingXp,
            long totalPlayTicks,
            int maxLevel,
            boolean enabled
    ) {
        private static Snapshot empty() {
            return new Snapshot(1, 0L, 0L, 0L, 0L, LevelingManager.DEFAULT_MAX_LEVEL, true);
        }

        private static Snapshot fromNbt(NbtCompound nbt) {
            return new Snapshot(
                    Math.max(1, nbt.getInt("Level")),
                    Math.max(0L, nbt.getLong("CurrentLevelXp")),
                    Math.max(0L, nbt.getLong("NextLevelXp")),
                    Math.max(0L, nbt.getLong("TotalLevelingXp")),
                    Math.max(0L, nbt.getLong("TotalPlayTicks")),
                    Math.max(1, nbt.getInt("MaxLevel")),
                    nbt.getBoolean("Enabled")
            );
        }

        public boolean isMaxLevel() {
            return this.level >= this.maxLevel || this.nextLevelXp <= 0L;
        }
    }
}
