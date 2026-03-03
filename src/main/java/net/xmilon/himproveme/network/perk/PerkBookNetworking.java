package net.xmilon.himproveme.network.perk;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.xmilon.himproveme.perk.PerkBookState;
import net.xmilon.himproveme.perk.PerkDefinition;
import net.xmilon.himproveme.perk.PerkBookStateHolder;
import net.xmilon.himproveme.perk.PerkFunctions;
import net.xmilon.himproveme.perk.PerkRegistry;

public final class PerkBookNetworking {
    private PerkBookNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(PerkBookRequestSyncPayload.ID, PerkBookRequestSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PerkBookCreateInstancePayload.ID, PerkBookCreateInstancePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PerkBookSelectInstancePayload.ID, PerkBookSelectInstancePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PerkBookUpgradePayload.ID, PerkBookUpgradePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PerkBookSyncPayload.ID, PerkBookSyncPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(PerkBookRequestSyncPayload.ID, (payload, context) ->
                context.server().execute(() -> sync(context.player()))
        );
        ServerPlayNetworking.registerGlobalReceiver(PerkBookCreateInstancePayload.ID, (payload, context) ->
                context.server().execute(() -> createInstance(context.player()))
        );
        ServerPlayNetworking.registerGlobalReceiver(PerkBookSelectInstancePayload.ID, (payload, context) ->
                context.server().execute(() -> selectInstance(context.player(), payload.index()))
        );
        ServerPlayNetworking.registerGlobalReceiver(PerkBookUpgradePayload.ID, (payload, context) ->
                context.server().execute(() -> upgrade(context.player(), payload))
        );

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sync(handler.player));
    }

    public static void sync(ServerPlayerEntity player) {
        PerkBookState state = ((PerkBookStateHolder) player).himproveme$getPerkBookState();
        ServerPlayNetworking.send(player, new PerkBookSyncPayload(state.toNbt()));
    }

    private static void createInstance(ServerPlayerEntity player) {
        PerkBookState state = ((PerkBookStateHolder) player).himproveme$getPerkBookState();
        if (!state.createInstance()) {
            player.sendMessage(Text.translatable("perk.himproveme.error.too_many_instances"), true);
            player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.PLAYERS, 0.8f, 0.9f);
        }
        sync(player);
    }

    private static void selectInstance(ServerPlayerEntity player, int index) {
        PerkBookState state = ((PerkBookStateHolder) player).himproveme$getPerkBookState();
        state.selectInstance(index);
        sync(player);
    }

    private static void upgrade(ServerPlayerEntity player, PerkBookUpgradePayload payload) {
        PerkBookState state = ((PerkBookStateHolder) player).himproveme$getPerkBookState();
        PerkDefinition definition = PerkRegistry.get(payload.perkId());
        PerkBookState.UpgradeResult result = state.upgrade(player, payload.instanceIndex(), payload.perkId());
        switch (result) {
            case SUCCESS -> {
                player.sendMessage(Text.translatable("perk.himproveme.unlock.success"), true);
                if (definition != null) {
                    PerkFunctions.runUnlock(player, definition.id());
                }
                SoundEvent unlockSound = definition == null
                        ? SoundEvents.ENTITY_PLAYER_LEVELUP
                        : Registries.SOUND_EVENT.get(definition.unlockSoundId());
                if (unlockSound == null) {
                    unlockSound = SoundEvents.ENTITY_PLAYER_LEVELUP;
                }
                player.playSoundToPlayer(unlockSound, SoundCategory.PLAYERS, 0.7f, 1.15f);
            }
            case NOT_ENOUGH_LEVELS -> {
                player.sendMessage(
                        Text.translatable("perk.himproveme.error.not_enough_levels", PerkRegistry.XP_LEVEL_COST_PER_UPGRADE)
                                .formatted(Formatting.RED),
                        true
                );
                player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.PLAYERS, 0.8f, 0.9f);
            }
            case PREREQUISITE_LOCKED -> {
                player.sendMessage(Text.translatable("perk.himproveme.error.prerequisite").formatted(Formatting.RED), true);
                player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.PLAYERS, 0.8f, 0.9f);
            }
            case MAX_LEVEL -> {
                player.sendMessage(Text.translatable("perk.himproveme.error.already_unlocked").formatted(Formatting.YELLOW), true);
                player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.PLAYERS, 0.8f, 1.1f);
            }
            default -> player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.PLAYERS, 0.7f, 0.8f);
        }
        sync(player);
    }
}
