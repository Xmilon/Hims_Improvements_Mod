package net.xmilon.himproveme;

import net.minecraft.world.GameRules;

import java.lang.reflect.Method;

public final class HimProveMeGameRules {
    public static final GameRules.Key<GameRules.BooleanRule> KEEP_XP_AFTER_DEATH = registerRule();

    private HimProveMeGameRules() {}

    public static void register() {}

    @SuppressWarnings("unchecked")
    private static GameRules.Key<GameRules.BooleanRule> registerRule() {
        try {
            Method create = GameRules.BooleanRule.class.getDeclaredMethod("create", boolean.class);
            create.setAccessible(true);
            Object ruleType = create.invoke(null, false);

            Method register = null;
            for (Method m : GameRules.class.getDeclaredMethods()) {
                if (m.getName().equals("register") && m.getParameterCount() == 3) {
                    register = m;
                    break;
                }
            }

            if (register == null) {
                throw new RuntimeException("Could not find GameRules.register method.");
            }

            register.setAccessible(true);
            return (GameRules.Key<GameRules.BooleanRule>) register.invoke(null,
                    "keepXpAfterDeath", GameRules.Category.PLAYER, ruleType);

        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("Unable to initialize KeepXPAfterDeath gamerule.", ex);
        }
    }
}