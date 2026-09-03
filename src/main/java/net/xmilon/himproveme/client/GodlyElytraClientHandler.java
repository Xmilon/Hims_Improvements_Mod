package net.xmilon.himproveme.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.xmilon.himproveme.item.ModItem;

@Environment(EnvType.CLIENT)
public class GodlyElytraClientHandler {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(GodlyElytraClientHandler::onClientTick);
    }

    private static void onClientTick(MinecraftClient client) {
        if (!(client.player instanceof ClientPlayerEntity player)) return;
        if (!player.input.jumping) return;
        if (player.getAbilities().flying) return;
        if (player.hasVehicle()) return;
        if (player.isClimbing()) return;

        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        if (!chest.isOf(ModItem.GODLY_ELYTRA)) return;
        if (player.isFallFlying()) return;
        if (player.isOnGround() || player.isTouchingWater() || player.hasStatusEffect(StatusEffects.LEVITATION)) return;

        player.startFallFlying();
        player.networkHandler.sendPacket(
                new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING)
        );
    }
}
