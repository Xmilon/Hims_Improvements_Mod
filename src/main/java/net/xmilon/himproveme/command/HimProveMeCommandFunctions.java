package net.xmilon.himproveme.command;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.xmilon.himproveme.HimProveMeGameRules;
import net.xmilon.himproveme.item.custom.BreezeStaffConfig;
import net.xmilon.himproveme.network.perk.PerkBookNetworking;
import net.xmilon.himproveme.perk.PerkBookState;
import net.xmilon.himproveme.perk.PerkBookStateHolder;
import net.xmilon.himproveme.perk.PerkRegistry;

import java.util.Collection;

public final class HimProveMeCommandFunctions {
    private HimProveMeCommandFunctions() {
    }

    public static int removePerk(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Identifier perkId) {
        if (PerkRegistry.get(perkId) == null) {
            source.sendError(Text.literal("Unknown perk: " + perkId));
            return 0;
        }

        int changedPlayers = 0;
        for (ServerPlayerEntity player : targets) {
            PerkBookState state = ((PerkBookStateHolder) player).himproveme$getPerkBookState();
            boolean changed = state.removePerkFromAllInstances(perkId);
            PerkBookNetworking.sync(player);
            if (changed) {
                changedPlayers++;
            }
        }

        final int changedPlayersFinal = changedPlayers;
        final int targetCount = targets.size();
        source.sendFeedback(() -> Text.literal("Removed perk " + perkId + " from " + changedPlayersFinal + "/" + targetCount + " player(s)."), true);
        return changedPlayers;
    }

    public static int addPerk(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Identifier perkId) {
        if (PerkRegistry.get(perkId) == null) {
            source.sendError(Text.literal("Unknown perk: " + perkId));
            return 0;
        }

        int changedPlayers = 0;
        for (ServerPlayerEntity player : targets) {
            PerkBookState state = ((PerkBookStateHolder) player).himproveme$getPerkBookState();
            boolean changed = state.addPerkToAllInstances(perkId);
            PerkBookNetworking.sync(player);
            if (changed) {
                changedPlayers++;
            }
        }

        final int changedPlayersFinal = changedPlayers;
        final int targetCount = targets.size();
        source.sendFeedback(() -> Text.literal("Added perk " + perkId + " to " + changedPlayersFinal + "/" + targetCount + " player(s)."), true);
        return changedPlayers;
    }

    public static int resetPerks(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        for (ServerPlayerEntity player : targets) {
            PerkBookState state = ((PerkBookStateHolder) player).himproveme$getPerkBookState();
            state.resetAllPerks();
            PerkBookNetworking.sync(player);
        }

        int targetCount = targets.size();
        source.sendFeedback(() -> Text.literal("Reset all perks for " + targetCount + " player(s)."), true);
        return targetCount;
    }

    public static int setBreezeLinkXpCost(ServerCommandSource source, int levels) {
        BreezeStaffConfig.LINK_XP_COST = levels;
        source.sendFeedback(() -> Text.literal("Breeze Staff link XP cost now requires " + levels + " level(s)."), true);
        return 1;
    }

    public static int setBreezeTeleportXpCost(ServerCommandSource source, int levels) {
        BreezeStaffConfig.TELEPORT_XP_COST = levels;
        source.sendFeedback(() -> Text.literal("Breeze Staff teleport XP cost now requires " + levels + " level(s)."), true);
        return 1;
    }

    public static int setKeepXpOnDeath(ServerCommandSource source, boolean keep) {
        GameRules.BooleanRule keepXpRule = source.getWorld().getGameRules().get(HimProveMeGameRules.KEEP_XP_AFTER_DEATH);
        if (keepXpRule == null) {
            source.sendError(Text.literal("KeepXPAfterDeath rule is unavailable."));
            return 0;
        }
        keepXpRule.set(keep, source.getWorld().getServer());
        source.sendFeedback(() -> Text.literal("Players will " + (keep ? "keep" : "lose") + " XP on death."), true);
        return 1;
    }
}
