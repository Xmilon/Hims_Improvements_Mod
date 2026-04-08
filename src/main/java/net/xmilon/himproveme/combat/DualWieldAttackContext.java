package net.xmilon.himproveme.combat;

import net.minecraft.entity.player.PlayerEntity;

public final class DualWieldAttackContext {
    private static final ThreadLocal<PlayerEntity> CURRENT_OFFHAND_ATTACKER = new ThreadLocal<>();

    private DualWieldAttackContext() {
    }

    public static boolean isOffhandAttack(PlayerEntity player) {
        return CURRENT_OFFHAND_ATTACKER.get() == player;
    }

    public static void run(PlayerEntity player, Runnable action) {
        PlayerEntity previousPlayer = CURRENT_OFFHAND_ATTACKER.get();
        CURRENT_OFFHAND_ATTACKER.set(player);
        try {
            action.run();
        } finally {
            if (previousPlayer == null) {
                CURRENT_OFFHAND_ATTACKER.remove();
            } else {
                CURRENT_OFFHAND_ATTACKER.set(previousPlayer);
            }
        }
    }
}
