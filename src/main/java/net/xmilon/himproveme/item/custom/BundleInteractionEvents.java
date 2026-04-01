package net.xmilon.himproveme.item.custom;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class BundleInteractionEvents {
    private BundleInteractionEvents() {
    }

    public static void register() {
        UseEntityCallback.EVENT.register(BundleInteractionEvents::handleItemEntityInteraction);
        UseItemCallback.EVENT.register(BundleInteractionEvents::handleHandToBundleInteraction);
    }

    private static ActionResult handleItemEntityInteraction(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable net.minecraft.util.hit.EntityHitResult hitResult) {
        if (player.isSpectator() || !(entity instanceof ItemEntity itemEntity)) {
            return ActionResult.PASS;
        }

        Hand bundleHand = findBundleHand(player, hand);
        if (bundleHand == null) {
            return ActionResult.PASS;
        }

        ItemStack bundleStack = player.getStackInHand(bundleHand);
        ItemStack entityStack = itemEntity.getStack();
        if (!BundleUpgradeHelper.canInsert(bundleStack, entityStack)) {
            return ActionResult.PASS;
        }

        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        Item pickedItem = entityStack.getItem();
        int inserted = BundleUpgradeHelper.tryInsert(bundleStack, entityStack);
        if (inserted <= 0) {
            return ActionResult.PASS;
        }

        player.setStackInHand(bundleHand, bundleStack);
        player.getInventory().markDirty();
        itemEntity.setStack(entityStack);
        if (entityStack.isEmpty()) {
            itemEntity.discard();
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendPickup(itemEntity, inserted);
            serverPlayer.increaseStat(Stats.PICKED_UP.getOrCreateStat(pickedItem), inserted);
            serverPlayer.triggerItemPickedUpByEntityCriteria(itemEntity);
        }

        player.currentScreenHandler.syncState();
        playInsertSound(player);
        return ActionResult.SUCCESS;
    }

    private static TypedActionResult<ItemStack> handleHandToBundleInteraction(PlayerEntity player, World world, Hand hand) {
        if (player.isSpectator()) {
            return TypedActionResult.pass(player.getStackInHand(hand));
        }

        HandTransferContext context = findHandTransferContext(player, hand);
        if (context == null || !BundleUpgradeHelper.canInsert(context.bundleStack(), context.sourceStack())) {
            return TypedActionResult.pass(player.getStackInHand(hand));
        }

        if (world.isClient) {
            return TypedActionResult.success(player.getStackInHand(hand), true);
        }

        int inserted = BundleUpgradeHelper.tryInsert(context.bundleStack(), context.sourceStack());
        if (inserted <= 0) {
            return TypedActionResult.pass(player.getStackInHand(hand));
        }

        player.setStackInHand(context.bundleHand(), context.bundleStack());
        player.setStackInHand(context.sourceHand(), context.sourceStack().isEmpty() ? ItemStack.EMPTY : context.sourceStack());
        player.getInventory().markDirty();
        player.currentScreenHandler.syncState();
        playInsertSound(player);
        return TypedActionResult.success(player.getStackInHand(hand), true);
    }

    @Nullable
    private static Hand findBundleHand(PlayerEntity player, Hand preferredHand) {
        if (BundleUpgradeHelper.isBundle(player.getStackInHand(preferredHand))) {
            return preferredHand;
        }

        Hand otherHand = otherHand(preferredHand);
        return BundleUpgradeHelper.isBundle(player.getStackInHand(otherHand)) ? otherHand : null;
    }

    @Nullable
    private static HandTransferContext findHandTransferContext(PlayerEntity player, Hand preferredHand) {
        ItemStack preferredStack = player.getStackInHand(preferredHand);
        Hand otherHand = otherHand(preferredHand);
        ItemStack otherStack = player.getStackInHand(otherHand);

        if (BundleUpgradeHelper.isBundle(preferredStack) && !otherStack.isEmpty() && !BundleUpgradeHelper.isBundle(otherStack)) {
            return new HandTransferContext(preferredHand, preferredStack, otherHand, otherStack);
        }

        if (BundleUpgradeHelper.isBundle(otherStack) && !preferredStack.isEmpty() && !BundleUpgradeHelper.isBundle(preferredStack)) {
            return new HandTransferContext(otherHand, otherStack, preferredHand, preferredStack);
        }

        return null;
    }

    private static Hand otherHand(Hand hand) {
        return hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }

    private static void playInsertSound(PlayerEntity player) {
        player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + player.getWorld().random.nextFloat() * 0.4F);
    }

    private record HandTransferContext(Hand bundleHand, ItemStack bundleStack, Hand sourceHand, ItemStack sourceStack) {
    }
}
