package net.xmilon.himproveme.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.xmilon.himproveme.HimProveMeGameRules;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class KeepXpManager {
    private static final Map<UUID, ExperienceSnapshot> SNAPSHOTS = new ConcurrentHashMap<>();

    private KeepXpManager() {}

    public static boolean store(ServerPlayerEntity player) {
        if (!isRuleEnabled(player.getWorld())) {
            return false;
        }

        SNAPSHOTS.put(player.getUuid(), new ExperienceSnapshot(
                player.totalExperience,
                player.experienceLevel,
                player.experienceProgress
        ));

        player.totalExperience = 0;
        player.experienceLevel = 0;
        player.experienceProgress = 0f;
        return true;
    }

    public static void apply(ServerPlayerEntity player, ServerPlayerEntity oldPlayer, boolean alive) {
        if (!isRuleEnabled(player.getWorld())) {
            SNAPSHOTS.remove(oldPlayer.getUuid());
            return;
        }

        if (alive) {
            SNAPSHOTS.remove(oldPlayer.getUuid());
            return;
        }

        ExperienceSnapshot snapshot = SNAPSHOTS.remove(oldPlayer.getUuid());
        if (snapshot == null) {
            return;
        }

        player.totalExperience = snapshot.totalExperience();
        player.experienceLevel = snapshot.level();
        player.experienceProgress = snapshot.progress();

        player.sendMessage(Text.literal("KeepXPAfterDeath restored your experience after death."), true);

        MinecraftServer server = player.getServer();
        if (server != null) {
            server.sendMessage(Text.literal("KeepXPAfterDeath returned XP for " + player.getName().getString() + "."));
        }
    }

    private static boolean isRuleEnabled(World world) {
        GameRules.BooleanRule rule = world.getGameRules().get(HimProveMeGameRules.KEEP_XP_AFTER_DEATH);
        return rule != null && rule.get();
    }

    private record ExperienceSnapshot(int totalExperience, int level, float progress) {
    }
}
