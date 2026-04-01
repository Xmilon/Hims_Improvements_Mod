package net.xmilon.himproveme.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.perk.PerkDefinition;
import net.xmilon.himproveme.perk.PerkRegistry;

import java.util.Collection;

public final class HimProveMeCommands {
    private static final SuggestionProvider<ServerCommandSource> PERK_ID_SUGGESTIONS = (context, builder) -> {
        for (PerkDefinition definition : PerkRegistry.values()) {
            builder.suggest(definition.id().getPath());
        }
        return builder.buildFuture();
    };

    private HimProveMeCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal(HimProveMe.MOD_ID)
                    .requires(source -> source.hasPermissionLevel(2));

            LiteralArgumentBuilder<ServerCommandSource> perkRoot = CommandManager.literal("perk");

            registerPerkAction(
                    perkRoot,
                    "reset",
                    CommandManager.argument("targets", EntityArgumentType.players())
                            .executes(context -> executeReset(context, EntityArgumentType.getPlayers(context, "targets")))
            );

            registerPerkAction(
                    perkRoot,
                    "remove",
                    CommandManager.argument("targets", EntityArgumentType.players())
                            .then(CommandManager.literal(HimProveMe.MOD_ID)
                                    .then(CommandManager.argument("perk_id", StringArgumentType.word())
                                            .suggests(PERK_ID_SUGGESTIONS)
                                            .executes(HimProveMeCommands::executeRemove)))
            );
            registerPerkAction(
                    perkRoot,
                    "add",
                    CommandManager.argument("targets", EntityArgumentType.players())
                            .then(CommandManager.literal(HimProveMe.MOD_ID)
                                    .then(CommandManager.argument("perk_id", StringArgumentType.word())
                                            .suggests(PERK_ID_SUGGESTIONS)
                                            .executes(HimProveMeCommands::executeAdd)))
            );

            root.then(perkRoot);
            root.then(createBreezeCommandTree());
            root.then(createDebugCommandTree());
            root.then(createKeepXpCommand());
            dispatcher.register(root);
        });
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createBreezeCommandTree() {
        LiteralArgumentBuilder<ServerCommandSource> breezeRoot = CommandManager.literal("breeze");

        breezeRoot.then(
                CommandManager.literal("link-xp-cost")
                        .then(CommandManager.argument("levels", IntegerArgumentType.integer(0))
                                .executes(context -> setBreezeLinkXpCost(context, IntegerArgumentType.getInteger(context, "levels"))))
        );

        breezeRoot.then(
                CommandManager.literal("teleport-xp-cost")
                        .then(CommandManager.argument("levels", IntegerArgumentType.integer(0))
                                .executes(context -> setBreezeTeleportXpCost(context, IntegerArgumentType.getInteger(context, "levels"))))
        );

        return breezeRoot;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createKeepXpCommand() {
        return CommandManager.literal("keep-xp")
                .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> setKeepXp(context, BoolArgumentType.getBool(context, "value"))));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createDebugCommandTree() {
        return CommandManager.literal("debug")
                .then(CommandManager.literal("movement")
                        .executes(context -> debugMovementSelf(context))
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(context -> debugMovementTargets(context))));
    }

    private static void registerPerkAction(
            LiteralArgumentBuilder<ServerCommandSource> perkRoot,
            String action,
            com.mojang.brigadier.builder.ArgumentBuilder<ServerCommandSource, ?> syntax
    ) {
        perkRoot.then(CommandManager.literal(action).then(syntax));
    }

    private static int executeRemove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        String perkPath = StringArgumentType.getString(context, "perk_id");
        Identifier perkId = Identifier.of(HimProveMe.MOD_ID, perkPath);
        return HimProveMeCommandFunctions.removePerk(context.getSource(), targets, perkId);
    }

    private static int executeAdd(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        String perkPath = StringArgumentType.getString(context, "perk_id");
        Identifier perkId = Identifier.of(HimProveMe.MOD_ID, perkPath);
        return HimProveMeCommandFunctions.addPerk(context.getSource(), targets, perkId);
    }

    private static int executeReset(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets) {
        return HimProveMeCommandFunctions.resetPerks(context.getSource(), targets);
    }

    private static int setBreezeLinkXpCost(CommandContext<ServerCommandSource> context, int levels) {
        return HimProveMeCommandFunctions.setBreezeLinkXpCost(context.getSource(), levels);
    }

    private static int setBreezeTeleportXpCost(CommandContext<ServerCommandSource> context, int levels) {
        return HimProveMeCommandFunctions.setBreezeTeleportXpCost(context.getSource(), levels);
    }

    private static int setKeepXp(CommandContext<ServerCommandSource> context, boolean keep) {
        return HimProveMeCommandFunctions.setKeepXpOnDeath(context.getSource(), keep);
    }

    private static int debugMovementSelf(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return HimProveMeCommandFunctions.debugMovement(context.getSource(), java.util.List.of(context.getSource().getPlayerOrThrow()));
    }

    private static int debugMovementTargets(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return HimProveMeCommandFunctions.debugMovement(context.getSource(), EntityArgumentType.getPlayers(context, "targets"));
    }
}
