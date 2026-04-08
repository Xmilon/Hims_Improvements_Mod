package net.xmilon.himproveme.command;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.xmilon.himproveme.leveling.LevelingConfigState;
import net.xmilon.himproveme.leveling.LevelingManager;
import net.xmilon.himproveme.leveling.LevelingState;

import java.util.Collection;

public final class LevelingCommandFunctions {
    private LevelingCommandFunctions() {
    }

    public static int showInfo(ServerCommandSource source) {
        LevelingConfigState config = LevelingManager.getConfig(source.getServer());
        source.sendFeedback(
                () -> Text.literal("Leveling is " + (config.isEnabled() ? "enabled" : "disabled") + ". Max level: " + config.getMaxLevel() + "."),
                false
        );

        if (source.getEntity() instanceof ServerPlayerEntity player) {
            LevelingState state = LevelingManager.getState(player);
            long needed = LevelingManager.getXpNeededForNextLevel(player);
            source.sendFeedback(
                    () -> Text.literal("Current level: " + state.getLevel()
                            + " | Level XP: " + state.getCurrentLevelXp()
                            + (needed > 0L ? " / " + needed : " / MAX")
                            + " | Total leveling XP: " + state.getTotalLevelingXp()),
                    false
            );
        }

        return 1;
    }

    public static int setEnabled(ServerCommandSource source, boolean enabled) {
        LevelingConfigState config = LevelingManager.getConfig(source.getServer());
        config.setEnabled(enabled);
        LevelingManager.syncAll(source.getServer());
        source.sendFeedback(() -> Text.literal("Leveling " + (enabled ? "enabled." : "disabled.")), true);
        return 1;
    }

    public static int setMaxLevel(ServerCommandSource source, int maxLevel) {
        LevelingConfigState config = LevelingManager.getConfig(source.getServer());
        config.setMaxLevel(maxLevel);
        LevelingManager.clampAndSyncAllPlayers(source.getServer());
        source.sendFeedback(() -> Text.literal("Max leveling level set to " + config.getMaxLevel() + "."), true);
        return 1;
    }

    public static int setLevel(ServerCommandSource source, Collection<ServerPlayerEntity> targets, int level) {
        for (ServerPlayerEntity player : targets) {
            LevelingManager.setLevel(player, level);
        }

        int count = targets.size();
        source.sendFeedback(() -> Text.literal("Set leveling level for " + count + " player(s)."), true);
        return count;
    }

    public static int addXp(ServerCommandSource source, Collection<ServerPlayerEntity> targets, int amount) {
        for (ServerPlayerEntity player : targets) {
            LevelingManager.addAdminXp(player, amount);
        }

        int count = targets.size();
        source.sendFeedback(() -> Text.literal("Added " + amount + " leveling XP to " + count + " player(s)."), true);
        return count;
    }

    public static int giveBook(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        for (ServerPlayerEntity player : targets) {
            LevelingManager.grantGuideBook(player);
        }

        int count = targets.size();
        source.sendFeedback(() -> Text.literal("Gave the leveling guide book to " + count + " player(s)."), true);
        return count;
    }

    public static int claimGuideBook(ServerPlayerEntity player) {
        return switch (LevelingManager.claimGuideBook(player)) {
            case GIVEN -> 1;
            case TOO_SOON -> 0;
        };
    }
}
