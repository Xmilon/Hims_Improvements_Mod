package net.xmilon.himproveme.perk;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayDeque;
import java.util.Deque;

public final class SculkInvisibilityContext {
    private static final ThreadLocal<Deque<ServerPlayerEntity>> ACTIVE_PLAYERS =
            ThreadLocal.withInitial(ArrayDeque::new);

    private SculkInvisibilityContext() {
    }

    public static void push(ServerPlayerEntity player) {
        ACTIVE_PLAYERS.get().push(player);
    }

    public static void pop(ServerPlayerEntity player) {
        Deque<ServerPlayerEntity> activePlayers = ACTIVE_PLAYERS.get();
        if (activePlayers.isEmpty()) {
            return;
        }

        if (activePlayers.peek() == player) {
            activePlayers.pop();
        } else {
            activePlayers.removeFirstOccurrence(player);
        }

        if (activePlayers.isEmpty()) {
            ACTIVE_PLAYERS.remove();
        }
    }

    public static boolean isActive() {
        return !ACTIVE_PLAYERS.get().isEmpty();
    }
}
