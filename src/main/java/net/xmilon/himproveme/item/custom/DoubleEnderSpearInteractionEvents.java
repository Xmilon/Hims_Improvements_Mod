package net.xmilon.himproveme.item.custom;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.xmilon.himproveme.combat.EnderSpearSlashHelper;
import net.xmilon.himproveme.compat.SpearBackportCompat;

public final class DoubleEnderSpearInteractionEvents {
    private DoubleEnderSpearInteractionEvents() {
    }

    public static void register() {
        UseItemCallback.EVENT.register(DoubleEnderSpearInteractionEvents::handleUse);
    }

    private static TypedActionResult<ItemStack> handleUse(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!SpearBackportCompat.isCompatDoubleEnderSpear(stack)) {
            return TypedActionResult.pass(stack);
        }

        if (player.isSpectator()) {
            return TypedActionResult.fail(stack);
        }

        if (!EnderSpearSlashHelper.activateSlash(player, hand, stack)) {
            return TypedActionResult.fail(stack);
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}
