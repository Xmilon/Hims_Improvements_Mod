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

        // Fireproof: burn-time reduction is handled directly in the entity fire-tick mixin.
        register(
                Identifier.of(HimProveMe.MOD_ID, "fireproof"),
                new FunctionBundle(
                        player -> {
                            // No unlock burst needed.
                        },
                        (player, level) -> {
                            // No ticking function; the fire-tick hook checks the perk directly.
                        }
                )
        );

        register(
                Identifier.of(HimProveMe.MOD_ID, "friendly_piglins"),
                new FunctionBundle(
                        player -> {
                            // No unlock burst needed; piglin AI hooks check the perk directly.
                        },
                        (player, level) -> {
                            // No ticking function.
                        }
                )
        );

        register(
                Identifier.of(HimProveMe.MOD_ID, "piglin_bartering"),
                new FunctionBundle(
                        player -> {
                            // No unlock burst needed; piglin interaction hooks check the perk directly.
                        },
                        (player, level) -> {
                            // No ticking function.
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

        // Muffled Steps: movement behavior is handled directly in entity mixins.
        register(
                Identifier.of(HimProveMe.MOD_ID, "muffled_steps"),
                new FunctionBundle(
                        player -> {
                            // No unlock burst needed.
                        },
                        (player, level) -> {
                            // No ticking function; entity hooks check the perk state directly.
                        }
                )
        );

        // Sculk Invisibility: vibration suppression is handled directly in the world game-event mixin.
        register(
                Identifier.of(HimProveMe.MOD_ID, "sculk_invisibility"),
                new FunctionBundle(
                        player -> {
                            // No unlock burst needed.
                        },
                        (player, level) -> {
                            // No ticking function; world game-event hooks check the perk state directly.
                        }
                )
        );

        register(
                Identifier.of(HimProveMe.MOD_ID, "lucky_totems"),
                new FunctionBundle(
                        player -> {
                            // No unlock burst needed; illager loot hooks check the perk directly.
                        },
                        (player, level) -> {
                            // No ticking function.
                        }
                )
        );

        register(
                Identifier.of(HimProveMe.MOD_ID, "friendly_pillagers"),
                new FunctionBundle(
                        player -> {
                            // No unlock burst needed; raider AI hooks check the perk directly.
                        },
                        (player, level) -> {
                            // No ticking function.
                        }
                )
        );

        register(
                Identifier.of(HimProveMe.MOD_ID, "domesticated_ravanger"),
                new FunctionBundle(
                        player -> {
                            // No unlock burst needed; ravager interactions check the perk directly.
                        },
                        (player, level) -> {
                            // No ticking function.
                        }
                )
        );

        register(
                Identifier.of(HimProveMe.MOD_ID, "job_application"),
                new FunctionBundle(
                        player -> {
                            // No unlock burst needed; nitwit interactions check the perk directly.
                        },
                        (player, level) -> {
                            // No ticking function.
                        }
                )
        );

        register(
                Identifier.of(HimProveMe.MOD_ID, "market_connections"),
                new FunctionBundle(
                        player -> {
                            // No unlock burst needed; villager trading hooks check the perk directly.
                        },
                        (player, level) -> {
                            // No ticking function.
                        }
                )
        );

        register(
                Identifier.of(HimProveMe.MOD_ID, "travelling_treasures"),
                new FunctionBundle(
                        player -> {
                            // No unlock burst needed; wandering trader hooks check the perk directly.
                        },
                        (player, level) -> {
                            // No ticking function.
                        }
                )
        );

        register(
                Identifier.of(HimProveMe.MOD_ID, "acrobat"),
                new FunctionBundle(
                        player -> {
                            // No unlock burst needed; the mobility ability is handled by the acrobat helper.
                        },
                        (player, level) -> {
                            // No passive tick hook; activation is client-input driven.
                        }
                )
        );

        register(Identifier.of(HimProveMe.MOD_ID, "warden_bleeding"), FunctionBundle.empty());
        register(Identifier.of(HimProveMe.MOD_ID, "warden_stunned"), FunctionBundle.empty());
        register(Identifier.of(HimProveMe.MOD_ID, "warden_frenzy"), FunctionBundle.empty());
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
