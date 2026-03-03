package net.xmilon.himproveme.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.item.custom.EnderStaffItem;
import net.xmilon.himproveme.item.custom.ability.SpecialAbilityItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpecialAbilityManager {
    private record ActiveAbility(Hand hand, Identifier itemId) {
    }

    private static final Map<UUID, ActiveAbility> ACTIVE_SPECIAL_ABILITY = new HashMap<>();

    private SpecialAbilityManager() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(SpecialAbilityTogglePayload.ID, SpecialAbilityTogglePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SpecialAbilityTogglePayload.ID, (payload, context) ->
                context.server().execute(() -> toggle(context.player())));
        ServerTickEvents.END_SERVER_TICK.register(SpecialAbilityManager::tickActiveAbilities);
    }

    private static void toggle(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();

        if (tryUseAbility(player, Hand.MAIN_HAND)) {
            return;
        }
        if (tryUseAbility(player, Hand.OFF_HAND)) {
            return;
        }

        ACTIVE_SPECIAL_ABILITY.remove(playerId);
    }

    private static boolean tryUseAbility(ServerPlayerEntity player, Hand hand) {
        UUID playerId = player.getUuid();
        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();

        if (item instanceof EnderStaffItem) {
            return EnderStaffItem.tryTeleport(player, stack);
        }

        if (!(item instanceof SpecialAbilityItem specialAbilityItem)) {
            return false;
        }

        if (!specialAbilityItem.keepsAbilityActive()) {
            specialAbilityItem.onSpecialAbilityActivated(player, stack);
            return true;
        }

        Identifier heldItemId = Registries.ITEM.getId(item);
        ActiveAbility current = ACTIVE_SPECIAL_ABILITY.get(playerId);
        if (current != null && current.hand == hand && current.itemId.equals(heldItemId)) {
            ACTIVE_SPECIAL_ABILITY.remove(playerId);
            player.sendMessage(Text.literal("Special ability OFF").formatted(Formatting.RED), true);
            player.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 0.8f, 1.0f);
            return true;
        }

        ACTIVE_SPECIAL_ABILITY.put(playerId, new ActiveAbility(hand, heldItemId));
        specialAbilityItem.onSpecialAbilityActivated(player, stack);
        player.sendMessage(Text.literal("Special ability ON").formatted(Formatting.GREEN), true);
        player.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_TRADE, SoundCategory.PLAYERS, 0.8f, 1.0f);
        return true;
    }

    private static void tickActiveAbilities(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ActiveAbility activeAbility = ACTIVE_SPECIAL_ABILITY.get(player.getUuid());
            if (activeAbility == null) {
                continue;
            }

            ItemStack stack = player.getStackInHand(activeAbility.hand());
            Item item = stack.getItem();
            if (!(item instanceof SpecialAbilityItem specialAbilityItem)) {
                ACTIVE_SPECIAL_ABILITY.remove(player.getUuid());
                continue;
            }

            Identifier heldItemId = Registries.ITEM.getId(item);
            if (!heldItemId.equals(activeAbility.itemId())) {
                ACTIVE_SPECIAL_ABILITY.remove(player.getUuid());
                continue;
            }

            specialAbilityItem.onSpecialAbilityTick(player, stack);
        }
    }
}
