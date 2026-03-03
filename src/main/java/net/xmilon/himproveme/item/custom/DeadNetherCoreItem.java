package net.xmilon.himproveme.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import net.xmilon.himproveme.item.ModItem;

public class DeadNetherCoreItem extends Item {
    public DeadNetherCoreItem(Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (world.isClient() || !(entity instanceof PlayerEntity player)) {
            return;
        }

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, 0, true, false, true));
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        triggerCoreRebirth(entity);
        super.onItemEntityDestroyed(entity);
    }

    public static void triggerCoreRebirth(ItemEntity entity) {
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        PlayerEntity targetPlayer = null;
        Entity owner = entity.getOwner();
        if (owner instanceof PlayerEntity ownerPlayer) {
            targetPlayer = ownerPlayer;
        }

        if (targetPlayer == null) {
            targetPlayer = serverWorld.getClosestPlayer(x, y, z, 48.0, true);
        }

        serverWorld.playSound(null, x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 2.0f, 1.0f);
        serverWorld.playSound(null, x, y, z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.6f, 0.9f);
        serverWorld.playSound(null, x, y, z, SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1.2f, 1.0f);

        serverWorld.spawnParticles(ParticleTypes.EXPLOSION, x, y + 0.2, z, 2, 0.2, 0.1, 0.2, 0.0);
        serverWorld.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y + 0.5, z, 80, 0.6, 0.8, 0.6, 0.15);
        serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y + 0.2, z, 20, 0.4, 0.3, 0.4, 0.02);

        if (targetPlayer == null) {
            return;
        }

        double px = targetPlayer.getX();
        double py = targetPlayer.getY() + 1.0;
        double pz = targetPlayer.getZ();

        serverWorld.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, px, py, pz, 50, 0.7, 0.9, 0.7, 0.12);
        serverWorld.spawnParticles(ParticleTypes.END_ROD, px, py, pz, 30, 0.7, 0.8, 0.7, 0.04);
        serverWorld.playSound(null, px, py, pz, SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1.0f, 1.15f);

        targetPlayer.giveItemStack(new ItemStack(ModItem.NETHER_CORE));
    }
}
