package net.xmilon.himproveme.command;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.LevelInfo;
import net.xmilon.himproveme.HimProveMeGameRules;
import net.xmilon.himproveme.item.custom.BreezeStaffConfig;
import net.xmilon.himproveme.network.perk.PerkBookNetworking;
import net.xmilon.himproveme.perk.PerkBookState;
import net.xmilon.himproveme.perk.PerkBookStateHolder;
import net.xmilon.himproveme.perk.PerkRegistry;
import net.xmilon.himproveme.prone.ProneNetworking;

import java.util.Collection;
import java.util.Locale;
import java.lang.reflect.Field;
import java.util.stream.Collectors;

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

    public static int setHardcoreMode(ServerCommandSource source, boolean hardcore) {
        MinecraftServer server = source.getServer();
        SaveProperties saveProperties = server.getSaveProperties();
        LevelInfo currentInfo = saveProperties.getLevelInfo();
        if (currentInfo.isHardcore() == hardcore) {
            source.sendFeedback(() -> Text.literal("World hardcore mode is already set to " + hardcore + "."), true);
            return 1;
        }

        LevelInfo updatedInfo = new LevelInfo(
                currentInfo.getLevelName(),
                currentInfo.getGameMode(),
                hardcore,
                currentInfo.getDifficulty(),
                currentInfo.areCommandsAllowed(),
                currentInfo.getGameRules(),
                currentInfo.getDataConfiguration()
        );

        try {
            setLevelInfo(saveProperties, updatedInfo);
        } catch (ReflectiveOperationException exception) {
            source.sendError(Text.literal("Unable to change hardcore mode: " + exception.getMessage()));
            return 0;
        }

        if (!hardcore) {
            server.setDifficultyLocked(false);
        }

        server.saveAll(true, false, false);
        source.sendFeedback(
                () -> Text.literal("World hardcore mode set to " + hardcore + ". Players may need to rejoin for the UI to refresh."),
                true
        );
        return 1;
    }

    public static int debugMovement(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        for (ServerPlayerEntity player : targets) {
            source.sendFeedback(() -> Text.literal(describeMovementState(player)), false);
        }
        return targets.size();
    }

    private static String describeMovementState(ServerPlayerEntity player) {
        EntityAttributeInstance movementSpeed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        double movementBase = movementSpeed != null ? movementSpeed.getBaseValue() : Double.NaN;
        double movementValue = movementSpeed != null ? movementSpeed.getValue() : Double.NaN;

        String effects = player.getStatusEffects().isEmpty()
                ? "none"
                : player.getStatusEffects().stream()
                        .map(HimProveMeCommandFunctions::formatEffect)
                        .collect(Collectors.joining(", "));

        return player.getName().getString()
                + " walkSpeed=" + formatDecimal(player.getAbilities().getWalkSpeed())
                + " flySpeed=" + formatDecimal(player.getAbilities().getFlySpeed())
                + " movementBase=" + formatDecimal(movementBase)
                + " movementValue=" + formatDecimal(movementValue)
                + " pose=" + player.getPose()
                + " swimming=" + player.isSwimming()
                + " sprinting=" + player.isSprinting()
                + " onGround=" + player.isOnGround()
                + " flying=" + player.getAbilities().flying
                + " prone=" + ProneNetworking.isProne(player.getUuid())
                + " effects=[" + effects + "]";
    }

    private static String formatEffect(StatusEffectInstance effect) {
        Identifier effectId = Registries.STATUS_EFFECT.getId(effect.getEffectType().value());
        String effectName = effectId == null ? "unknown" : effectId.toString();
        int amplifier = effect.getAmplifier() + 1;
        return effectName + " x" + amplifier + " (" + effect.getDuration() + "t)";
    }

    private static String formatDecimal(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    private static void setLevelInfo(SaveProperties saveProperties, LevelInfo updatedInfo) throws ReflectiveOperationException {
        Field levelInfoField = findField(saveProperties.getClass(), "levelInfo");
        if (levelInfoField == null) {
            throw new NoSuchFieldException("levelInfo");
        }

        levelInfoField.setAccessible(true);
        levelInfoField.set(saveProperties, updatedInfo);
    }

    private static Field findField(Class<?> type, String fieldName) {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
