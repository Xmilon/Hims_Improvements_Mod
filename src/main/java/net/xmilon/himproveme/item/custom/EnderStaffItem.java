package net.xmilon.himproveme.item.custom;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.xmilon.himproveme.perk.PerkAccess;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnderStaffItem extends Item {
    private static final int MAX_RANGE = 100;
    private static final int COOLDOWN_TICKS = 160;
    private static final Map<UUID, Long> LAST_USE = new HashMap<>();

    public EnderStaffItem(Settings settings) {
        super(settings);
    }

    public static boolean tryTeleport(ServerPlayerEntity player, ItemStack stack) {
        if (!PerkAccess.hasInfiniteEnderPearl(player)) {
            player.sendMessage(Text.translatable("ability.himproveme.ender_staff.no_perk").formatted(Formatting.RED), true);
            return false;
        }
        long now = player.getWorld().getTime();
        UUID uuid = player.getUuid();
        long last = LAST_USE.getOrDefault(uuid, now - COOLDOWN_TICKS);
        if (now - last < COOLDOWN_TICKS) {
            long remaining = (COOLDOWN_TICKS - (now - last) + 19) / 20;
            player.sendMessage(Text.translatable("ability.himproveme.ender_staff.cooldown", remaining).formatted(Formatting.YELLOW), true);
            return false;
        }

        ServerWorld serverWorld = (ServerWorld) player.getWorld();
        Vec3d destination = resolveDestination(player, serverWorld);
        if (destination == null) {
            player.sendMessage(Text.translatable("ability.himproveme.ender_staff.no_target").formatted(Formatting.RED), true);
            return false;
        }

        spawnParticles(serverWorld, player.getCameraPosVec(1.0F));
        spawnParticles(serverWorld, destination);
        player.teleport(destination.x, destination.y, destination.z, true);
        player.setYaw(player.getYaw());
        player.setPitch(player.getPitch());
        player.getItemCooldownManager().set(stack.getItem(), COOLDOWN_TICKS);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        player.setVelocity(0.0D, 0.0D, 0.0D);
        LAST_USE.put(uuid, now);
        return true;
    }

    private static Vec3d resolveDestination(ServerPlayerEntity player, ServerWorld world) {
        Vec3d cameraPos = player.getCameraPosVec(1.0F);
        Vec3d lookVec = player.getRotationVec(1.0F);
        Vec3d maxTarget = cameraPos.add(lookVec.multiply(MAX_RANGE));
        RaycastContext context = new RaycastContext(cameraPos, maxTarget, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player);
        HitResult hitResult = world.raycast(context);
        Vec3d target;
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            target = Vec3d.ofCenter(blockHit.getBlockPos().up());
        } else {
            target = maxTarget;
        }

        return findSafePosition(world, target);
    }

    private static Vec3d findSafePosition(ServerWorld world, Vec3d base) {
        BlockPos floor = BlockPos.ofFloored(base);
        if (!world.getBlockState(floor).isAir()) {
            return buildStandingPosition(world, floor);
        }

        for (int i = 0; i < 5; i++) {
            BlockPos candidate = floor.down(i);
            if (!world.getBlockState(candidate).isAir()) {
                return buildStandingPosition(world, candidate);
            }
        }
        return null;
    }

    private static Vec3d buildStandingPosition(ServerWorld world, BlockPos floor) {
        BlockPos head = floor.up();
        BlockPos head2 = head.up();
        if (!world.getBlockState(head).isAir() || !world.getBlockState(head2).isAir()) {
            return null;
        }
        return Vec3d.ofCenter(head);
    }

    private static void spawnParticles(ServerWorld world, Vec3d pos) {
        world.spawnParticles(ParticleTypes.PORTAL, pos.x, pos.y, pos.z, 30, 0.2, 0.5, 0.2, 0.05);
    }
}
