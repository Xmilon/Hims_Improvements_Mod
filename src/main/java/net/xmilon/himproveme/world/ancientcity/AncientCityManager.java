package net.xmilon.himproveme.world.ancientcity;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.xmilon.himproveme.item.ModItem;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles all Ancient City specific runtime rules:
 * scent trails, the one-Warden city cap, city-scoped loot tweaks and the guaranteed token reward.
 */
public final class AncientCityManager {
    private static final int SCENT_LIFETIME_TICKS = 20 * 30;
    private static final int MAX_SCENT_MARKERS_PER_PLAYER = 128;
    private static final int WARDEN_SCAN_INTERVAL_TICKS = 20;
    private static final int MAX_MARKERS_DETECTED_PER_SCAN = 2;
    private static final double WARDEN_SCENT_RADIUS = 18.0D;
    private static final float LOCK_ON_THRESHOLD = 9.0F;
    private static final float LOCK_RELEASE_THRESHOLD = 4.5F;
    private static final float SUSPICION_DECAY_PER_TICK = 0.015F;
    private static final float LOCKED_SUSPICION_DECAY_PER_TICK = 0.008F;
    private static final int WARDEN_LOCK_REFRESH_INTERVAL = 10;
    private static final Map<RegistryKey<net.minecraft.world.World>, WorldRuntime> WORLD_RUNTIMES = new HashMap<>();

    private AncientCityManager() {
    }

    /**
     * Registers the Ancient City runtime hooks and clears transient state when a server stops.
     */
    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(AncientCityManager::tickWorld);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> WORLD_RUNTIMES.clear());
    }

    /**
     * Returns true when the loot table belongs to one of the vanilla Ancient City chest variants.
     */
    public static boolean shouldModifyLoot(RegistryKey<LootTable> lootTableKey) {
        return lootTableKey == LootTables.ANCIENT_CITY_CHEST || lootTableKey == LootTables.ANCIENT_CITY_ICE_BOX_CHEST;
    }

    /**
     * Applies the Ancient City loot rebalance after vanilla has generated the chest contents.
     * This runs post-generation because city-level token guarantees and loot trimming need inventory context.
     */
    public static void onLootGenerated(ServerPlayerEntity player, LootableInventory lootableInventory, RegistryKey<LootTable> lootTableKey) {
        if (!shouldModifyLoot(lootTableKey) || !(lootableInventory instanceof Inventory inventory)) {
            return;
        }
        if (!(lootableInventory.getWorld() instanceof ServerWorld world)) {
            return;
        }

        Optional<AncientCityLocator.CityReference> city = AncientCityLocator.locate(world, lootableInventory.getPos());
        if (city.isEmpty()) {
            return;
        }

        Random random = world.getRandom();
        float tableScale = lootTableKey == LootTables.ANCIENT_CITY_ICE_BOX_CHEST ? 0.75F : 1.0F;
        trimLoot(inventory, random, tableScale);
        addNetheriteLoot(inventory, random, tableScale);
        addBookLoot(world, inventory, random, tableScale);
        addEquipmentLoot(world, inventory, random, tableScale);
        ensureGuaranteedToken(world, inventory, city.get().key());
    }

    /**
     * Returns true when a shrieker inside an Ancient City should be prevented from spawning a second Warden.
     */
    public static boolean shouldBlockCityWardenSpawn(ServerWorld world, BlockPos shriekerPos) {
        Optional<AncientCityLocator.CityReference> city = AncientCityLocator.locate(world, shriekerPos);
        return city.filter(reference -> hasLivingWardenInCity(world, reference)).isPresent();
    }

    /**
     * Ticks scent trails and lock-on behavior for one world.
     */
    private static void tickWorld(ServerWorld world) {
        getRuntime(world).tick(world);
    }

    /**
     * Returns the lazy-created runtime container for a world.
     */
    private static WorldRuntime getRuntime(ServerWorld world) {
        return WORLD_RUNTIMES.computeIfAbsent(world.getRegistryKey(), ignored -> new WorldRuntime());
    }

    /**
     * Checks the cached city state first and then falls back to loaded Warden entities inside the structure box.
     */
    private static boolean hasLivingWardenInCity(ServerWorld world, AncientCityLocator.CityReference city) {
        WorldRuntime runtime = getRuntime(world);
        CityRuntimeState cityState = runtime.cityStates.get(city.key());
        if (cityState != null && cityState.wardenUuid != null) {
            if (world.getEntity(cityState.wardenUuid) instanceof WardenEntity warden
                    && warden.isAlive()
                    && city.boundingBox().contains(warden.getBlockPos())) {
                return true;
            }
        }

        List<? extends WardenEntity> wardens = world.getEntitiesByType(
                TypeFilter.instanceOf(WardenEntity.class),
                warden -> warden.isAlive() && city.boundingBox().contains(warden.getBlockPos())
        );
        if (wardens.isEmpty()) {
            return false;
        }

        CityRuntimeState state = runtime.cityStates.computeIfAbsent(city.key(), ignored -> new CityRuntimeState());
        state.wardenUuid = wardens.get(0).getUuid();
        return true;
    }

    /**
     * Removes some of the vanilla chest contents so the added late-game loot does not inflate total chest value.
     */
    private static void trimLoot(Inventory inventory, Random random, float tableScale) {
        List<Integer> filledSlots = new ArrayList<>();
        for (int slot = 0; slot < inventory.size(); slot++) {
            if (!inventory.getStack(slot).isEmpty()) {
                filledSlots.add(slot);
            }
        }

        if (filledSlots.size() <= 4) {
            return;
        }

        int removeCount = Math.max(1, Math.round(filledSlots.size() * (0.28F * tableScale)));
        removeCount = Math.min(removeCount, Math.max(1, filledSlots.size() - 4));

        for (int removed = 0; removed < removeCount && !filledSlots.isEmpty(); removed++) {
            int slotIndex = random.nextInt(filledSlots.size());
            int slot = filledSlots.remove(slotIndex);
            inventory.setStack(slot, ItemStack.EMPTY);
        }
    }

    /**
     * Adds low-weight netherite crafting materials to Ancient City chests.
     */
    private static void addNetheriteLoot(Inventory inventory, Random random, float tableScale) {
        if (random.nextFloat() < 0.16F * tableScale) {
            insertLoot(inventory, new ItemStack(Items.NETHERITE_SCRAP, 1 + random.nextInt(2)));
        }
        if (random.nextFloat() < 0.045F * tableScale) {
            insertLoot(inventory, new ItemStack(Items.NETHERITE_INGOT));
        }
    }

    /**
     * Adds a low-weight enchanted book reward without depending on custom scroll items that may not exist yet.
     */
    private static void addBookLoot(ServerWorld world, Inventory inventory, Random random, float tableScale) {
        if (random.nextFloat() >= 0.12F * tableScale) {
            return;
        }

        Registry<Enchantment> enchantmentRegistry = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        RegistryEntry<Enchantment> enchantmentEntry;
        int level;

        switch (random.nextInt(4)) {
            case 0 -> {
                enchantmentEntry = enchantmentRegistry.entryOf(Enchantments.MENDING);
                level = 1;
            }
            case 1 -> {
                enchantmentEntry = enchantmentRegistry.entryOf(Enchantments.SWIFT_SNEAK);
                level = 1 + random.nextInt(3);
            }
            case 2 -> {
                enchantmentEntry = enchantmentRegistry.entryOf(Enchantments.UNBREAKING);
                level = 2 + random.nextInt(2);
            }
            default -> {
                enchantmentEntry = enchantmentRegistry.entryOf(Enchantments.SOUL_SPEED);
                level = 1 + random.nextInt(3);
            }
        }

        insertLoot(inventory, EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantmentEntry, level)));
    }

    /**
     * Adds a very rare netherite-tier weapon or armor piece, usually with a random enchantment.
     */
    private static void addEquipmentLoot(ServerWorld world, Inventory inventory, Random random, float tableScale) {
        if (random.nextFloat() >= 0.035F * tableScale) {
            return;
        }

        ItemStack reward = random.nextBoolean()
                ? new ItemStack(Items.NETHERITE_SWORD)
                : new ItemStack(Items.NETHERITE_CHESTPLATE);

        if (random.nextFloat() < 0.75F) {
            reward = EnchantmentHelper.enchant(
                    random,
                    reward,
                    18 + random.nextInt(10),
                    world.getRegistryManager(),
                    Optional.empty()
            );
        }

        insertLoot(inventory, reward);
    }

    /**
     * Gives the city its one guaranteed token on the first Ancient City chest that gets generated for that city.
     */
    private static void ensureGuaranteedToken(ServerWorld world, Inventory inventory, AncientCityKey cityKey) {
        AncientCityPersistentState persistentState = AncientCityPersistentState.get(world);
        if (persistentState.hasGrantedToken(cityKey)) {
            return;
        }

        insertLoot(inventory, new ItemStack(ModItem.WARDEN_TOKEN));
        persistentState.markTokenGranted(cityKey);
    }

    /**
     * Inserts bonus loot into the chest, reusing empty slots created by trimming when possible.
     */
    private static void insertLoot(Inventory inventory, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack current = inventory.getStack(slot);
            if (current.isEmpty()) {
                inventory.setStack(slot, stack);
                return;
            }

            if (ItemStack.areItemsAndComponentsEqual(current, stack) && current.getCount() + stack.getCount() <= current.getMaxCount()) {
                current.increment(stack.getCount());
                return;
            }
        }
    }

    /**
     * Per-world transient state for scent trails and Warden suspicion.
     */
    private static final class WorldRuntime {
        private final Map<UUID, ArrayDeque<ScentMarker>> markersByPlayer = new HashMap<>();
        private final Map<UUID, PlayerTrailState> trailStates = new HashMap<>();
        private final Map<AncientCityKey, CityRuntimeState> cityStates = new HashMap<>();

        /**
         * Advances all transient systems in a deterministic order so scent trails and Warden locks stay coherent.
         */
        private void tick(ServerWorld world) {
            long now = world.getTime();
            expireOldMarkers(now);
            updatePlayerTrails(world, now);
            decayCitySuspicion();
            tickWardens(world, now);
            cleanup(world, now);
        }

        /**
         * Drops expired scent markers from the front of each player queue to keep memory bounded.
         */
        private void expireOldMarkers(long now) {
            Iterator<Map.Entry<UUID, ArrayDeque<ScentMarker>>> iterator = this.markersByPlayer.entrySet().iterator();
            while (iterator.hasNext()) {
                ArrayDeque<ScentMarker> markers = iterator.next().getValue();
                while (!markers.isEmpty() && markers.peekFirst().expiryTick() <= now) {
                    markers.removeFirst();
                }
                if (markers.isEmpty()) {
                    iterator.remove();
                }
            }
        }

        /**
         * Leaves a new scent marker only when a player actually steps onto a new supporting block inside a city.
         */
        private void updatePlayerTrails(ServerWorld world, long now) {
            for (ServerPlayerEntity player : world.getPlayers(candidate -> candidate.isAlive() && !candidate.isSpectator())) {
                BlockPos steppingPos = player.getSteppingPos();
                PlayerTrailState state = this.trailStates.computeIfAbsent(player.getUuid(), ignored -> new PlayerTrailState());
                if (steppingPos.equals(state.lastSteppingPos)) {
                    continue;
                }

                state.lastSteppingPos = steppingPos;
                Optional<AncientCityLocator.CityReference> city = AncientCityLocator.locate(world, steppingPos);
                if (city.isEmpty()) {
                    continue;
                }

                addMarker(player.getUuid(), city.get().key(), steppingPos, now + SCENT_LIFETIME_TICKS);
            }
        }

        /**
         * Adds a scent marker and trims the oldest markers for that player when the configured cap is reached.
         */
        private void addMarker(UUID playerUuid, AncientCityKey cityKey, BlockPos steppingPos, long expiryTick) {
            ArrayDeque<ScentMarker> markers = this.markersByPlayer.computeIfAbsent(playerUuid, ignored -> new ArrayDeque<>());
            markers.addLast(new ScentMarker(cityKey, steppingPos.toImmutable(), expiryTick));

            while (markers.size() > MAX_SCENT_MARKERS_PER_PLAYER) {
                markers.removeFirst();
            }
        }

        /**
         * Slowly bleeds suspicion away so a Warden needs a sustained trail instead of a single burst.
         */
        private void decayCitySuspicion() {
            for (CityRuntimeState cityState : this.cityStates.values()) {
                float decay = cityState.lockedTargetUuid == null ? SUSPICION_DECAY_PER_TICK : LOCKED_SUSPICION_DECAY_PER_TICK;
                Iterator<Map.Entry<UUID, Float>> iterator = cityState.suspicionByPlayer.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<UUID, Float> entry = iterator.next();
                    float updatedValue = entry.getValue() - decay;
                    if (updatedValue <= 0.05F) {
                        iterator.remove();
                    } else {
                        entry.setValue(updatedValue);
                    }
                }

                if (cityState.lockedTargetUuid != null
                        && cityState.suspicionByPlayer.getOrDefault(cityState.lockedTargetUuid, 0.0F) < LOCK_RELEASE_THRESHOLD) {
                    cityState.lockedTargetUuid = null;
                }
            }
        }

        /**
         * Lets every Warden in an Ancient City sniff nearby scent markers and aggressively chase a locked target.
         */
        private void tickWardens(ServerWorld world, long now) {
            List<? extends WardenEntity> wardens = world.getEntitiesByType(TypeFilter.instanceOf(WardenEntity.class), LivingEntity::isAlive);
            for (WardenEntity warden : wardens) {
                Optional<AncientCityLocator.CityReference> city = AncientCityLocator.locate(world, warden.getBlockPos());
                if (city.isEmpty()) {
                    continue;
                }

                CityRuntimeState cityState = this.cityStates.computeIfAbsent(city.get().key(), ignored -> new CityRuntimeState());
                cityState.wardenUuid = warden.getUuid();
                cityState.lastSeenTick = now;

                if (now >= cityState.nextScanTick) {
                    detectNearbyMarkers(world, warden, city.get(), cityState);
                    cityState.nextScanTick = now + WARDEN_SCAN_INTERVAL_TICKS;
                }

                refreshLockedTarget(world, warden, city.get(), cityState, now);
            }
        }

        /**
         * Consumes a few nearby markers each scan so the Warden follows the trail instead of instantly solving it.
         */
        private void detectNearbyMarkers(ServerWorld world, WardenEntity warden, AncientCityLocator.CityReference city, CityRuntimeState cityState) {
            for (Map.Entry<UUID, ArrayDeque<ScentMarker>> markerEntry : this.markersByPlayer.entrySet()) {
                UUID ownerUuid = markerEntry.getKey();
                ArrayDeque<ScentMarker> markers = markerEntry.getValue();

                if (world.getPlayerByUuid(ownerUuid) == null) {
                    removeMarkersForCity(markers, city.key());
                    continue;
                }

                int detectedMarkers = 0;
                Iterator<ScentMarker> iterator = markers.iterator();
                while (iterator.hasNext() && detectedMarkers < MAX_MARKERS_DETECTED_PER_SCAN) {
                    ScentMarker marker = iterator.next();
                    if (!marker.cityKey().equals(city.key())) {
                        continue;
                    }

                    if (marker.pos().getSquaredDistance(warden.getX(), warden.getY(), warden.getZ()) > WARDEN_SCENT_RADIUS * WARDEN_SCENT_RADIUS) {
                        continue;
                    }

                    iterator.remove();
                    detectedMarkers++;
                }

                if (detectedMarkers > 0) {
                    cityState.suspicionByPlayer.merge(ownerUuid, (float) detectedMarkers, Float::sum);
                }
            }

            UUID topSuspect = null;
            float topSuspicion = 0.0F;
            for (Map.Entry<UUID, Float> suspicionEntry : cityState.suspicionByPlayer.entrySet()) {
                if (suspicionEntry.getValue() > topSuspicion) {
                    topSuspicion = suspicionEntry.getValue();
                    topSuspect = suspicionEntry.getKey();
                }
            }

            if (topSuspect != null && topSuspicion >= LOCK_ON_THRESHOLD) {
                cityState.lockedTargetUuid = topSuspect;
            }
        }

        /**
         * Forces the Warden to keep pressuring the locked player even if vanilla anger logic would normally cool off.
         */
        private void refreshLockedTarget(
                ServerWorld world,
                WardenEntity warden,
                AncientCityLocator.CityReference city,
                CityRuntimeState cityState,
                long now
        ) {
            if (cityState.lockedTargetUuid == null) {
                return;
            }

            ServerPlayerEntity target = world.getPlayerByUuid(cityState.lockedTargetUuid) instanceof ServerPlayerEntity serverPlayer
                    ? serverPlayer
                    : null;
            if (target == null || !target.isAlive() || !city.boundingBox().contains(target.getBlockPos())) {
                cityState.lockedTargetUuid = null;
                return;
            }

            warden.updateAttackTarget(target);
            warden.setTarget(target);

            if (now % WARDEN_LOCK_REFRESH_INTERVAL == 0L) {
                warden.getNavigation().startMovingTo(target, 1.25D);
            }
            if (now % 20L == 0L) {
                warden.increaseAngerAt(target, 35, true);
            }
        }

        /**
         * Removes stale players and city states once their markers, suspicion and Wardens have all gone away.
         */
        private void cleanup(ServerWorld world, long now) {
            this.trailStates.keySet().removeIf(uuid -> world.getPlayerByUuid(uuid) == null);
            this.cityStates.entrySet().removeIf(entry -> {
                CityRuntimeState cityState = entry.getValue();
                boolean wardenExpired = cityState.wardenUuid == null || now - cityState.lastSeenTick > 200L;
                return wardenExpired && cityState.suspicionByPlayer.isEmpty() && cityState.lockedTargetUuid == null;
            });
        }

        /**
         * Clears all markers for one city when the owner is gone so the queues do not retain dead references.
         */
        private void removeMarkersForCity(ArrayDeque<ScentMarker> markers, AncientCityKey cityKey) {
            markers.removeIf(marker -> marker.cityKey().equals(cityKey));
        }
    }

    /**
     * Per-city runtime data shared by all scent systems in that city.
     */
    private static final class CityRuntimeState {
        private final Map<UUID, Float> suspicionByPlayer = new HashMap<>();
        private UUID wardenUuid;
        private UUID lockedTargetUuid;
        private long nextScanTick;
        private long lastSeenTick;
    }

    /**
     * Tracks the last stepping block we processed for a player so idle players do not spam scent markers.
     */
    private static final class PlayerTrailState {
        private BlockPos lastSteppingPos = BlockPos.ORIGIN;
    }

    /**
     * Immutable scent marker entry stored in the capped per-player queue.
     */
    private record ScentMarker(AncientCityKey cityKey, BlockPos pos, long expiryTick) {
    }
}
