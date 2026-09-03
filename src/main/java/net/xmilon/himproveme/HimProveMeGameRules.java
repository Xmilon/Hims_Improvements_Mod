package net.xmilon.himproveme;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public final class HimProveMeGameRules {
    public static final GameRules.Key<GameRules.BooleanRule> KEEP_XP_AFTER_DEATH =
            GameRuleRegistry.register("keepXpAfterDeath", GameRules.Category.PLAYER,
                    GameRuleFactory.createBooleanRule(false));

    private HimProveMeGameRules() {}

    public static void register() {
        // Calling this method triggers the static initializer above
    }
}