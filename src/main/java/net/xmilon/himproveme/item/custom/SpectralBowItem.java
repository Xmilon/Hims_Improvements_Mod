package net.xmilon.himproveme.item.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.xmilon.himproveme.item.custom.ability.SpecialAbilityItem;

import java.util.List;

public class SpectralBowItem extends BowItem implements SpecialAbilityItem {
    private static final double SPECTRAL_MAX_DISTANCE = 70.0;

    public SpectralBowItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) {
            return;
        }

        ItemStack ammo = player.getProjectileType(stack);
        if (ammo.isEmpty()) {
            ammo = new ItemStack(Items.PRISMARINE_SHARD);
        }

        int useTicks = this.getMaxUseTime(stack, user) - remainingUseTicks;
        float pullProgress = BowItem.getPullProgress(useTicks);
        if (pullProgress < 0.1F) {
            return;
        }

        List<ItemStack> projectiles = load(stack, ammo, player);
        if (world instanceof ServerWorld serverWorld && !projectiles.isEmpty()) {
            this.shootAll(serverWorld, player, player.getActiveHand(), stack, projectiles, pullProgress * 3.0F, 1.0F, pullProgress == 1.0F, null);
        }

        world.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ENTITY_ARROW_SHOOT,
                SoundCategory.PLAYERS,
                1.0F,
                1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + pullProgress * 0.5F
        );
        player.incrementStat(Stats.USED.getOrCreateStat(this));
    }

    @Override
    protected void shoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, LivingEntity target) {
        super.shoot(shooter, projectile, index, speed, divergence, yaw, target);
        if (projectile instanceof PersistentProjectileEntity persistentProjectile
                && persistentProjectile instanceof SpectralProjectileAccess spectralProjectile) {
            spectralProjectile.himproveme$markSpectral(shooter.getX(), shooter.getEyeY(), shooter.getZ(), SPECTRAL_MAX_DISTANCE);
            persistentProjectile.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
        }
    }

    @Override
    public void onSpecialAbilityTick(ServerPlayerEntity player, ItemStack stack) {
        if (player.age % 5 != 0) {
            return;
        }

        for (LivingEntity nearbyEntity : player.getWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(25.0),
                entity -> entity.isAlive() && entity != player && (entity instanceof MobEntity || entity instanceof PlayerEntity)
        )) {
            nearbyEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 12, 0, true, false, true));
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.translatable("item.himproveme.spectral_bow.desc"));
    }
}
