package net.xmilon.himproveme.perk;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

import java.util.HashMap;
import java.util.Map;

/**
 * Central assembly point for perk gameplay functions.
 * Add all custom unlock/tick behavior wiring here.
 */
public final class PerkFunctions {
    @FunctionalInterface
    public interface UnlockFunction {
        void onUnlock(ServerPlayerEntity player);
    }

    @FunctionalInterface
    public interface ActiveTickFunction {
        void tick(ServerPlayerEntity player, int level);
    }

    public record FunctionBundle(UnlockFunction onUnlock, ActiveTickFunction onTick) {
        public static FunctionBundle empty() {
            return new FunctionBundle(player -> {
            }, (player, level) -> {
            });
        }
    }

    private static final Map<Identifier, FunctionBundle> FUNCTIONS = new HashMap<>();

    private PerkFunctions() {
    }

    public static void registerDefaults() {
        if (!FUNCTIONS.isEmpty()) {
            return;
        }

        // Lava Swim WIP: placeholder behavior hook.
        register(
                Identifier.of(HimProveMe.MOD_ID, "lava_swim"),
                new FunctionBundle(
                        player -> {
                            // Unlock hook intentionally empty for now.
                        },
                        (player, level) -> {
                            // Active/tick hook intentionally empty for now.
                        }
                )
        );

        // Fireproof: placeholder behavior hook.
        register(
                Identifier.of(HimProveMe.MOD_ID, "fireproof"),
                new FunctionBundle(
                        player -> {
                            // Unlock hook intentionally empty for now.
                        },
                        (player, level) -> {
                            // Active/tick hook intentionally empty for now.
                        }
                )
        );

        // Infinite Ender Pearl: placeholder behavior hook for the teleport perk.
        register(
                Identifier.of(HimProveMe.MOD_ID, "infinite_ender_pearl"),
                new FunctionBundle(
                        player -> {
                            // Unlock hook intentionally empty; ability lives in the item/ability handler.
                        },
                        (player, level) -> {
                            // No ticking behavior.
                        }
                )
        );

        // Ender Stare: keeps endermen calm when players stare at them.
        register(
                Identifier.of(HimProveMe.MOD_ID, "ender_stare"),
                new FunctionBundle(
                        player -> {
                            // No extra unlock behavior yet.
                        },
                        (player, level) -> {
                            // No ticking behavior; the mixin handles the calm stare.
                        }
                )
        );

        // Safe Levitation: the fall immunity is handled directly in the player fall mixin.
        register(
                Identifier.of(HimProveMe.MOD_ID, "safe_levitation"),
                new FunctionBundle(
                        player -> {
                            // No unlock burst needed; this perk passively arms during levitation.
                        },
                        (player, level) -> {
                            // No ticking function; the player mixin tracks levitation state directly.
                        }
                )
        );
    }

    public static void runUnlock(ServerPlayerEntity player, Identifier perkId) {
        FunctionBundle bundle = FUNCTIONS.get(perkId);
        if (bundle != null) {
            bundle.onUnlock.onUnlock(player);
        }
    }

    public static void runTick(ServerPlayerEntity player, Identifier perkId, int level) {
        FunctionBundle bundle = FUNCTIONS.get(perkId);
        if (bundle != null) {
            bundle.onTick.tick(player, level);
        }
    }

    public static FunctionBundle get(Identifier perkId) {
        return FUNCTIONS.getOrDefault(perkId, FunctionBundle.empty());
    }

    private static void register(Identifier perkId, FunctionBundle bundle) {
        FUNCTIONS.put(perkId, bundle);
    }
}
