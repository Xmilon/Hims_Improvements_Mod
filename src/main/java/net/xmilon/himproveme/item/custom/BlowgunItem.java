package net.xmilon.himproveme.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.xmilon.himproveme.combat.BlowgunShotHelper;
import net.xmilon.himproveme.item.ModItem;

import java.util.List;
import java.util.function.Predicate;

public class BlowgunItem extends RangedWeaponItem {
    public static final int FIRE_INTERVAL_TICKS = 20;
    public static final int AIM_RAISE_TICKS = 5;
    public static final int RECOIL_TICKS = 4;
    public static final float SHOT_SPEED = 2.2F;
    public static final float SHOT_DIVERGENCE = 0.08F;
    public static final double BASE_DAMAGE = 2.1D;
    public static final double STRAIGHT_DISTANCE = 11.0D;
    public static final double MAX_DISTANCE = 23.0D;
    private static final double FIREBALL_SPEED = 0.9D;
    private static final double FIREBALL_ACCELERATION = 0.02D;
    private static final float ROCKET_SPEED = 1.45F;

    public BlowgunItem(Settings settings) {
        super(settings);
    }

    public static boolean isBlowgun(ItemStack stack) {
        return stack.getItem() instanceof BlowgunItem;
    }

    public static boolean isAiming(LivingEntity entity, Hand hand) {
        return entity != null
                && entity.isUsingItem()
                && entity.getActiveHand() == hand
                && isBlowgun(entity.getActiveItem());
    }

    public static float getAimProgress(LivingEntity entity, Hand hand, float tickDelta) {
        if (!isAiming(entity, hand)) {
            return 0.0F;
        }

        float useTicks = entity.getItemUseTime() + tickDelta;
        return MathHelper.clamp(useTicks / (float) AIM_RAISE_TICKS, 0.0F, 1.0F);
    }

    public static boolean fire(ServerPlayerEntity player, Hand hand) {
        ItemStack blowgunStack = player.getStackInHand(hand);
        if (!isBlowgun(blowgunStack)
                || !isAiming(player, hand)
                || player.getItemCooldownManager().isCoolingDown(blowgunStack.getItem())) {
            return false;
        }

        boolean creative = player.getAbilities().creativeMode;
        ItemStack ammoStack = BlowgunShotHelper.getAmmo(player, blowgunStack);
        if (ammoStack.isEmpty()) {
            return false;
        }

        ItemStack projectileStack = ammoStack.copyWithCount(1);
        ServerWorld world = player.getServerWorld();
        if (BlowgunShotHelper.hasExplosiveBlow(player, blowgunStack) && BlowgunShotHelper.isFireChargeAmmo(projectileStack)) {
            SmallFireballEntity fireball = createFireball(world, player, projectileStack);
            world.spawnEntity(fireball);
        } else if (BlowgunShotHelper.hasExplosiveBlow(player, blowgunStack) && BlowgunShotHelper.isFireworkRocketAmmo(projectileStack)) {
            FireworkRocketEntity rocket = createRocket(world, player, projectileStack);
            world.spawnEntity(rocket);
        } else {
            PersistentProjectileEntity projectile = createDartProjectile(player, blowgunStack, projectileStack);
            world.spawnEntity(projectile);
        }

        playFireSounds(world, player, projectileStack);

        if (!creative && !ammoStack.isEmpty()) {
            ammoStack.decrement(1);
        }

        player.getItemCooldownManager().set(blowgunStack.getItem(), BlowgunShotHelper.getShotCooldownTicks(player, blowgunStack, projectileStack));
        player.swingHand(hand, true);
        player.incrementStat(Stats.USED.getOrCreateStat(ModItem.BLOWGUN));
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(stack.getItem())) {
            return TypedActionResult.fail(stack);
        }

        if (BlowgunShotHelper.getAmmo(user, stack).isEmpty()) {
            return TypedActionResult.fail(stack);
        }

        user.setSprinting(false);
        user.setCurrentHand(hand);
        world.playSound(
                null,
                user.getX(),
                user.getEyeY(),
                user.getZ(),
                SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_1,
                SoundCategory.PLAYERS,
                0.18F,
                1.5F + world.random.nextFloat() * 0.08F
        );
        return TypedActionResult.consume(stack);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        user.setSprinting(false);
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72_000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.NONE;
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return false;
    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
        return BOW_PROJECTILES;
    }

    @Override
    public int getRange() {
        return 23;
    }

    @Override
    protected void shoot(LivingEntity shooter, net.minecraft.entity.projectile.ProjectileEntity projectile, int index, float speed, float divergence, float yaw, LivingEntity target) {
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.translatable("item.himproveme.blowgun.desc"));
    }

    private static PersistentProjectileEntity createDartProjectile(ServerPlayerEntity player, ItemStack blowgunStack, ItemStack projectileStack) {
        ArrowItem arrowItem = projectileStack.getItem() instanceof ArrowItem typedArrow ? typedArrow : (ArrowItem) Items.ARROW;
        PersistentProjectileEntity projectile = arrowItem.createArrow(player.getWorld(), projectileStack, player, blowgunStack);
        projectile.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, SHOT_SPEED, SHOT_DIVERGENCE);
        projectile.setDamage(BASE_DAMAGE);
        projectile.setCritical(false);

        if (projectile instanceof BlowgunProjectileAccess blowgunProjectile) {
            blowgunProjectile.himproveme$markBlowgun(
                    player.getX(),
                    player.getEyeY(),
                    player.getZ(),
                    STRAIGHT_DISTANCE,
                    MAX_DISTANCE
            );
        }

        return projectile;
    }

    private static SmallFireballEntity createFireball(ServerWorld world, ServerPlayerEntity player, ItemStack projectileStack) {
        Vec3d forward = player.getRotationVec(1.0F).normalize();
        Vec3d spawnPos = player.getEyePos().add(forward.multiply(0.45D));
        SmallFireballEntity fireball = new SmallFireballEntity(world, player, forward);
        fireball.setPosition(spawnPos.x, spawnPos.y - 0.12D, spawnPos.z);
        fireball.setVelocity(forward.multiply(FIREBALL_SPEED));
        fireball.accelerationPower = FIREBALL_ACCELERATION;
        fireball.setItem(projectileStack);
        return fireball;
    }

    private static FireworkRocketEntity createRocket(ServerWorld world, ServerPlayerEntity player, ItemStack projectileStack) {
        Vec3d forward = player.getRotationVec(1.0F).normalize();
        Vec3d spawnPos = player.getEyePos().add(forward.multiply(0.35D));
        FireworkRocketEntity rocket = new FireworkRocketEntity(world, projectileStack, player, spawnPos.x, spawnPos.y - 0.12D, spawnPos.z, true);
        rocket.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, ROCKET_SPEED, 0.0F);
        return rocket;
    }

    private static void playFireSounds(ServerWorld world, ServerPlayerEntity player, ItemStack projectileStack) {
        if (BlowgunShotHelper.isFireChargeAmmo(projectileStack)) {
            world.playSound(
                    null,
                    player.getX(),
                    player.getEyeY(),
                    player.getZ(),
                    SoundEvents.ITEM_FIRECHARGE_USE,
                    SoundCategory.PLAYERS,
                    0.55F,
                    1.05F + world.random.nextFloat() * 0.08F
            );
            world.playSound(
                    null,
                    player.getX(),
                    player.getEyeY(),
                    player.getZ(),
                    SoundEvents.ENTITY_BLAZE_SHOOT,
                    SoundCategory.PLAYERS,
                    0.28F,
                    1.15F + world.random.nextFloat() * 0.08F
            );
            return;
        }

        if (BlowgunShotHelper.isFireworkRocketAmmo(projectileStack)) {
            world.playSound(
                    null,
                    player.getX(),
                    player.getEyeY(),
                    player.getZ(),
                    SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH,
                    SoundCategory.PLAYERS,
                    0.6F,
                    1.15F + world.random.nextFloat() * 0.06F
            );
            return;
        }

        world.playSound(
                null,
                player.getX(),
                player.getEyeY(),
                player.getZ(),
                SoundEvents.ENTITY_LLAMA_SPIT,
                SoundCategory.PLAYERS,
                0.28F,
                1.25F + world.random.nextFloat() * 0.08F
        );
        world.playSound(
                null,
                player.getX(),
                player.getEyeY(),
                player.getZ(),
                SoundEvents.ENTITY_ARROW_SHOOT,
                SoundCategory.PLAYERS,
                0.22F,
                1.6F + world.random.nextFloat() * 0.1F
        );
    }
}
