package net.xmilon.himproveme.perk.warden;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.effect.ModStatusEffects;
import net.xmilon.himproveme.item.ModItem;
import net.xmilon.himproveme.network.warden.WardenAfflictionSyncPayload;
import net.xmilon.himproveme.network.warden.WardenSepukuPayload;
import net.xmilon.himproveme.perk.PerkAccess;
import net.xmilon.himproveme.perk.PerkInstanceState;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Central runtime for the three Warden perks:
 * unlock gating, exclusive activation, affliction tracking, status effects, client sync and the shared health penalty.
 */
public final class WardenPerkHelper {
    public static final Identifier WARDEN_BLEEDING = Identifier.of(HimProveMe.MOD_ID, "warden_bleeding");
    public static final Identifier WARDEN_STUNNED = Identifier.of(HimProveMe.MOD_ID, "warden_stunned");
    public static final Identifier WARDEN_FRENZY = Identifier.of(HimProveMe.MOD_ID, "warden_frenzy");
    public static final List<Identifier> WARDEN_PERKS = List.of(WARDEN_BLEEDING, WARDEN_STUNNED, WARDEN_FRENZY);

    private static final Identifier HEALTH_PENALTY_MODIFIER_ID = Identifier.of(HimProveMe.MOD_ID, "warden_perk_health_penalty");
    private static final double HEALTH_PENALTY = -4.0D;
    private static final int DECAY_DELAY_TICKS = 200;
    private static final int EFFECT_REFRESH_TICKS = 40;
    private static final int BLEED_DAMAGE_INTERVAL_TICKS = 100;
    private static final int CONTROL_ROLL_INTERVAL_TICKS = 20;
    private static final int FRENZY_AXIS_INTERVAL_TICKS = 30;
    private static final int SEPUKU_DURATION_TICKS = 36;
    private static final Map<Identifier, WorldRuntime> WORLD_RUNTIMES = new HashMap<>();
    private static final Map<UUID, WardenAfflictionSyncPayload> LAST_SYNC_PAYLOADS = new HashMap<>();

    private WardenPerkHelper() {
    }

    /**
     * Registers the S2C payload types and the server tick hooks used by the Warden perk runtime.
     */
    public static void register() {
        PayloadTypeRegistry.playS2C().register(WardenAfflictionSyncPayload.ID, WardenAfflictionSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WardenSepukuPayload.ID, WardenSepukuPayload.CODEC);
        ServerTickEvents.END_WORLD_TICK.register(WardenPerkHelper::tickWorld);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncPlayer(handler.player, WardenAfflictionSyncPayload.inactive()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LAST_SYNC_PAYLOADS.remove(handler.player.getUuid()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            WORLD_RUNTIMES.clear();
            LAST_SYNC_PAYLOADS.clear();
        });
    }

    /**
     * Returns true when the perk belongs to the mutually-exclusive Warden trio.
     */
    public static boolean isWardenPerk(Identifier perkId) {
        return WARDEN_PERKS.contains(perkId);
    }

    /**
     * Returns true when the player currently has the required Warden Token equipped for unlock.
     */
    public static boolean hasUnlockToken(ServerPlayerEntity player) {
        return player.getMainHandStack().isOf(ModItem.WARDEN_TOKEN);
    }

    /**
     * Consumes the Warden Token from the player's main hand once a Warden perk unlock succeeds.
     */
    public static void consumeUnlockToken(ServerPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        if (stack.isOf(ModItem.WARDEN_TOKEN)) {
            stack.decrement(1);
        }
    }

    /**
     * Disables the other Warden perks in the same loadout when one of them gets enabled.
     */
    public static void enforceExclusiveToggle(PerkInstanceState instance, Identifier enabledPerkId) {
        for (Identifier perkId : WARDEN_PERKS) {
            if (!perkId.equals(enabledPerkId) && instance.getLevel(perkId) > 0) {
                instance.setEnabled(perkId, false);
            }
        }
    }

    /**
     * Returns true when the player has one of the three Warden perks currently active in the selected loadout.
     */
    public static boolean hasAnyActiveWardenPerk(PlayerEntity player) {
        return getActiveProfile(player).isPresent();
    }

    /**
     * Returns true once the selected loadout has unlocked at least one Warden perk, even if none of them are currently enabled.
     */
    public static boolean hasAnyUnlockedWardenPerk(PlayerEntity player) {
        return PerkAccess.hasUnlocked(player, WARDEN_BLEEDING)
                || PerkAccess.hasUnlocked(player, WARDEN_STUNNED)
                || PerkAccess.hasUnlocked(player, WARDEN_FRENZY);
    }

    /**
     * Resolves the active affliction profile from the player's enabled Warden perk.
     */
    public static Optional<AfflictionProfile> getActiveProfile(PlayerEntity player) {
        if (PerkAccess.hasEffect(player, WARDEN_BLEEDING)) {
            return Optional.of(AfflictionProfile.BLEEDING);
        }
        if (PerkAccess.hasEffect(player, WARDEN_STUNNED)) {
            return Optional.of(AfflictionProfile.STUNNED);
        }
        if (PerkAccess.hasEffect(player, WARDEN_FRENZY)) {
            return Optional.of(AfflictionProfile.FRENZY);
        }
        return Optional.empty();
    }

    /**
     * Exposes the ephemeral affliction state for integrations such as Jade without persisting anything on the entity.
     */
    public static Optional<AfflictionView> getAfflictionView(LivingEntity target) {
        if (!(target.getWorld() instanceof ServerWorld world)) {
            return Optional.empty();
        }

        AfflictionState state = getRuntime(world).statesByTarget.get(target.getUuid());
        if (state == null) {
            return Optional.empty();
        }

        return Optional.of(new AfflictionView(state.getProfile(), state.getBarPercent(), state.isEffectActive()));
    }

    /**
     * Keeps the shared max-health penalty in sync with the currently active Warden perk loadout.
     */
    public static void applyHealthPenalty(PlayerEntity player) {
        EntityAttributeInstance maxHealth = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        if (hasAnyActiveWardenPerk(player)) {
            maxHealth.updateModifier(new EntityAttributeModifier(HEALTH_PENALTY_MODIFIER_ID, HEALTH_PENALTY, EntityAttributeModifier.Operation.ADD_VALUE));
            if (!player.getWorld().isClient() && player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        } else {
            maxHealth.removeModifier(HEALTH_PENALTY_MODIFIER_ID);
        }
    }

    /**
     * Handles the actual bar gain after a player lands a valid melee hit with an active Warden perk.
     */
    public static void onSuccessfulAttack(ServerPlayerEntity attacker, LivingEntity target, float dealtDamage, boolean critical) {
        Optional<AfflictionProfile> profile = getActiveProfile(attacker);
        if (profile.isEmpty() || dealtDamage <= 0.0F || !target.isAlive()) {
            return;
        }

        WorldRuntime runtime = getRuntime(attacker.getServerWorld());
        AfflictionState currentState = runtime.statesByTarget.get(target.getUuid());
        if (currentState == null || currentState.canBeReplaced(attacker.getUuid(), profile.get(), attacker.getServerWorld().getTime())) {
            if (currentState == null || currentState.getOwnerUuid().equals(attacker.getUuid()) || currentState.canBeReplaced(attacker.getUuid(), profile.get(), attacker.getServerWorld().getTime())) {
                currentState = new AfflictionState(attacker.getUuid(), target.getUuid(), profile.get(), attacker.getServerWorld().getTime());
                runtime.statesByTarget.put(target.getUuid(), currentState);
            }
        }

        if (!currentState.getOwnerUuid().equals(attacker.getUuid()) || currentState.getProfile() != profile.get()) {
            return;
        }

        currentState.addBar(profile.get().computeGain(dealtDamage, target.getMaxHealth(), critical), attacker.getServerWorld().getTime());
    }

    /**
     * Advances affliction decay, effect application, client sync and the sepuku countdown for one world.
     */
    private static void tickWorld(ServerWorld world) {
        WorldRuntime runtime = getRuntime(world);
        Set<UUID> syncedPlayers = new HashSet<>();
        Iterator<Map.Entry<UUID, AfflictionState>> iterator = runtime.statesByTarget.entrySet().iterator();

        while (iterator.hasNext()) {
            AfflictionState state = iterator.next().getValue();
            if (!(world.getEntity(state.getTargetUuid()) instanceof LivingEntity target) || !target.isAlive()) {
                clearTargetEffects(targetOrNull(world, state.getTargetUuid()));
                iterator.remove();
                continue;
            }

            if (!tickState(world, target, state)) {
                clearTargetEffects(target);
                iterator.remove();
                continue;
            }

            if (target instanceof ServerPlayerEntity player) {
                syncPlayer(player, createPayload(state));
                syncedPlayers.add(player.getUuid());
            }
        }

        for (ServerPlayerEntity player : world.getPlayers()) {
            if (syncedPlayers.contains(player.getUuid())) {
                continue;
            }
            if (LAST_SYNC_PAYLOADS.containsKey(player.getUuid())) {
                syncPlayer(player, WardenAfflictionSyncPayload.inactive());
            }
        }
    }

    /**
     * Updates one afflicted entity for the current tick and returns false when the state should be discarded.
     */
    private static boolean tickState(ServerWorld world, LivingEntity target, AfflictionState state) {
        if (!state.isSepukuTriggered() && world.getTime() > state.getLastHitTick() + DECAY_DELAY_TICKS) {
            state.decay(state.getProfile().getDecayPerTick(state.isEffectActive()));
        }

        return switch (state.getProfile()) {
            case BLEEDING -> tickBleeding(world, target, state);
            case STUNNED -> tickStunned(world, target, state);
            case FRENZY -> tickFrenzy(world, target, state);
        };
    }

    /**
     * Applies the bleeding threshold, the periodic health drain and the visual marker effect.
     */
    private static boolean tickBleeding(ServerWorld world, LivingEntity target, AfflictionState state) {
        boolean active = state.getBarPercent() >= state.getProfile().getTriggerThreshold()
                || (state.isEffectActive() && state.getProfile().shouldRemainActive(state.getBarPercent()));
        state.setEffectActive(active);

        if (!active) {
            target.removeStatusEffect(ModStatusEffects.BLEEDING);
            return state.getBarPercent() > 0.05F;
        }

        refreshStatusEffect(target, ModStatusEffects.BLEEDING);
        if (world.getTime() >= state.getNextDotTick()) {
            target.damage(world.getDamageSources().magic(), 2.0F);
            state.setNextDotTick(world.getTime() + BLEED_DAMAGE_INTERVAL_TICKS);
        }
        return true;
    }

    /**
     * Applies the stunned threshold and drives the short-lived control disruption for players and mobs.
     */
    private static boolean tickStunned(ServerWorld world, LivingEntity target, AfflictionState state) {
        boolean active = state.getBarPercent() >= state.getProfile().getTriggerThreshold()
                || (state.isEffectActive() && state.getProfile().shouldRemainActive(state.getBarPercent()));
        state.setEffectActive(active);

        if (!active) {
            state.clearControlDisruption();
            target.removeStatusEffect(ModStatusEffects.STUNNED);
            return state.getBarPercent() > 0.05F;
        }

        refreshStatusEffect(target, ModStatusEffects.STUNNED);
        target.setSprinting(false);

        if (state.getBarPercent() >= 100.0F && world.getTime() >= state.getNextControlShuffleTick()) {
            if (target instanceof ServerPlayerEntity) {
                state.shuffleMovementMapping(world.getRandom());
            } else if (target instanceof MobEntity mob) {
                randomizeMobTarget(world, mob);
            }
            state.setNextControlShuffleTick(world.getTime() + CONTROL_ROLL_INTERVAL_TICKS);
        }
        return true;
    }

    /**
     * Applies Frenzy hallucination/input pressure and starts the sepuku sequence once the bar is full.
     */
    private static boolean tickFrenzy(ServerWorld world, LivingEntity target, AfflictionState state) {
        state.setEffectActive(state.getBarPercent() > 5.0F || state.isSepukuTriggered());
        if (!state.isEffectActive()) {
            state.clearControlDisruption();
            target.removeStatusEffect(ModStatusEffects.FRENZY);
            return false;
        }

        refreshStatusEffect(target, ModStatusEffects.FRENZY);

        if (state.isSepukuTriggered()) {
            freezeTarget(target);
            state.decrementSepukuTicks();
            if (state.getSepukuTicksRemaining() <= 0) {
                killForOwner(world, target, state.getOwnerUuid());
                return false;
            }
            return true;
        }

        if (target instanceof ServerPlayerEntity) {
            if (world.getTime() >= state.getNextControlShuffleTick()) {
                state.rollFrenzyAxis(world.getRandom());
                state.setNextControlShuffleTick(world.getTime() + FRENZY_AXIS_INTERVAL_TICKS);
            }
        } else if (target instanceof MobEntity mob && world.getTime() >= state.getNextControlShuffleTick()) {
            forceMobDetour(world, mob);
            state.setNextControlShuffleTick(world.getTime() + FRENZY_AXIS_INTERVAL_TICKS);
        }

        if (state.getBarPercent() >= 100.0F) {
            state.beginSepuku(SEPUKU_DURATION_TICKS);
            broadcastSepukuStart(target, SEPUKU_DURATION_TICKS);
        }
        return true;
    }

    /**
     * Refreshes the icon-only status effect without showing particles every tick.
     */
    private static void refreshStatusEffect(LivingEntity target, RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect) {
        target.addStatusEffect(new StatusEffectInstance(effect, EFFECT_REFRESH_TICKS, 0, true, false, true));
    }

    /**
     * Removes all three custom Warden perk status effects from a target.
     */
    private static void clearTargetEffects(LivingEntity target) {
        if (target == null) {
            return;
        }
        target.removeStatusEffect(ModStatusEffects.BLEEDING);
        target.removeStatusEffect(ModStatusEffects.STUNNED);
        target.removeStatusEffect(ModStatusEffects.FRENZY);
    }

    /**
     * Chooses a random nearby living target for a stunned mob or makes it stumble toward a random point.
     */
    private static void randomizeMobTarget(ServerWorld world, MobEntity mob) {
        List<LivingEntity> nearbyTargets = world.getNonSpectatingEntities(LivingEntity.class, mob.getBoundingBox().expand(8.0D));
        nearbyTargets.removeIf(candidate -> candidate == mob || !candidate.isAlive());

        if (!nearbyTargets.isEmpty()) {
            LivingEntity newTarget = nearbyTargets.get(world.getRandom().nextInt(nearbyTargets.size()));
            mob.setTarget(newTarget);
            mob.getNavigation().startMovingTo(newTarget, 1.0D);
            return;
        }

        forceMobDetour(world, mob);
    }

    /**
     * Makes a Frenzied mob wander off-course for a moment instead of following its normal path.
     */
    private static void forceMobDetour(ServerWorld world, MobEntity mob) {
        double offsetX = (world.getRandom().nextDouble() - 0.5D) * 10.0D;
        double offsetZ = (world.getRandom().nextDouble() - 0.5D) * 10.0D;
        mob.getNavigation().startMovingTo(mob.getX() + offsetX, mob.getY(), mob.getZ() + offsetZ, 1.0D);
    }

    /**
     * Locks a target in place during the sepuku animation so the rendered pose reads clearly.
     */
    private static void freezeTarget(LivingEntity target) {
        target.setVelocity(0.0D, 0.0D, 0.0D);
        target.velocityModified = true;
        target.setSprinting(false);
        if (target instanceof MobEntity mob) {
            mob.getNavigation().stop();
        }
    }

    /**
     * Attempts to attribute the finishing blow to the player who filled the bar while still guaranteeing the kill.
     */
    private static void killForOwner(ServerWorld world, LivingEntity target, UUID ownerUuid) {
        if (world.getServer().getPlayerManager().getPlayer(ownerUuid) instanceof ServerPlayerEntity owner) {
            target.setAttacker(owner);
            target.damage(owner.getDamageSources().playerAttack(owner), Float.MAX_VALUE);
        } else {
            target.damage(world.getDamageSources().genericKill(), Float.MAX_VALUE);
        }

        if (target.isAlive()) {
            target.kill();
        }
    }

    /**
     * Broadcasts the sepuku animation start to all tracking players and the victim when it is also a player.
     */
    private static void broadcastSepukuStart(LivingEntity target, int durationTicks) {
        Collection<ServerPlayerEntity> recipients = new HashSet<>(PlayerLookup.tracking(target));
        if (target instanceof ServerPlayerEntity playerTarget) {
            recipients.add(playerTarget);
        }

        WardenSepukuPayload payload = new WardenSepukuPayload(target.getUuid(), durationTicks);
        for (ServerPlayerEntity recipient : recipients) {
            ServerPlayNetworking.send(recipient, payload);
        }
    }

    /**
     * Builds the client snapshot for an afflicted player.
     */
    private static WardenAfflictionSyncPayload createPayload(AfflictionState state) {
        boolean controlChaos = state.getProfile() == AfflictionProfile.STUNNED && state.getBarPercent() >= 100.0F;
        return new WardenAfflictionSyncPayload(
                true,
                state.getProfile().ordinal(),
                state.getBarPercent(),
                controlChaos,
                state.getMovementMapping(),
                state.isInvertForwardAxis(),
                state.isInvertSidewaysAxis(),
                state.getSepukuTicksRemaining()
        );
    }

    /**
     * Sends a payload only when the snapshot changed so the client receives stable, low-noise updates.
     */
    private static void syncPlayer(ServerPlayerEntity player, WardenAfflictionSyncPayload payload) {
        WardenAfflictionSyncPayload previousPayload = LAST_SYNC_PAYLOADS.get(player.getUuid());
        if (payload.equals(previousPayload)) {
            return;
        }

        ServerPlayNetworking.send(player, payload);
        if (payload.active()) {
            LAST_SYNC_PAYLOADS.put(player.getUuid(), payload);
        } else {
            LAST_SYNC_PAYLOADS.remove(player.getUuid());
        }
    }

    /**
     * Returns the lazy-created affliction runtime for a world.
     */
    private static WorldRuntime getRuntime(ServerWorld world) {
        return WORLD_RUNTIMES.computeIfAbsent(world.getRegistryKey().getValue(), ignored -> new WorldRuntime());
    }

    /**
     * Helper that tolerates already-removed targets during cleanup.
     */
    private static LivingEntity targetOrNull(ServerWorld world, UUID targetUuid) {
        return world.getEntity(targetUuid) instanceof LivingEntity living ? living : null;
    }

    /**
     * Per-world ephemeral affliction state keyed by target UUID.
     */
    private static final class WorldRuntime {
        private final Map<UUID, AfflictionState> statesByTarget = new HashMap<>();
    }

    /**
     * Immutable server snapshot used by compatibility providers that need read-only access to the current bar state.
     */
    public record AfflictionView(AfflictionProfile profile, float barPercent, boolean effectActive) {
    }
}
