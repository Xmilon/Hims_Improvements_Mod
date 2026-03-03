package net.xmilon.himproveme.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class GodlyElytraItem extends ElytraItem {
    public GodlyElytraItem(Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (world.isClient() || !(entity instanceof PlayerEntity player)) {
            return;
        }

        if (!player.getEquippedStack(EquipmentSlot.CHEST).isOf(this)) {
            return;
        }

        ServerWorld serverWorld = (ServerWorld) world;
        Vec3d look = player.getRotationVector();
        Vec3d back = look.multiply(-0.75);
        double x = player.getX() + back.x;
        double z = player.getZ() + back.z;

        if (player.age % 20 != 0) {
            return;
        }

        if (player.isInSwimmingPose()) {
            double y = player.getY() + 0.72;
            serverWorld.spawnParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.06, 0.03, 0.06, 0.0);
            return;
        }

        double[] wingHeights = {0.72, 0.96, 1.18};
        for (double h : wingHeights) {
            serverWorld.spawnParticles(ParticleTypes.END_ROD, x, player.getY() + h, z, 1, 0.06, 0.03, 0.06, 0.0);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.translatable("item.himproveme.godly_elytra.desc"));
    }
}
