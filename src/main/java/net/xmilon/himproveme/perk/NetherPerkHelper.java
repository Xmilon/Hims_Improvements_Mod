package net.xmilon.himproveme.perk;

import com.mojang.serialization.DataResult;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.world.World;
import net.xmilon.himproveme.access.PiglinTradeAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Function;

public final class NetherPerkHelper {
    private static final String PIGLIN_OFFERS_KEY = "HimProveMePiglinOffers";
    private static final int PIGLIN_ALERT_RADIUS = 16;
    private static final int PIGLIN_ANGER_TICKS = 600;
    private static final int PIGLIN_MAX_OFFERS = 3;

    private NetherPerkHelper() {
    }

    public static boolean shouldPiglinsIgnore(AbstractPiglinEntity piglin, LivingEntity target) {
        if (!(target instanceof PlayerEntity player)) {
            return false;
        }

        if (player.isCreative() || player.isSpectator()) {
            return true;
        }

        if (!PerkAccess.hasFriendlyPiglins(player)) {
            return false;
        }

        return !isAngryAt(piglin, player);
    }

    public static void alertNearbyPiglins(World world, Vec3d center, PlayerEntity player, @Nullable AbstractPiglinEntity directWitness) {
        if (!PerkAccess.hasFriendlyPiglins(player) || player.isCreative() || player.isSpectator()) {
            return;
        }

        List<AbstractPiglinEntity> nearbyPiglins = world.getEntitiesByClass(
                AbstractPiglinEntity.class,
                new Box(center, center).expand(PIGLIN_ALERT_RADIUS),
                piglin -> piglin.isAlive()
                        && piglin.isAdult()
                        && (piglin == directWitness || piglin.canSee(player))
        );
        for (AbstractPiglinEntity piglin : nearbyPiglins) {
            angerPiglin(piglin, player);
        }

        if (directWitness != null && directWitness.isAlive() && directWitness.isAdult()) {
            angerPiglin(directWitness, player);
        }
    }

    public static void maybeAlertPiglinsForInteraction(ServerPlayerEntity player, World world, BlockPos pos) {
        if (!PerkAccess.hasFriendlyPiglins(player)) {
            return;
        }

        BlockState state = world.getBlockState(pos);
        if (!state.isIn(BlockTags.GUARDED_BY_PIGLINS) && !state.isIn(BlockTags.GOLD_ORES)) {
            return;
        }

        alertNearbyPiglins(world, Vec3d.ofCenter(pos), player, null);
    }

    public static boolean canTradeWith(PlayerEntity player, AbstractPiglinEntity piglin) {
        if (!PerkAccess.hasPiglinBartering(player)) {
            return false;
        }

        if (piglin instanceof PiglinEntity piglinEntity && piglinEntity.isBaby()) {
            return false;
        }

        return !isAngryAt(piglin, player);
    }

    public static boolean tryOpenTrade(ServerPlayerEntity player, AbstractPiglinEntity piglin) {
        if (!(piglin instanceof PiglinTradeAccess access) || !(player.getWorld() instanceof ServerWorld serverWorld)) {
            return false;
        }

        TradeOfferList offers = getOrCreateOffers(serverWorld, piglin, access);
        Merchant merchant = new PiglinMerchant(piglin, access);
        merchant.setCustomer(player);

        NamedScreenHandlerFactory factory = new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, ignoredPlayer) -> new MerchantScreenHandler(syncId, playerInventory, merchant),
                piglin.getDisplayName()
        );
        OptionalInt syncId = player.openHandledScreen(factory);
        if (syncId.isEmpty()) {
            return false;
        }

        player.sendTradeOffers(syncId.getAsInt(), offers, 0, 0, false, false);
        return true;
    }

    public static void writeTradeOffers(AbstractPiglinEntity piglin, NbtCompound nbt, PiglinTradeAccess access) {
        TradeOfferList offers = access.himproveme$getTradeOffers();
        if (offers.isEmpty()) {
            return;
        }

        RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, piglin.getRegistryManager());
        DataResult<NbtElement> encoded = TradeOfferList.CODEC.encodeStart(ops, offers);
        encoded.result().ifPresent(element -> nbt.put(PIGLIN_OFFERS_KEY, element));
    }

    public static void readTradeOffers(AbstractPiglinEntity piglin, NbtCompound nbt, PiglinTradeAccess access) {
        if (!nbt.contains(PIGLIN_OFFERS_KEY)) {
            access.himproveme$setTradeOffers(new TradeOfferList());
            return;
        }

        RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, piglin.getRegistryManager());
        TradeOfferList offers = TradeOfferList.CODEC.parse(ops, nbt.get(PIGLIN_OFFERS_KEY))
                .result()
                .orElseGet(TradeOfferList::new);
        access.himproveme$setTradeOffers(offers);
    }

    private static void angerPiglin(AbstractPiglinEntity piglin, PlayerEntity player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        piglin.getBrain().remember(MemoryModuleType.ANGRY_AT, player.getUuid(), PIGLIN_ANGER_TICKS);
        piglin.getBrain().remember(MemoryModuleType.ATTACK_TARGET, player, PIGLIN_ANGER_TICKS);
        piglin.setTarget(player);
    }

    private static boolean isAngryAt(AbstractPiglinEntity piglin, PlayerEntity player) {
        UUID playerUuid = player.getUuid();
        if (piglin.getBrain().getOptionalMemory(MemoryModuleType.ANGRY_AT).filter(playerUuid::equals).isPresent()) {
            return true;
        }

        return piglin.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET)
                .filter(target -> target == player)
                .isPresent();
    }

    private static TradeOfferList getOrCreateOffers(ServerWorld world, AbstractPiglinEntity piglin, PiglinTradeAccess access) {
        TradeOfferList offers = access.himproveme$getTradeOffers();
        if (!offers.isEmpty()) {
            return offers;
        }

        TradeOfferList generated = piglin instanceof PiglinBruteEntity
                ? generateBruteOffers(world, piglin)
                : generatePiglinOffers(world, piglin);
        access.himproveme$setTradeOffers(generated);
        return generated;
    }

    private static TradeOfferList generatePiglinOffers(ServerWorld world, AbstractPiglinEntity piglin) {
        Random random = piglin.getRandom();
        List<OfferFactory> pool = new ArrayList<>(List.of(
                weightedOffer(12, ignored -> sellForGold(4, new ItemStack(Items.STRING, 8), 12, 1)),
                weightedOffer(11, ignored -> sellForGold(4, new ItemStack(Items.LEATHER, 4), 12, 1)),
                weightedOffer(10, ignored -> sellForGold(5, new ItemStack(Items.QUARTZ, 6), 12, 1)),
                weightedOffer(10, ignored -> sellForGold(5, new ItemStack(Items.BLACKSTONE, 12), 12, 1)),
                weightedOffer(9, ignored -> sellForGold(5, new ItemStack(Items.SOUL_SAND, 8), 12, 1)),
                weightedOffer(8, ignored -> sellForGold(6, new ItemStack(Items.NETHER_BRICK, 8), 12, 1)),
                weightedOffer(7, ignored -> sellForGold(7, new ItemStack(Items.GLOWSTONE_DUST, 6), 10, 1)),
                weightedOffer(6, ignored -> sellForGold(8, new ItemStack(Items.OBSIDIAN, 2), 8, 2)),
                weightedOffer(4, ignored -> sellForGold(10, new ItemStack(Items.CRYING_OBSIDIAN, 1), 6, 2)),
                weightedOffer(5, ignored -> sellForGold(8, new ItemStack(Items.MAGMA_CREAM, 2), 8, 2)),
                weightedOffer(5, ignored -> sellForGold(7, new ItemStack(Items.FIRE_CHARGE, 3), 8, 2)),
                weightedOffer(3, ignored -> sellForGold(12, new ItemStack(Items.ENDER_PEARL, 1), 6, 3)),
                weightedOffer(2, ignored -> sellForGold(20, new ItemStack(Items.GHAST_TEAR, 1), 4, 4))
        ));

        TradeOfferList offers = new TradeOfferList();
        for (int i = 0; i < Math.min(PIGLIN_MAX_OFFERS, pool.size()); i++) {
            OfferFactory chosen = takeWeighted(pool, random);
            if (chosen == null) {
                break;
            }
            offers.add(chosen.create(world));
        }
        return offers;
    }

    private static TradeOfferList generateBruteOffers(ServerWorld world, AbstractPiglinEntity piglin) {
        Random random = piglin.getRandom();
        List<OfferFactory> pool = new ArrayList<>(List.of(
                weightedOffer(40, ignored -> sellForGold(22, new ItemStack(Items.NETHERITE_SCRAP, 1), 12, 4)),
                weightedOffer(14, ignored -> sellForGold(36, new ItemStack(Items.ANCIENT_DEBRIS, 1), 8, 6)),
                weightedOffer(8, ignored -> sellForGold(52, new ItemStack(Items.NETHERITE_SWORD, 1), 4, 8)),
                weightedOffer(8, ignored -> sellForGold(52, new ItemStack(Items.NETHERITE_AXE, 1), 4, 8)),
                weightedOffer(7, ignored -> sellForGold(50, new ItemStack(Items.NETHERITE_PICKAXE, 1), 4, 8)),
                weightedOffer(5, ignored -> sellForGold(48, new ItemStack(Items.NETHERITE_HELMET, 1), 4, 8)),
                weightedOffer(6, ignored -> sellForGold(58, new ItemStack(Items.NETHERITE_BOOTS, 1), 4, 8)),
                weightedOffer(4, ignored -> sellForGold(62, new ItemStack(Items.NETHERITE_CHESTPLATE, 1), 3, 10)),
                weightedOffer(4, ignored -> sellForGold(60, new ItemStack(Items.NETHERITE_LEGGINGS, 1), 3, 10)),
                weightedOffer(2, serverWorld -> sellForGold(84, enchantItem(serverWorld, random, Items.NETHERITE_LEGGINGS, 34), 1, 18)),
                weightedOffer(1, serverWorld -> sellForGold(88, enchantItem(serverWorld, random, Items.NETHERITE_CHESTPLATE, 36), 1, 18)),
                weightedOffer(1, serverWorld -> sellForGold(80, enchantItem(serverWorld, random, Items.NETHERITE_SWORD, 32), 1, 16)),
                weightedOffer(1, serverWorld -> sellForGold(78, enchantItem(serverWorld, random, Items.NETHERITE_AXE, 32), 1, 16))
        ));

        TradeOfferList offers = new TradeOfferList();
        OfferFactory chosen = takeWeighted(pool, random);
        if (chosen != null) {
            offers.add(chosen.create(world));
        }
        return offers;
    }

    private static OfferFactory weightedOffer(int weight, Function<ServerWorld, TradeOffer> factory) {
        return new OfferFactory(weight, factory);
    }

    @Nullable
    private static OfferFactory takeWeighted(List<OfferFactory> pool, Random random) {
        if (pool.isEmpty()) {
            return null;
        }

        int totalWeight = 0;
        for (OfferFactory entry : pool) {
            totalWeight += entry.weight();
        }

        int picked = random.nextInt(totalWeight);
        int cursor = 0;
        for (int i = 0; i < pool.size(); i++) {
            cursor += pool.get(i).weight();
            if (picked < cursor) {
                return pool.remove(i);
            }
        }
        return pool.remove(pool.size() - 1);
    }

    private static TradeOffer sellForGold(int goldCost, ItemStack sellItem, int maxUses, int merchantExperience) {
        TradedItem firstBuy = new TradedItem(Items.GOLD_INGOT, Math.min(64, goldCost));
        Optional<TradedItem> secondBuy = goldCost > 64
                ? Optional.of(new TradedItem(Items.GOLD_INGOT, goldCost - 64))
                : Optional.empty();
        return new TradeOffer(firstBuy, secondBuy, sellItem, 0, maxUses, merchantExperience, 0.05f);
    }

    private static ItemStack enchantItem(ServerWorld world, Random random, Item item, int enchantPower) {
        return net.minecraft.enchantment.EnchantmentHelper.enchant(
                random,
                new ItemStack(item),
                enchantPower,
                world.getRegistryManager(),
                Optional.empty()
        );
    }

    private record OfferFactory(int weight, Function<ServerWorld, TradeOffer> factory) {
        private TradeOffer create(ServerWorld world) {
            return factory.apply(world);
        }
    }

    private static final class PiglinMerchant implements Merchant {
        private final AbstractPiglinEntity piglin;
        private final PiglinTradeAccess access;
        private @Nullable PlayerEntity customer;

        private PiglinMerchant(AbstractPiglinEntity piglin, PiglinTradeAccess access) {
            this.piglin = piglin;
            this.access = access;
        }

        @Override
        public void setCustomer(PlayerEntity player) {
            this.customer = player;
        }

        @Override
        public @Nullable PlayerEntity getCustomer() {
            return this.customer;
        }

        @Override
        public TradeOfferList getOffers() {
            return access.himproveme$getTradeOffers();
        }

        @Override
        public void setOffersFromServer(TradeOfferList offers) {
            access.himproveme$setTradeOffers(offers);
        }

        @Override
        public void trade(TradeOffer offer) {
        }

        @Override
        public void onSellingItem(ItemStack stack) {
            piglin.playSound(getYesSound(), 1.0f, 1.0f);
        }

        @Override
        public int getExperience() {
            return 0;
        }

        @Override
        public void setExperienceFromServer(int experience) {
        }

        @Override
        public boolean isLeveledMerchant() {
            return false;
        }

        @Override
        public SoundEvent getYesSound() {
            return piglin instanceof PiglinBruteEntity
                    ? SoundEvents.ENTITY_PIGLIN_BRUTE_AMBIENT
                    : SoundEvents.ENTITY_PIGLIN_ADMIRING_ITEM;
        }

        @Override
        public boolean isClient() {
            return piglin.getWorld().isClient;
        }
    }
}
