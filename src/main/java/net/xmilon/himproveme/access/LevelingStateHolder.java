package net.xmilon.himproveme.access;

import net.xmilon.himproveme.leveling.LevelingState;

public interface LevelingStateHolder {
    LevelingState himproveme$getLevelingState();

    void himproveme$setLevelingState(LevelingState state);
}
