package net.xmilon.himproveme.leveling;

import net.minecraft.nbt.NbtCompound;

public final class LevelingState {
    private static final String LEVEL_KEY = "Level";
    private static final String CURRENT_XP_KEY = "CurrentXp";
    private static final String TOTAL_XP_KEY = "TotalXp";
    private static final String TOTAL_PLAY_TICKS_KEY = "TotalPlayTicks";
    private static final String PASSIVE_BUFFER_KEY = "PassiveTickBuffer";
    private static final String GUIDE_BOOK_RECEIVED_KEY = "GuideBookReceived";
    private static final String LAST_GUIDE_BOOK_DAY_KEY = "LastGuideBookDay";

    private int level = 1;
    private long currentLevelXp = 0L;
    private long totalLevelingXp = 0L;
    private long totalPlayTicks = 0L;
    private long passiveTickBuffer = 0L;
    private boolean guideBookReceived = false;
    private long lastGuideBookClaimDay = -1L;

    public static LevelingState fromNbt(NbtCompound nbt) {
        LevelingState state = new LevelingState();
        state.level = Math.max(1, nbt.getInt(LEVEL_KEY));
        state.currentLevelXp = Math.max(0L, nbt.getLong(CURRENT_XP_KEY));
        state.totalLevelingXp = Math.max(0L, nbt.getLong(TOTAL_XP_KEY));
        state.totalPlayTicks = Math.max(0L, nbt.getLong(TOTAL_PLAY_TICKS_KEY));
        state.passiveTickBuffer = Math.max(0L, nbt.getLong(PASSIVE_BUFFER_KEY));
        state.guideBookReceived = nbt.getBoolean(GUIDE_BOOK_RECEIVED_KEY);
        state.lastGuideBookClaimDay = nbt.contains(LAST_GUIDE_BOOK_DAY_KEY) ? nbt.getLong(LAST_GUIDE_BOOK_DAY_KEY) : -1L;
        return state;
    }

    public LevelingState copy() {
        LevelingState copy = new LevelingState();
        copy.level = this.level;
        copy.currentLevelXp = this.currentLevelXp;
        copy.totalLevelingXp = this.totalLevelingXp;
        copy.totalPlayTicks = this.totalPlayTicks;
        copy.passiveTickBuffer = this.passiveTickBuffer;
        copy.guideBookReceived = this.guideBookReceived;
        copy.lastGuideBookClaimDay = this.lastGuideBookClaimDay;
        return copy;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt(LEVEL_KEY, this.level);
        nbt.putLong(CURRENT_XP_KEY, this.currentLevelXp);
        nbt.putLong(TOTAL_XP_KEY, this.totalLevelingXp);
        nbt.putLong(TOTAL_PLAY_TICKS_KEY, this.totalPlayTicks);
        nbt.putLong(PASSIVE_BUFFER_KEY, this.passiveTickBuffer);
        nbt.putBoolean(GUIDE_BOOK_RECEIVED_KEY, this.guideBookReceived);
        nbt.putLong(LAST_GUIDE_BOOK_DAY_KEY, this.lastGuideBookClaimDay);
        return nbt;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public long getCurrentLevelXp() {
        return this.currentLevelXp;
    }

    public void setCurrentLevelXp(long currentLevelXp) {
        this.currentLevelXp = Math.max(0L, currentLevelXp);
    }

    public long getTotalLevelingXp() {
        return this.totalLevelingXp;
    }

    public void setTotalLevelingXp(long totalLevelingXp) {
        this.totalLevelingXp = Math.max(0L, totalLevelingXp);
    }

    public long getTotalPlayTicks() {
        return this.totalPlayTicks;
    }

    public void setTotalPlayTicks(long totalPlayTicks) {
        this.totalPlayTicks = Math.max(0L, totalPlayTicks);
    }

    public long getPassiveTickBuffer() {
        return this.passiveTickBuffer;
    }

    public void setPassiveTickBuffer(long passiveTickBuffer) {
        this.passiveTickBuffer = Math.max(0L, passiveTickBuffer);
    }

    public boolean hasReceivedGuideBook() {
        return this.guideBookReceived;
    }

    public void setGuideBookReceived(boolean guideBookReceived) {
        this.guideBookReceived = guideBookReceived;
    }

    public long getLastGuideBookClaimDay() {
        return this.lastGuideBookClaimDay;
    }

    public void setLastGuideBookClaimDay(long lastGuideBookClaimDay) {
        this.lastGuideBookClaimDay = lastGuideBookClaimDay;
    }
}
