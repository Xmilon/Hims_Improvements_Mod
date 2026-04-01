package net.xmilon.himproveme.item.custom;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public final class ShulkerItemInteractionEvents {
    private ShulkerItemInteractionEvents() {
    }

    public static void register() {
        UseItemCallback.EVENT.register(ShulkerItemInteractionEvents::handleUse);
    }

    private static TypedActionResult<ItemStack> handleUse(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (player.isSpectator() || !LockableContainerHelper.isShulkerBox(stack)) {
            return TypedActionResult.pass(stack);
        }

        if (world.isClient) {
            return TypedActionResult.success(stack);
        }

        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return TypedActionResult.pass(stack);
        }

        if (LockableContainerHelper.isLocked(stack)) {
            LockableContainerHelper.denyAccess(player);
            return TypedActionResult.fail(stack);
        }

        HeldShulkerBoxInventory inventory = new HeldShulkerBoxInventory(stack);
        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.BLOCK_SHULKER_BOX_OPEN,
                SoundCategory.PLAYERS,
                0.8f,
                0.95f + world.random.nextFloat() * 0.1f
        );
        int lockedHotbarSlot = hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : -1;
        int lockedSwapButton = hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
        serverPlayer.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, ignoredPlayer) -> new HeldShulkerBoxScreenHandler(syncId, playerInventory, inventory, lockedHotbarSlot, lockedSwapButton),
                stack.getName()
        ));
        return TypedActionResult.success(stack, false);
    }
}
