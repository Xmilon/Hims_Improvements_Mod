package net.xmilon.himproveme.perk;

import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.xmilon.himproveme.access.DomesticatedRavagerAccess;
import net.xmilon.himproveme.access.RaiderPerkAggroAccess;

import java.util.List;

public final class PillagerPerkHelper {
    public static final int PILLAGER_ANGER_TICKS = 1200;
    private static final double RAIDER_ALERT_RADIUS = 16.0;
    private static final double CONTAINER_TRESPASS_RADIUS = 16.0;

    private PillagerPerkHelper() {
    }

    public static boolean shouldRaidersIgnore(RaiderEntity raider, LivingEntity target) {
        if (!(target instanceof PlayerEntity player)) {
            return false;
        }

        if (player.isCreative() || player.isSpectator()) {
            return true;
        }

        if (!PerkAccess.hasFriendlyPillagers(player)) {
            return false;
        }

        if (raider instanceof RavagerEntity ravager
                && ((DomesticatedRavagerAccess) ravager).himproveme$isOwnedBy(player)) {
            return false;
        }

        return !(raider instanceof RaiderPerkAggroAccess access) || !access.himproveme$isAngryAt(player);
    }

    public static void maybeMarkContainerTrespass(ServerPlayerEntity player, World world, BlockPos pos) {
        if (!PerkAccess.hasFriendlyPillagers(player) || !isChestLikeContainer(world, pos)) {
            return;
        }

        alertNearbyRaiders(world, Vec3d.ofCenter(pos), player, null, CONTAINER_TRESPASS_RADIUS);
    }

    public static void dropLuckyTotems(IllagerEntity illager, DamageSource source) {
        if (!(illager.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        Entity attacker = source.getAttacker();
        if (!(attacker instanceof PlayerEntity player) || !PerkAccess.hasLuckyTotems(player)) {
            return;
        }

        if (illager instanceof PillagerEntity || illager instanceof VindicatorEntity) {
            if (illager.getRandom().nextFloat() < getMinorIllagerTotemChance(illager, getLootingLevel(serverWorld, player))) {
                illager.dropStack(new ItemStack(Items.TOTEM_OF_UNDYING));
            }
            return;
        }

        int totalTotems = 1;
        int lootingLevel = getLootingLevel(serverWorld, player);
        if (illager.getRandom().nextFloat() < Math.min(0.35f + lootingLevel * 0.12f, 0.9f)) {
            totalTotems++;
        }
        if (illager.getRandom().nextFloat() < Math.min(0.12f + lootingLevel * 0.07f, 0.65f)) {
            totalTotems++;
        }

        if (illager.getType() == net.minecraft.entity.EntityType.EVOKER) {
            totalTotems--;
        }

        if (totalTotems > 0) {
            illager.dropStack(new ItemStack(Items.TOTEM_OF_UNDYING, totalTotems));
        }
    }

    public static void alertNearbyRaiders(World world, Vec3d center, PlayerEntity player, RaiderEntity directWitness) {
        alertNearbyRaiders(world, center, player, directWitness, RAIDER_ALERT_RADIUS);
    }

    public static void alertNearbyRaiders(World world, Vec3d center, PlayerEntity player, RaiderEntity directWitness, double radius) {
        if (!PerkAccess.hasFriendlyPillagers(player) || player.isCreative() || player.isSpectator()) {
            return;
        }

        List<RaiderEntity> nearbyRaiders = world.getEntitiesByClass(
                RaiderEntity.class,
                new Box(center, center).expand(radius),
                raider -> raider.isAlive() && (raider == directWitness || raider.canSee(player))
        );
        for (RaiderEntity raider : nearbyRaiders) {
            angerRaider(raider, player);
        }

        if (directWitness != null && directWitness.isAlive()) {
            angerRaider(directWitness, player);
        }
    }

    private static boolean isChestLikeContainer(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return block instanceof ChestBlock
                || block instanceof BarrelBlock
                || block instanceof EnderChestBlock
                || block instanceof ShulkerBoxBlock;
    }

    private static void angerRaider(RaiderEntity raider, PlayerEntity player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        if (raider instanceof RavagerEntity ravager
                && ((DomesticatedRavagerAccess) ravager).himproveme$isOwnedBy(player)) {
            return;
        }

        if (raider instanceof RaiderPerkAggroAccess access) {
            access.himproveme$angerAt(player, PILLAGER_ANGER_TICKS);
        }
        raider.setTarget(player);
    }

    private static float getMinorIllagerTotemChance(IllagerEntity illager, int lootingLevel) {
        if (illager instanceof PillagerEntity) {
            return Math.min(0.04f + lootingLevel * 0.03f, 0.2f);
        }
        return Math.min(0.08f + lootingLevel * 0.04f, 0.25f);
    }

    private static int getLootingLevel(ServerWorld world, PlayerEntity player) {
        RegistryEntry<net.minecraft.enchantment.Enchantment> looting = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .getEntry(Enchantments.LOOTING)
                .orElse(null);
        if (looting == null) {
            return 0;
        }
        return EnchantmentHelper.getEquipmentLevel(looting, player);
    }
}
