package net.xmilon.himproveme.perk;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.xmilon.himproveme.access.MerchantPerkAccess;
import net.xmilon.himproveme.access.VillagerJobApplicationAccess;
import net.xmilon.himproveme.item.ModItem;
import net.xmilon.himproveme.network.perk.VillagerTradeStatusPayload;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class VillagerPerkHelper {
    public static final long TRADE_COOLDOWN_TICKS = 216000L;
    public static final int JOB_APPLICATION_PANIC_TICKS = 60;

    private static final int TRADE_MULTIPLIER = 5;
    private static final float JOB_APPLICATION_FAILURE_CHANCE = 0.01F;
    private static final long EXTRA_DIMENSION_SPAWN_INTERVAL_TICKS = 24000L;
    private static final long EXTRA_DIMENSION_SPAWN_OFFSET_TICKS = 6000L;
    private static final int EXTRA_DIMENSION_TRADER_RADIUS = 128;
    private static final int EXTRA_DIMENSION_TRADER_DESPAWN_DELAY = 48000;
    private static final int MAX_SUPER_RARE_TRADES = 2;
    private static final String JOB_APPLICATION_NAME = "jobapplication";

    private VillagerPerkHelper() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(VillagerPerkHelper::tickDimensionTraderSpawns);
    }

    public static ActionResult tryUseJobApplication(PlayerEntity player, VillagerEntity villager, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!canUseJobApplication(player, villager, stack)) {
            return ActionResult.PASS;
        }

        if (player.getWorld().isClient) {
            return ActionResult.SUCCESS;
        }

        if (!player.isCreative()) {
            stack.decrement(1);
        }

        if (villager.getRandom().nextFloat() < JOB_APPLICATION_FAILURE_CHANCE) {
            triggerRejectedApplication(villager, player);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.translatable("ability.himproveme.villager.job_application_failure"), true);
            }
            return ActionResult.SUCCESS;
        }

        villager.setVillagerData(villager.getVillagerData()
                .withProfession(VillagerProfession.NONE)
                .withLevel(1));
        villager.setExperience(0);
        villager.setOffers(new TradeOfferList());

        if (villager instanceof MerchantPerkAccess access) {
            access.himproveme$setBoostedOfferCount(0);
            access.himproveme$resizeTradeCooldowns(0);
        }

        if (villager.getWorld() instanceof ServerWorld serverWorld) {
            villager.reinitializeBrain(serverWorld);
            serverWorld.sendEntityStatus(villager, (byte) 14);
        }

        villager.playSound(SoundEvents.ENTITY_VILLAGER_CELEBRATE, 1.0F, 1.0F);
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.translatable("ability.himproveme.villager.job_application_success"), true);
        }
        return ActionResult.SUCCESS;
    }

    public static void prepareVillagerForTrading(VillagerEntity villager, ServerPlayerEntity player) {
        if (!PerkAccess.hasMarketConnections(player)) {
            return;
        }

        ensureVillagerOffersBoosted(villager);
        applyVillagerCooldowns(villager);
    }

    public static void onVillagerTradeUsed(VillagerEntity villager, TradeOffer offer) {
        if (!(villager.getCustomer() instanceof ServerPlayerEntity player) || !PerkAccess.hasMarketConnections(player)) {
            return;
        }

        if (villager instanceof MerchantPerkAccess access) {
            int offerIndex = villager.getOffers().indexOf(offer);
            if (offerIndex >= 0 && offer.isDisabled()) {
                access.himproveme$setTradeCooldownEndTick(offerIndex, villager.getWorld().getTime() + TRADE_COOLDOWN_TICKS);
            }
        }

        applyVillagerCooldowns(villager);
        refreshOpenVillagerTrades(villager, player);
    }

    public static void onVillagerRestock(VillagerEntity villager) {
        if (applyVillagerCooldowns(villager)
                && villager.getCustomer() instanceof ServerPlayerEntity player
                && PerkAccess.hasMarketConnections(player)) {
            refreshOpenVillagerTrades(villager, player);
        }
    }

    public static void tickVillager(VillagerEntity villager) {
        if (!(villager.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        tickRejectedApplication(villager, serverWorld);

        if (applyVillagerCooldowns(villager)
                && villager.getCustomer() instanceof ServerPlayerEntity player
                && PerkAccess.hasMarketConnections(player)) {
            refreshOpenVillagerTrades(villager, player);
        }
    }

    public static void prepareWanderingTraderOffers(WanderingTraderEntity trader) {
        if (!(trader.getWorld() instanceof ServerWorld serverWorld) || !(trader instanceof MerchantPerkAccess access)) {
            return;
        }
        if (access.himproveme$hasEnhancedTraderOffers()) {
            return;
        }

        TradeOfferList updatedOffers = new TradeOfferList();
        for (TradeOffer offer : trader.getOffers()) {
            updatedOffers.add(createDiscountedTraderOffer(offer));
        }

        Random random = trader.getRandom();
        List<OfferFactory> rarePool = new ArrayList<>(List.of(
                weightedOffer(12, world -> sellForEmeralds(28, Optional.empty(), enchantItem(world, random, Items.DIAMOND_SWORD, 28), 4, 10)),
                weightedOffer(12, world -> sellForEmeralds(28, Optional.empty(), enchantItem(world, random, Items.DIAMOND_PICKAXE, 28), 4, 10)),
                weightedOffer(11, world -> sellForEmeralds(30, Optional.empty(), enchantItem(world, random, Items.DIAMOND_CHESTPLATE, 26), 3, 12)),
                weightedOffer(10, world -> sellForEmeralds(22, Optional.empty(), new ItemStack(ModItem.BREEZE_STAFF), 2, 10)),
                weightedOffer(10, world -> sellForEmeralds(18, Optional.empty(), new ItemStack(ModItem.MEGA_ROCKET), 4, 8)),
                weightedOffer(9, world -> sellForEmeralds(24, Optional.empty(), new ItemStack(ModItem.LIFE_PEARL), 2, 10)),
                weightedOffer(8, world -> sellForEmeralds(20, Optional.empty(), new ItemStack(ModItem.ENDER_BUNDLE), 2, 10)),
                weightedOffer(8, world -> sellForEmeralds(26, Optional.empty(), enchantItem(world, random, ModItem.ENDER_SWORD, 24), 2, 12)),
                weightedOffer(7, world -> sellForEmeralds(30, Optional.empty(), enchantItem(world, random, ModItem.ENDER_PICKAXE, 24), 2, 12))
        ));

        List<OfferFactory> superRarePool = new ArrayList<>(List.of(
                weightedOffer(4, world -> sellForEmeralds(48, Optional.of(new TradedItem(Items.DIAMOND, 6)), new ItemStack(Items.BEACON), 1, 18)),
                weightedOffer(3, world -> sellForEmeralds(42, Optional.of(new TradedItem(Items.DIAMOND, 4)), new ItemStack(ModItem.ENDER_STAFF), 1, 16)),
                weightedOffer(2, world -> sellForEmeralds(40, Optional.of(new TradedItem(Items.DIAMOND, 4)), new ItemStack(ModItem.NETHER_CORE), 1, 16)),
                weightedOffer(1, world -> sellForEmeralds(56, Optional.of(new TradedItem(Items.DIAMOND_BLOCK, 1)), new ItemStack(ModItem.GODLY_ELYTRA), 1, 20))
        ));

        for (int i = 0; i < 2 && !rarePool.isEmpty(); i++) {
            OfferFactory factory = takeWeighted(rarePool, random);
            if (factory != null) {
                updatedOffers.add(factory.create(serverWorld));
            }
        }

        int superRareTrades = Math.min(MAX_SUPER_RARE_TRADES, random.nextFloat() < 0.45F ? 2 : 1);
        for (int i = 0; i < superRareTrades && !superRarePool.isEmpty(); i++) {
            OfferFactory factory = takeWeighted(superRarePool, random);
            if (factory != null) {
                updatedOffers.add(factory.create(serverWorld));
            }
        }

        TradeOfferList traderOffers = trader.getOffers();
        traderOffers.clear();
        traderOffers.addAll(updatedOffers);
        access.himproveme$setEnhancedTraderOffers(true);
    }

    public static void syncVillagerTradeStatus(VillagerEntity villager, ServerPlayerEntity player) {
        if (!(player.currentScreenHandler instanceof MerchantScreenHandler handler)) {
            return;
        }

        NbtCompound data = new NbtCompound();
        data.putInt("SyncId", handler.syncId);
        data.putLong("TotalCooldownTicks", TRADE_COOLDOWN_TICKS);

        MerchantPerkAccess access = villager instanceof MerchantPerkAccess merchantAccess ? merchantAccess : null;
        long now = villager.getWorld().getTime();
        NbtList offers = new NbtList();
        for (int i = 0; i < villager.getOffers().size(); i++) {
            TradeOffer offer = villager.getOffers().get(i);
            NbtCompound offerData = new NbtCompound();
            offerData.putInt("RemainingTrades", Math.max(0, offer.getMaxUses() - offer.getUses()));
            long cooldownEndTick = access == null ? 0L : access.himproveme$getTradeCooldownEndTick(i);
            offerData.putLong("CooldownLeftTicks", Math.max(0L, cooldownEndTick - now));
            offers.add(offerData);
        }
        data.put("Offers", offers);
        ServerPlayNetworking.send(player, new VillagerTradeStatusPayload(data));
    }

    private static boolean canUseJobApplication(PlayerEntity player, VillagerEntity villager, ItemStack stack) {
        return PerkAccess.hasJobApplication(player)
                && stack.isOf(Items.PAPER)
                && isJobApplication(stack)
                && villager.getVillagerData().getProfession() == VillagerProfession.NITWIT
                && villager.isAlive()
                && !villager.isBaby();
    }

    private static boolean isJobApplication(ItemStack stack) {
        if (stack.get(DataComponentTypes.CUSTOM_NAME) == null) {
            return false;
        }

        String normalizedName = stack.getName().getString()
                .toLowerCase()
                .replaceAll("[^a-z]", "");
        return JOB_APPLICATION_NAME.equals(normalizedName);
    }

    private static void triggerRejectedApplication(VillagerEntity villager, PlayerEntity player) {
        if (!(villager instanceof VillagerJobApplicationAccess access) || !(villager.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        access.himproveme$startJobApplicationPanic(player.getPos());
        serverWorld.sendEntityStatus(villager, (byte) 13);
        villager.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0F, 0.8F);
    }

    private static void tickRejectedApplication(VillagerEntity villager, ServerWorld world) {
        if (!(villager instanceof VillagerJobApplicationAccess access)) {
            return;
        }

        int panicTicks = access.himproveme$getJobApplicationPanicTicks();
        if (panicTicks <= 0) {
            return;
        }

        Vec3d escapeDirection = villager.getPos().subtract(access.himproveme$getJobApplicationPanicSource());
        if (escapeDirection.lengthSquared() < 1.0E-4) {
            escapeDirection = villager.getRotationVector();
        }
        escapeDirection = escapeDirection.normalize();

        villager.getNavigation().startMovingTo(
                villager.getX() + escapeDirection.x * 8.0D,
                villager.getY(),
                villager.getZ() + escapeDirection.z * 8.0D,
                1.25D
        );
        villager.setVelocity(new Vec3d(
                villager.getVelocity().x + escapeDirection.x * 0.045D,
                villager.getVelocity().y,
                villager.getVelocity().z + escapeDirection.z * 0.045D
        ));
        if (panicTicks % 10 == 0) {
            world.sendEntityStatus(villager, (byte) 13);
        }

        panicTicks--;
        access.himproveme$setJobApplicationPanicTicks(panicTicks);
        if (panicTicks <= 0 && villager.isAlive()) {
            villager.damage(villager.getDamageSources().genericKill(), Float.MAX_VALUE);
        }
    }

    private static boolean ensureVillagerOffersBoosted(VillagerEntity villager) {
        if (!(villager instanceof MerchantPerkAccess access)) {
            return false;
        }

        TradeOfferList offers = villager.getOffers();
        access.himproveme$resizeTradeCooldowns(offers.size());

        int boostedOfferCount = MathHelper.clamp(access.himproveme$getBoostedOfferCount(), 0, offers.size());
        if (boostedOfferCount == offers.size()) {
            return false;
        }

        TradeOfferList updatedOffers = new TradeOfferList();
        for (int i = 0; i < offers.size(); i++) {
            updatedOffers.add(i < boostedOfferCount ? offers.get(i) : createBoostedVillagerOffer(offers.get(i)));
        }

        villager.setOffers(updatedOffers);
        access.himproveme$setBoostedOfferCount(updatedOffers.size());
        return true;
    }

    private static boolean applyVillagerCooldowns(VillagerEntity villager) {
        if (!(villager instanceof MerchantPerkAccess access)) {
            return false;
        }

        TradeOfferList offers = villager.getOffers();
        access.himproveme$resizeTradeCooldowns(offers.size());

        long now = villager.getWorld().getTime();
        boolean changed = false;
        for (int i = 0; i < offers.size(); i++) {
            TradeOffer offer = offers.get(i);
            long cooldownEndTick = access.himproveme$getTradeCooldownEndTick(i);
            if (cooldownEndTick <= 0L) {
                continue;
            }

            if (now >= cooldownEndTick) {
                access.himproveme$clearTradeCooldownEndTick(i);
                if (offer.isDisabled()) {
                    offer.resetUses();
                    changed = true;
                }
                continue;
            }

            if (!offer.isDisabled()) {
                offer.disable();
                changed = true;
            }
        }

        return changed;
    }

    private static void refreshOpenVillagerTrades(VillagerEntity villager, ServerPlayerEntity player) {
        if (!(player.currentScreenHandler instanceof MerchantScreenHandler handler)) {
            return;
        }

        player.sendTradeOffers(
                handler.syncId,
                villager.getOffers(),
                villager.getVillagerData().getLevel(),
                villager.getExperience(),
                villager.isLeveledMerchant(),
                villager.canRefreshTrades()
        );
        syncVillagerTradeStatus(villager, player);
    }

    private static TradeOffer createBoostedVillagerOffer(TradeOffer offer) {
        TradeOffer boostedOffer = new TradeOffer(
                copyTradedItem(offer.getFirstBuyItem(), offer.getFirstBuyItem().count()),
                offer.getSecondBuyItem().map(secondBuy -> copyTradedItem(secondBuy, secondBuy.count())),
                offer.copySellItem(),
                offer.getUses(),
                Math.max(1, offer.getMaxUses() * TRADE_MULTIPLIER),
                offer.getMerchantExperience(),
                offer.getPriceMultiplier(),
                offer.getSpecialPrice()
        );
        if (offer.isDisabled()) {
            boostedOffer.disable();
        }
        return boostedOffer;
    }

    private static TradeOffer createDiscountedTraderOffer(TradeOffer offer) {
        int discountedFirstCost = Math.max(1, Math.round(offer.getFirstBuyItem().count() * 0.7F));
        TradeOffer discountedOffer = new TradeOffer(
                copyTradedItem(offer.getFirstBuyItem(), discountedFirstCost),
                offer.getSecondBuyItem().map(secondBuy -> copyTradedItem(secondBuy, secondBuy.count())),
                offer.copySellItem(),
                offer.getUses(),
                Math.max(8, offer.getMaxUses()),
                offer.getMerchantExperience(),
                offer.getPriceMultiplier(),
                offer.getSpecialPrice()
        );
        if (offer.isDisabled()) {
            discountedOffer.disable();
        }
        return discountedOffer;
    }

    private static TradeOffer sellForEmeralds(int emeraldCost, Optional<TradedItem> secondBuyItem, ItemStack sellItem, int maxUses, int merchantExperience) {
        return new TradeOffer(
                new TradedItem(Items.EMERALD, emeraldCost),
                secondBuyItem,
                sellItem,
                0,
                maxUses,
                merchantExperience,
                0.05F
        );
    }

    private static TradedItem copyTradedItem(TradedItem tradedItem, int count) {
        return new TradedItem(tradedItem.item(), count, tradedItem.components());
    }

    private static ItemStack enchantItem(ServerWorld world, Random random, Item item, int enchantPower) {
        return EnchantmentHelper.enchant(
                random,
                new ItemStack(item),
                enchantPower,
                world.getRegistryManager(),
                Optional.empty()
        );
    }

    private static @Nullable OfferFactory takeWeighted(List<OfferFactory> pool, Random random) {
        if (pool.isEmpty()) {
            return null;
        }

        int totalWeight = 0;
        for (OfferFactory entry : pool) {
            totalWeight += entry.weight();
        }

        int pickedWeight = random.nextInt(totalWeight);
        int cursor = 0;
        for (int i = 0; i < pool.size(); i++) {
            cursor += pool.get(i).weight();
            if (pickedWeight < cursor) {
                return pool.remove(i);
            }
        }

        return pool.remove(pool.size() - 1);
    }

    private static OfferFactory weightedOffer(int weight, Function<ServerWorld, TradeOffer> factory) {
        return new OfferFactory(weight, factory);
    }

    private static void tickDimensionTraderSpawns(ServerWorld world) {
        if (world.getRegistryKey() == World.OVERWORLD
                || !world.getGameRules().getBoolean(GameRules.DO_TRADER_SPAWNING)
                || !world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)
                || (world.getTime() + EXTRA_DIMENSION_SPAWN_OFFSET_TICKS) % EXTRA_DIMENSION_SPAWN_INTERVAL_TICKS != 0L) {
            return;
        }

        List<ServerPlayerEntity> players = world.getPlayers(player -> player.isAlive() && !player.isSpectator());
        if (players.isEmpty()) {
            return;
        }

        ServerPlayerEntity anchor = players.get(world.getRandom().nextInt(players.size()));
        if (!world.getEntitiesByClass(
                WanderingTraderEntity.class,
                new Box(anchor.getBlockPos()).expand(EXTRA_DIMENSION_TRADER_RADIUS),
                trader -> trader.isAlive()
        ).isEmpty()) {
            return;
        }

        BlockPos spawnPos = findDimensionTraderSpawnPos(world, anchor.getBlockPos(), world.getRandom());
        if (spawnPos == null) {
            return;
        }

        WanderingTraderEntity trader = EntityType.WANDERING_TRADER.spawn(world, spawnPos, SpawnReason.EVENT);
        if (trader != null) {
            trader.setDespawnDelay(EXTRA_DIMENSION_TRADER_DESPAWN_DELAY);
        }
    }

    private static @Nullable BlockPos findDimensionTraderSpawnPos(ServerWorld world, BlockPos around, Random random) {
        for (int attempt = 0; attempt < 24; attempt++) {
            int x = around.getX() + random.nextInt(96) - 48;
            int z = around.getZ() + random.nextInt(96) - 48;

            BlockPos surfacePos = new BlockPos(x, world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            BlockPos spawnPos = findSafeSpawnNear(world, surfacePos);
            if (spawnPos != null) {
                return spawnPos;
            }

            BlockPos playerHeightPos = new BlockPos(x, around.getY(), z);
            spawnPos = findSafeSpawnNear(world, playerHeightPos);
            if (spawnPos != null) {
                return spawnPos;
            }
        }

        return null;
    }

    private static @Nullable BlockPos findSafeSpawnNear(ServerWorld world, BlockPos start) {
        int minY = world.getBottomY() + 1;
        int maxY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, start.getX(), start.getZ());
        for (int yOffset = -6; yOffset <= 6; yOffset++) {
            int y = MathHelper.clamp(start.getY() + yOffset, minY, maxY);
            BlockPos candidate = new BlockPos(start.getX(), y, start.getZ());
            if (canTraderSpawnAt(world, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean canTraderSpawnAt(ServerWorld world, BlockPos pos) {
        BlockPos below = pos.down();
        return world.getBlockState(below).isSolidBlock(world, below)
                && world.isAir(pos)
                && world.isAir(pos.up());
    }

    private record OfferFactory(int weight, Function<ServerWorld, TradeOffer> factory) {
        private TradeOffer create(ServerWorld world) {
            return factory.apply(world);
        }
    }
}
