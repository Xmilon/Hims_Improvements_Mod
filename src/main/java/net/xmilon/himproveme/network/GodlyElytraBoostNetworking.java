package net.xmilon.himproveme.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.xmilon.himproveme.item.ModItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GodlyElytraBoostNetworking {
    private static final long BOOST_COOLDOWN_TICKS = 100L;
    private static final Map<UUID, Long> NEXT_BOOST_TICK = new HashMap<>();

    private GodlyElytraBoostNetworking() {
    }

    public static void registerServerReceiver() {
        PayloadTypeRegistry.playC2S().register(GodlyElytraBoostPayload.ID, GodlyElytraBoostPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(GodlyElytraBoostPayload.ID, (payload, context) ->
                context.server().execute(() -> tryBoost(context.player())));
    }

    private static void tryBoost(PlayerEntity player) {
        ItemStack chestStack = player.getEquippedStack(EquipmentSlot.CHEST);
        if (!chestStack.isOf(ModItem.GODLY_ELYTRA) || !player.isFallFlying()) {
            return;
        }

        long now = player.getWorld().getTime();
        long nextBoostTick = NEXT_BOOST_TICK.getOrDefault(player.getUuid(), 0L);
        if (now < nextBoostTick) {
            long remainingTicks = nextBoostTick - now;
            int remainingSeconds = (int) Math.ceil(remainingTicks / 20.0);
            player.sendMessage(Text.literal("Recharge in " + remainingSeconds + "s").formatted(Formatting.RED), true);
            return;
        }

        Vec3d lookVector = player.getRotationVector();
        Vec3d velocity = player.getVelocity();
        Vec3d boostedVelocity = velocity.add(
                lookVector.x * 0.18 + (lookVector.x * 2.6 - velocity.x) * 0.75,
                lookVector.y * 0.18 + (lookVector.y * 2.6 - velocity.y) * 0.75,
                lookVector.z * 0.18 + (lookVector.z * 2.6 - velocity.z) * 0.75
        );

        player.setVelocity(boostedVelocity);
        player.velocityModified = true;
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, 0.8f, 1.0f);

        NEXT_BOOST_TICK.put(player.getUuid(), now + BOOST_COOLDOWN_TICKS);
    }
}
