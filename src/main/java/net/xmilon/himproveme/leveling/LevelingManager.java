package net.xmilon.himproveme.leveling;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.access.LevelingStateHolder;
import net.xmilon.himproveme.item.ModItem;
import net.xmilon.himproveme.item.custom.BlowgunItem;
import net.xmilon.himproveme.network.leveling.LevelingNetworking;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class LevelingManager {
    public static final int DEFAULT_MAX_LEVEL = 60;
    public static final int MIN_MAX_LEVEL = 5;
    public static final int HARD_MAX_LEVEL = 200;

    private static final long PASSIVE_INTERVAL_TICKS = 1800L;
    private static final long PASSIVE_XP_PER_INTERVAL = 4L;
    private static final long CHEST_DISCOVERY_XP = 8L;
    private static final double VANILLA_XP_CONVERSION_RATE = 0.30D;
    private static final double KILL_REWARD_HEALTH_MULTIPLIER = 0.22D;
    private static final long MONSTER_KILL_BONUS = 2L;
    private static final Identifier MOB_LEVEL_DAMAGE_MODIFIER_ID = Identifier.of(HimProveMe.MOD_ID, "leveling_mob_damage");
    private static final Map<Merchant, int[]> APPLIED_MERCHANT_PRICES = new IdentityHashMap<>();

    private LevelingManager() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(LevelingManager::tickPlayers);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(handler.player));
    }

    public static LevelingConfigState getConfig(MinecraftServer server) {
        return LevelingConfigState.get(server);
    }

    public static int clampMaxLevel(int maxLevel) {
        return Math.max(MIN_MAX_LEVEL, Math.min(HARD_MAX_LEVEL, maxLevel));
    }

    public static LevelingState getState(PlayerEntity player) {
        if (player instanceof LevelingStateHolder holder) {
            return holder.himproveme$getLevelingState();
        }
        return new LevelingState();
    }

    public static void syncAll(MinecraftServer server) {
        LevelingNetworking.syncAll(server);
    }

    public static void clampAndSyncAllPlayers(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            clampStateToConfig(player, getConfig(server));
        }
        syncAll(server);
    }

    public static void setLevel(ServerPlayerEntity player, int level) {
        LevelingConfigState config = getConfig(player.getServer());
        LevelingState state = getState(player);
        int clampedLevel = Math.max(1, Math.min(config.getMaxLevel(), level));
        state.setLevel(clampedLevel);
        state.setCurrentLevelXp(0L);
        state.setTotalLevelingXp(getTotalXpToReachLevel(clampedLevel));
        LevelingNetworking.sync(player);
    }

    public static void addAdminXp(ServerPlayerEntity player, long amount) {
        addProgressXp(player, amount, false);
    }

    public static long getXpNeededForNextLevel(ServerPlayerEntity player) {
        return getXpNeededForNextLevel(getState(player).getLevel(), getConfig(player.getServer()).getMaxLevel());
    }

    public static long getXpNeededForNextLevel(int level, int maxLevel) {
        if (level >= maxLevel) {
            return 0L;
        }

        int index = Math.max(0, level - 1);
        long linear = index * 34L;
        long quadratic = (long) index * index * 6L;
        long cubic = (long) index * index * index / 30L;
        return 130L + linear + quadratic + cubic;
    }

    public static long getTotalXpToReachLevel(int level) {
        long total = 0L;
        for (int currentLevel = 1; currentLevel < Math.max(1, level); currentLevel++) {
            total += getXpNeededForNextLevel(currentLevel, Integer.MAX_VALUE);
        }
        return total;
    }

    public static NbtCompound createSyncData(ServerPlayerEntity player) {
        LevelingConfigState config = getConfig(player.getServer());
        clampStateToConfig(player, config);

        LevelingState state = getState(player);
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("Level", state.getLevel());
        nbt.putLong("CurrentLevelXp", state.getCurrentLevelXp());
        nbt.putLong("NextLevelXp", getXpNeededForNextLevel(state.getLevel(), config.getMaxLevel()));
        nbt.putLong("TotalLevelingXp", state.getTotalLevelingXp());
        nbt.putLong("TotalPlayTicks", state.getTotalPlayTicks());
        nbt.putInt("MaxLevel", config.getMaxLevel());
        nbt.putBoolean("Enabled", config.isEnabled());
        return nbt;
    }

    public static void onVanillaExperienceGain(ServerPlayerEntity player, int amount) {
        if (amount <= 0) {
            return;
        }

        long levelingXp = Math.max(1L, Math.round(amount * VANILLA_XP_CONVERSION_RATE));
        addScaledProgressXp(player, levelingXp, true);
    }

    public static void onKilledEntity(ServerPlayerEntity player, LivingEntity entity) {
        long reward = Math.max(3L, Math.round(entity.getMaxHealth() * KILL_REWARD_HEALTH_MULTIPLIER));
        if (entity instanceof Monster) {
            reward += MONSTER_KILL_BONUS;
        }
        if (entity.isBaby()) {
            reward = Math.max(1L, reward / 2L);
        }
        addScaledProgressXp(player, reward, true);
    }

    public static void onTradeCompleted(ServerPlayerEntity player, TradeOffer offer) {
        int firstCost = offer.getFirstBuyItem().count();
        int secondCost = offer.getSecondBuyItem().map(TradedItem::count).orElse(0);
        long reward = 4L + firstCost + secondCost + offer.getMerchantExperience() * 2L;
        addScaledProgressXp(player, reward, true);
    }

    public static void onLootGenerated(ServerPlayerEntity player, LootableInventory lootableInventory) {
        MinecraftServer server = player.getServer();
        if (server == null || !getConfig(server).isEnabled()) {
            return;
        }

        int level = getState(player).getLevel();
        if (level >= 10) {
            improveLoot(lootableInventory, player, level);
        }

        addScaledProgressXp(player, CHEST_DISCOVERY_XP + Math.max(0, level - 1) / 12L, true);
    }

    public static void applyMerchantScaling(ServerPlayerEntity player, Merchant merchant) {
        MinecraftServer server = player.getServer();
        if (server == null || !getConfig(server).isEnabled() || APPLIED_MERCHANT_PRICES.containsKey(merchant)) {
            return;
        }

        int discount = getTradeDiscount(getState(player).getLevel(), merchant);
        if (discount <= 0) {
            return;
        }

        TradeOfferList offers = merchant.getOffers();
        if (offers.isEmpty()) {
            return;
        }

        int[] specialPrices = new int[offers.size()];
        for (int i = 0; i < offers.size(); i++) {
            TradeOffer offer = offers.get(i);
            specialPrices[i] = offer.getSpecialPrice();
            offer.setSpecialPrice(specialPrices[i] - discount);
        }

        APPLIED_MERCHANT_PRICES.put(merchant, specialPrices);
    }

    public static void clearMerchantScaling(Merchant merchant) {
        int[] specialPrices = APPLIED_MERCHANT_PRICES.remove(merchant);
        if (specialPrices == null) {
            return;
        }

        TradeOfferList offers = merchant.getOffers();
        for (int i = 0; i < offers.size() && i < specialPrices.length; i++) {
            offers.get(i).setSpecialPrice(specialPrices[i]);
        }
    }

    public static void applyMobScaling(MobEntity mob, ServerWorldAccess world, LocalDifficulty difficulty) {
        ServerWorld serverWorld = world.toServerWorld();
        if (!(mob instanceof Monster) || !isGearEligible(mob)) {
            return;
        }

        MinecraftServer server = serverWorld.getServer();
        LevelingConfigState config = getConfig(server);
        if (!config.isEnabled()) {
            return;
        }

        int nearbyLevel = getNearbyLevel(serverWorld, mob.getPos());
        int effectiveLevel = getEffectiveMobLevel(nearbyLevel, config.getMaxLevel(), server);
        if (effectiveLevel <= 1) {
            return;
        }

        float normalized = getNormalizedLevel(effectiveLevel, config.getMaxLevel());
        Random random = serverWorld.getRandom();
        int armorTier = pickArmorTier(effectiveLevel, normalized, random);

        applyArmorScaling(mob, armorTier, normalized, random);
        applyWeaponScaling(mob, armorTier, normalized, random);
        applyAttributeScaling(mob, normalized, server);
        applyEnchantmentScaling(mob, effectiveLevel, normalized, serverWorld, random);
        applyDropChanceScaling(mob, normalized);
    }

    public static void grantGuideBook(ServerPlayerEntity player) {
        ItemStack book = createGuideBook();
        player.getInventory().offerOrDrop(book);
        player.sendMessage(Text.literal("You received the Leveling Guide.").formatted(Formatting.YELLOW), false);
    }

    public static GuideBookClaimResult claimGuideBook(ServerPlayerEntity player) {
        LevelingState state = getState(player);
        long currentDay = getCurrentWorldDay(player);
        if (state.getLastGuideBookClaimDay() == currentDay) {
            long remaining = getTicksUntilNextDay(player);
            player.sendMessage(
                    Text.literal("You can claim another leveling guide in " + formatDuration(remaining) + ".")
                            .formatted(Formatting.RED),
                    false
            );
            return GuideBookClaimResult.TOO_SOON;
        }

        state.setLastGuideBookClaimDay(currentDay);
        grantGuideBook(player);
        LevelingNetworking.sync(player);
        return GuideBookClaimResult.GIVEN;
    }

    public static String formatPlayTime(long ticks) {
        long totalSeconds = ticks / 20L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        if (hours > 0L) {
            return minutes > 0L ? hours + "h " + minutes + "m" : hours + "h";
        }
        return Math.max(1L, minutes) + "m";
    }

    public enum GuideBookClaimResult {
        GIVEN,
        TOO_SOON
    }

    private static void onPlayerJoin(ServerPlayerEntity player) {
        LevelingState state = getState(player);
        clampStateToConfig(player, getConfig(player.getServer()));
        if (!state.hasReceivedGuideBook()) {
            state.setGuideBookReceived(true);
            grantGuideBook(player);
        }
        LevelingNetworking.sync(player);
    }

    private static void tickPlayers(MinecraftServer server) {
        LevelingConfigState config = getConfig(server);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            LevelingState state = getState(player);
            state.setTotalPlayTicks(state.getTotalPlayTicks() + 1L);

            if (!config.isEnabled()) {
                state.setPassiveTickBuffer(0L);
                continue;
            }

            long updatedBuffer = state.getPassiveTickBuffer() + 1L;
            if (updatedBuffer >= PASSIVE_INTERVAL_TICKS) {
                long intervals = updatedBuffer / PASSIVE_INTERVAL_TICKS;
                state.setPassiveTickBuffer(updatedBuffer % PASSIVE_INTERVAL_TICKS);
                addScaledProgressXp(player, intervals * PASSIVE_XP_PER_INTERVAL, true);
            } else {
                state.setPassiveTickBuffer(updatedBuffer);
            }
        }
    }

    private static boolean addScaledProgressXp(ServerPlayerEntity player, long amount, boolean silent) {
        MinecraftServer server = player.getServer();
        if (server == null || amount <= 0L) {
            return false;
        }

        long scaledAmount = Math.max(1L, Math.round(amount * getProgressionRewardMultiplier(server)));
        return addProgressXp(player, scaledAmount, silent);
    }

    private static boolean addProgressXp(ServerPlayerEntity player, long amount, boolean silent) {
        MinecraftServer server = player.getServer();
        if (server == null || amount <= 0L) {
            return false;
        }

        LevelingConfigState config = getConfig(server);
        if (!config.isEnabled()) {
            return false;
        }

        clampStateToConfig(player, config);
        LevelingState state = getState(player);
        if (state.getLevel() >= config.getMaxLevel()) {
            return false;
        }

        state.setTotalLevelingXp(state.getTotalLevelingXp() + amount);
        state.setCurrentLevelXp(state.getCurrentLevelXp() + amount);

        boolean leveledUp = false;
        while (state.getLevel() < config.getMaxLevel()) {
            long required = getXpNeededForNextLevel(state.getLevel(), config.getMaxLevel());
            if (required <= 0L || state.getCurrentLevelXp() < required) {
                break;
            }

            state.setCurrentLevelXp(state.getCurrentLevelXp() - required);
            state.setLevel(state.getLevel() + 1);
            leveledUp = true;

            int vanillaReward = getVanillaLevelReward(state.getLevel());
            if (vanillaReward > 0) {
                player.addExperienceLevels(vanillaReward);
            }
        }

        if (state.getLevel() >= config.getMaxLevel()) {
            state.setLevel(config.getMaxLevel());
            state.setCurrentLevelXp(0L);
        }

        if (leveledUp) {
            player.sendMessage(
                    Text.literal("Level Up! You are now level " + state.getLevel() + ".").formatted(Formatting.GOLD),
                    true
            );
        }

        if (leveledUp) {
            player.playSoundToPlayer(
                    SoundEvents.ENTITY_PLAYER_LEVELUP,
                    SoundCategory.PLAYERS,
                    0.55f,
                    0.95f + player.getRandom().nextFloat() * 0.15f
            );
        }

        LevelingNetworking.sync(player);
        return true;
    }

    private static boolean clampStateToConfig(ServerPlayerEntity player, LevelingConfigState config) {
        LevelingState state = getState(player);
        int clampedLevel = Math.max(1, Math.min(config.getMaxLevel(), state.getLevel()));
        boolean changed = false;
        if (state.getLevel() != clampedLevel) {
            state.setLevel(clampedLevel);
            state.setCurrentLevelXp(0L);
            changed = true;
        }

        if (state.getTotalLevelingXp() < getTotalXpToReachLevel(state.getLevel())) {
            state.setTotalLevelingXp(getTotalXpToReachLevel(state.getLevel()) + state.getCurrentLevelXp());
            changed = true;
        }

        if (state.getLevel() >= config.getMaxLevel() && state.getCurrentLevelXp() != 0L) {
            state.setCurrentLevelXp(0L);
            changed = true;
        }

        if (changed) {
            LevelingNetworking.sync(player);
        }
        return changed;
    }

    private static int getVanillaLevelReward(int customLevel) {
        if (customLevel < 8) {
            return 0;
        }
        if (customLevel < 20) {
            return 1;
        }
        if (customLevel < 35) {
            return 2;
        }
        if (customLevel < 50) {
            return 3;
        }
        return 4;
    }

    private static float getNormalizedLevel(int level, int maxLevel) {
        if (maxLevel <= 1) {
            return 1.0f;
        }
        return Math.min(1.0f, Math.max(0.0f, (level - 1.0f) / (maxLevel - 1.0f)));
    }

    private static double getProgressionRewardMultiplier(MinecraftServer server) {
        if (server.isHardcore()) {
            return 1.18D;
        }

        return switch (server.getSaveProperties().getDifficulty()) {
            case PEACEFUL -> 0.72D;
            case EASY -> 0.82D;
            case NORMAL -> 1.0D;
            case HARD -> 1.10D;
        };
    }

    private static int getEffectiveMobLevel(int nearbyLevel, int maxLevel, MinecraftServer server) {
        float multiplier;
        if (server.isHardcore()) {
            multiplier = 1.10f;
        } else {
            multiplier = switch (server.getSaveProperties().getDifficulty()) {
                case PEACEFUL -> 0.30f;
                case EASY -> 0.55f;
                case NORMAL -> 0.75f;
                case HARD -> 0.95f;
            };
        }

        int scaledLevel = 1 + Math.round((Math.max(1, nearbyLevel) - 1) * multiplier);
        return Math.max(1, Math.min(maxLevel, scaledLevel));
    }

    private static double getMobAttackDamageCap(MinecraftServer server) {
        if (server.isHardcore()) {
            return 2.0D;
        }

        Difficulty difficulty = server.getSaveProperties().getDifficulty();
        return switch (difficulty) {
            case PEACEFUL -> 0.0D;
            case EASY -> 0.6D;
            case NORMAL -> 1.0D;
            case HARD -> 1.5D;
        };
    }

    private static int getNearbyLevel(ServerWorld world, Vec3d position) {
        List<ServerPlayerEntity> players = world.getPlayers(player -> player.isAlive() && !player.isSpectator());
        ServerPlayerEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (ServerPlayerEntity player : players) {
            double distance = player.getPos().squaredDistanceTo(position);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = player;
            }
        }

        if (nearest != null && nearestDistance <= 96.0D * 96.0D) {
            return getState(nearest).getLevel();
        }

        if (players.isEmpty()) {
            return 1;
        }

        int total = 0;
        for (ServerPlayerEntity player : players) {
            total += getState(player).getLevel();
        }
        return Math.max(1, Math.round((float) total / players.size()));
    }

    private static boolean isGearEligible(MobEntity mob) {
        return mob instanceof ZombieEntity
                || mob instanceof AbstractSkeletonEntity
                || mob instanceof PillagerEntity
                || mob instanceof VindicatorEntity
                || mob instanceof PiglinEntity
                || mob instanceof PiglinBruteEntity
                || mob instanceof ZombifiedPiglinEntity;
    }

    private static int pickArmorTier(int level, float normalized, Random random) {
        int tier = 0;
        if (level >= 10) {
            tier = 1;
        }
        if (level >= 20) {
            tier = 2;
        }
        if (level >= 32) {
            tier = 3;
        }
        if (level >= 46) {
            tier = 4;
        }
        if (tier < 4 && random.nextFloat() < normalized * 0.12f) {
            tier++;
        }
        return Math.min(4, tier);
    }

    private static void applyArmorScaling(MobEntity mob, int armorTier, float normalized, Random random) {
        EquipmentSlot[] slots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < slots.length; i++) {
            EquipmentSlot slot = slots[i];
            if (random.nextFloat() > 0.10f + normalized * 0.36f - i * 0.03f) {
                continue;
            }

            ItemStack targetStack = createArmorForSlot(slot, armorTier);
            if (targetStack.isEmpty()) {
                continue;
            }

            ItemStack currentStack = mob.getEquippedStack(slot);
            if (currentStack.isEmpty() || random.nextFloat() < normalized * 0.35f) {
                mob.equipStack(slot, targetStack);
            }
        }
    }

    private static void applyWeaponScaling(MobEntity mob, int armorTier, float normalized, Random random) {
        ItemStack targetWeapon = createWeaponForMob(mob, armorTier, random);
        if (targetWeapon.isEmpty()) {
            return;
        }

        ItemStack currentWeapon = mob.getEquippedStack(EquipmentSlot.MAINHAND);
        if (currentWeapon.isEmpty()
                || shouldReplaceWeapon(currentWeapon, targetWeapon)
                || mob instanceof HuskEntity && BlowgunItem.isBlowgun(targetWeapon)
                || normalized > 0.92f) {
            mob.equipStack(EquipmentSlot.MAINHAND, targetWeapon);
        }
    }

    private static void applyAttributeScaling(MobEntity mob, float normalized, MinecraftServer server) {
        EntityAttributeInstance attackDamage = mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (attackDamage == null) {
            return;
        }

        double maxBonus = getMobAttackDamageCap(server);
        if (maxBonus <= 0.0D) {
            attackDamage.removeModifier(MOB_LEVEL_DAMAGE_MODIFIER_ID);
            return;
        }

        double bonus = maxBonus * Math.pow(normalized, 1.15D);
        if (bonus < 0.15D) {
            attackDamage.removeModifier(MOB_LEVEL_DAMAGE_MODIFIER_ID);
            return;
        }

        attackDamage.updateModifier(new EntityAttributeModifier(MOB_LEVEL_DAMAGE_MODIFIER_ID, bonus, EntityAttributeModifier.Operation.ADD_VALUE));
    }

    private static void applyEnchantmentScaling(MobEntity mob, int level, float normalized, ServerWorld serverWorld, Random random) {
        int enchantPower = 5 + level / 3;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.OFFHAND || slot == EquipmentSlot.BODY) {
                continue;
            }

            ItemStack stack = mob.getEquippedStack(slot);
            if (stack.isEmpty() || stack.getCount() != 1 || !stack.isEnchantable()) {
                continue;
            }

            float chance = slot == EquipmentSlot.MAINHAND ? 0.05f + normalized * 0.16f : 0.02f + normalized * 0.12f;
            if (random.nextFloat() > chance) {
                continue;
            }

            mob.equipStack(
                    slot,
                    EnchantmentHelper.enchant(random, stack.copy(), enchantPower, serverWorld.getRegistryManager(), Optional.empty())
            );
        }
    }

    private static void applyDropChanceScaling(MobEntity mob, float normalized) {
        float dropChance = Math.min(0.18f, 0.06f + normalized * 0.08f);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.BODY) {
                continue;
            }
            if (!mob.getEquippedStack(slot).isEmpty()) {
                mob.setEquipmentDropChance(slot, dropChance);
            }
        }
    }

    private static ItemStack createArmorForSlot(EquipmentSlot slot, int armorTier) {
        return switch (slot) {
            case HEAD -> new ItemStack(switch (armorTier) {
                case 0 -> Items.LEATHER_HELMET;
                case 1 -> Items.GOLDEN_HELMET;
                case 2 -> Items.CHAINMAIL_HELMET;
                case 3 -> Items.IRON_HELMET;
                default -> Items.DIAMOND_HELMET;
            });
            case CHEST -> new ItemStack(switch (armorTier) {
                case 0 -> Items.LEATHER_CHESTPLATE;
                case 1 -> Items.GOLDEN_CHESTPLATE;
                case 2 -> Items.CHAINMAIL_CHESTPLATE;
                case 3 -> Items.IRON_CHESTPLATE;
                default -> Items.DIAMOND_CHESTPLATE;
            });
            case LEGS -> new ItemStack(switch (armorTier) {
                case 0 -> Items.LEATHER_LEGGINGS;
                case 1 -> Items.GOLDEN_LEGGINGS;
                case 2 -> Items.CHAINMAIL_LEGGINGS;
                case 3 -> Items.IRON_LEGGINGS;
                default -> Items.DIAMOND_LEGGINGS;
            });
            case FEET -> new ItemStack(switch (armorTier) {
                case 0 -> Items.LEATHER_BOOTS;
                case 1 -> Items.GOLDEN_BOOTS;
                case 2 -> Items.CHAINMAIL_BOOTS;
                case 3 -> Items.IRON_BOOTS;
                default -> Items.DIAMOND_BOOTS;
            });
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack createWeaponForMob(MobEntity mob, int armorTier, Random random) {
        if (mob instanceof HuskEntity) {
            return new ItemStack(ModItem.BLOWGUN);
        }
        if (mob instanceof AbstractSkeletonEntity) {
            return new ItemStack(random.nextFloat() < 0.35f ? ModItem.BLOWGUN : Items.BOW);
        }
        if (mob instanceof PillagerEntity) {
            return new ItemStack(Items.CROSSBOW);
        }
        if (mob instanceof VindicatorEntity || mob instanceof PiglinBruteEntity) {
            ItemStack spear = createSpearForMob(armorTier, random, 0.18f);
            if (!spear.isEmpty()) {
                return spear;
            }
            return new ItemStack(armorTier >= 4 ? Items.DIAMOND_AXE : Items.IRON_AXE);
        }
        if (mob instanceof PiglinEntity || mob instanceof ZombifiedPiglinEntity) {
            ItemStack spear = createSpearForMob(armorTier, random, 0.24f);
            if (!spear.isEmpty()) {
                return spear;
            }
            return new ItemStack(armorTier <= 1 ? Items.GOLDEN_SWORD : armorTier >= 4 ? Items.DIAMOND_SWORD : Items.IRON_SWORD);
        }
        if (mob instanceof ZombieEntity) {
            ItemStack spear = createSpearForMob(armorTier, random, 0.20f);
            if (!spear.isEmpty()) {
                return spear;
            }
            return new ItemStack(armorTier == 0 ? Items.STONE_SWORD : armorTier >= 4 ? Items.DIAMOND_SWORD : Items.IRON_SWORD);
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack createSpearForMob(int armorTier, Random random, float chance) {
        if (armorTier < 3 || random.nextFloat() >= chance) {
            return ItemStack.EMPTY;
        }
        if (ModItem.DOUBLE_ENDER_SPEAR != null && armorTier >= 4 && random.nextFloat() < 0.4f) {
            return new ItemStack(ModItem.DOUBLE_ENDER_SPEAR);
        }
        if (ModItem.ENDER_SPEAR != null) {
            return new ItemStack(ModItem.ENDER_SPEAR);
        }
        if (ModItem.DOUBLE_ENDER_SPEAR != null) {
            return new ItemStack(ModItem.DOUBLE_ENDER_SPEAR);
        }
        return ItemStack.EMPTY;
    }

    private static boolean shouldReplaceWeapon(ItemStack currentWeapon, ItemStack targetWeapon) {
        if (currentWeapon.isEmpty()) {
            return true;
        }
        if (ItemStack.areItemsAndComponentsEqual(currentWeapon, targetWeapon)) {
            return false;
        }
        if (isWeakMeleeWeapon(currentWeapon)) {
            return true;
        }
        return isRangedWeapon(currentWeapon) && isRangedWeapon(targetWeapon);
    }

    private static boolean isWeakMeleeWeapon(ItemStack stack) {
        return stack.isOf(Items.WOODEN_SWORD)
                || stack.isOf(Items.STONE_SWORD)
                || stack.isOf(Items.GOLDEN_SWORD)
                || stack.isOf(Items.WOODEN_AXE)
                || stack.isOf(Items.STONE_AXE)
                || stack.isOf(Items.GOLDEN_AXE);
    }

    private static boolean isRangedWeapon(ItemStack stack) {
        return stack.isOf(Items.BOW)
                || stack.isOf(Items.CROSSBOW)
                || BlowgunItem.isBlowgun(stack);
    }

    private static void improveLoot(LootableInventory lootableInventory, ServerPlayerEntity player, int level) {
        Inventory inventory = (Inventory) lootableInventory;
        Random random = player.getRandom();
        float normalized = getNormalizedLevel(level, getConfig(player.getServer()).getMaxLevel());

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getCount() == 1 && stack.isEnchantable() && random.nextFloat() < 0.02f + normalized * 0.10f) {
                inventory.setStack(i, EnchantmentHelper.enchant(
                        random,
                        stack.copy(),
                        5 + level / 3,
                        player.getRegistryManager(),
                        Optional.empty()
                ));
                continue;
            }

            if (stack.isOf(Items.EMERALD) && random.nextFloat() < 0.08f + normalized * 0.07f) {
                stack.increment(1 + random.nextInt(1 + Math.max(0, level / 28)));
                continue;
            }

            if (stack.isOf(Items.IRON_INGOT) && random.nextFloat() < 0.05f + normalized * 0.05f) {
                stack.increment(1 + random.nextInt(1 + Math.max(0, level / 32)));
            }
        }

        if (random.nextFloat() < 0.04f + normalized * 0.08f) {
            insertBonusLoot(inventory, createBonusLoot(player, level, random));
        }
        if (level >= 48 && random.nextFloat() < 0.02f + normalized * 0.05f) {
            insertBonusLoot(inventory, createBonusLoot(player, level, random));
        }
    }

    private static void insertBonusLoot(Inventory inventory, @Nullable ItemStack bonusLoot) {
        if (bonusLoot == null || bonusLoot.isEmpty()) {
            return;
        }

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack current = inventory.getStack(i);
            if (current.isEmpty()) {
                inventory.setStack(i, bonusLoot);
                return;
            }

            if (ItemStack.areItemsAndComponentsEqual(current, bonusLoot) && current.getCount() + bonusLoot.getCount() <= current.getMaxCount()) {
                current.increment(bonusLoot.getCount());
                return;
            }
        }
    }

    private static ItemStack createBonusLoot(ServerPlayerEntity player, int level, Random random) {
        var registryLookup = player.getServerWorld().getRegistryManager();

        if (level >= 52 && random.nextFloat() < 0.08f) {
            return switch (random.nextInt(4)) {
                case 0 -> new ItemStack(ModItem.LIFE_PEARL);
                case 1 -> new ItemStack(ModItem.MEGA_ROCKET);
                case 2 -> EnchantmentHelper.enchant(random, new ItemStack(Items.DIAMOND_CHESTPLATE), 22, registryLookup, Optional.empty());
                default -> EnchantmentHelper.enchant(random, new ItemStack(Items.DIAMOND_PICKAXE), 20, registryLookup, Optional.empty());
            };
        }

        if (level >= 36 && random.nextFloat() < 0.18f) {
            return switch (random.nextInt(6)) {
                case 0 -> new ItemStack(Items.DIAMOND, 1 + random.nextInt(2));
                case 1 -> new ItemStack(Items.GOLDEN_APPLE);
                case 2 -> new ItemStack(Items.ENDER_PEARL, 1 + random.nextInt(2));
                case 3 -> EnchantmentHelper.enchant(random, new ItemStack(Items.IRON_SWORD), 16, registryLookup, Optional.empty());
                case 4 -> EnchantmentHelper.enchant(random, new ItemStack(Items.IRON_CHESTPLATE), 14, registryLookup, Optional.empty());
                default -> new ItemStack(Items.EMERALD, 5 + random.nextInt(5));
            };
        }

        return switch (random.nextInt(6)) {
            case 0 -> new ItemStack(Items.EMERALD, 3 + random.nextInt(4));
            case 1 -> new ItemStack(Items.IRON_INGOT, 2 + random.nextInt(3));
            case 2 -> new ItemStack(Items.GOLD_INGOT, 2 + random.nextInt(3));
            case 3 -> new ItemStack(Items.ARROW, 8 + random.nextInt(9));
            case 4 -> new ItemStack(Items.LAPIS_LAZULI, 3 + random.nextInt(4));
            default -> EnchantmentHelper.enchant(random, new ItemStack(Items.IRON_SWORD), 8 + level / 4, registryLookup, Optional.empty());
        };
    }

    private static int getTradeDiscount(int level, Merchant merchant) {
        if (level < 18) {
            return 0;
        }

        int discount = Math.min(6, 1 + (level - 18) / 12);
        if (merchant instanceof WanderingTraderEntity && level >= 42) {
            discount += 1;
        }
        return Math.min(7, discount);
    }

    private static ItemStack createGuideBook() {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        List<RawFilteredPair<Text>> pages = List.of(
                RawFilteredPair.of(Text.literal(
                        "Leveling Guide\n\n"
                                + "Play time, vanilla XP, combat, trading, and loot all feed your leveling XP.\n"
                                + "Fill the level requirement to raise your level."
                )),
                RawFilteredPair.of(Text.literal(
                        "Each custom level gives extra vanilla XP levels.\n\n"
                                + "High levels slowly improve chest rewards and trade prices.\n"
                                + "Only late levels unlock the strongest bonuses.\n"
                                + "Nearby hostile mobs can also scale up, especially on harder difficulties."
                )),
                RawFilteredPair.of(Text.literal(
                        "Admins can manage the system with /himproveme leveling ...\n\n"
                                + "You can claim this book again once per Minecraft day with /levelbook."
                ))
        );
        stack.set(
                DataComponentTypes.WRITTEN_BOOK_CONTENT,
                new WrittenBookContentComponent(
                        RawFilteredPair.of("Leveling Guide"),
                        "Him's Improvements",
                        0,
                        pages,
                        true
                )
        );
        return stack;
    }

    private static long getCurrentWorldDay(ServerPlayerEntity player) {
        return player.getServerWorld().getTime() / 24000L;
    }

    private static long getTicksUntilNextDay(ServerPlayerEntity player) {
        long time = player.getServerWorld().getTime();
        long nextDay = (time / 24000L + 1L) * 24000L;
        return Math.max(0L, nextDay - time);
    }

    private static String formatDuration(long ticks) {
        long totalSeconds = Math.max(0L, ticks / 20L);
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        if (minutes > 0L) {
            return seconds > 0L ? minutes + "m " + seconds + "s" : minutes + "m";
        }
        return seconds + "s";
    }
}
