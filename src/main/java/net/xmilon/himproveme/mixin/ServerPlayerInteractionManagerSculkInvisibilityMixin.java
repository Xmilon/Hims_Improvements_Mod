package net.xmilon.himproveme.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.xmilon.himproveme.perk.PerkAccess;
import net.xmilon.himproveme.perk.SculkInvisibilityContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerSculkInvisibilityMixin {
    @Shadow protected ServerPlayerEntity player;

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"))
    private void himproveme$beginBlockBreakingSuppression(
            BlockPos pos,
            PlayerActionC2SPacket.Action action,
            Direction direction,
            int worldHeight,
            int sequence,
            CallbackInfo ci
    ) {
        himproveme$pushSuppression();
    }

    @Inject(method = "processBlockBreakingAction", at = @At("RETURN"))
    private void himproveme$endBlockBreakingSuppression(
            BlockPos pos,
            PlayerActionC2SPacket.Action action,
            Direction direction,
            int worldHeight,
            int sequence,
            CallbackInfo ci
    ) {
        himproveme$popSuppression();
    }

    @Inject(method = "finishMining", at = @At("HEAD"))
    private void himproveme$beginFinishMiningSuppression(BlockPos pos, int sequence, String reason, CallbackInfo ci) {
        himproveme$pushSuppression();
    }

    @Inject(method = "finishMining", at = @At("RETURN"))
    private void himproveme$endFinishMiningSuppression(BlockPos pos, int sequence, String reason, CallbackInfo ci) {
        himproveme$popSuppression();
    }

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void himproveme$beginTryBreakBlockSuppression(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        himproveme$pushSuppression();
    }

    @Inject(method = "tryBreakBlock", at = @At("RETURN"))
    private void himproveme$endTryBreakBlockSuppression(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        himproveme$popSuppression();
    }

    @Inject(method = "interactItem", at = @At("HEAD"))
    private void himproveme$beginInteractItemSuppression(
            ServerPlayerEntity player,
            World world,
            ItemStack stack,
            Hand hand,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        himproveme$pushSuppression();
    }

    @Inject(method = "interactItem", at = @At("RETURN"))
    private void himproveme$endInteractItemSuppression(
            ServerPlayerEntity player,
            World world,
            ItemStack stack,
            Hand hand,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        himproveme$popSuppression();
    }

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void himproveme$beginInteractBlockSuppression(
            ServerPlayerEntity player,
            World world,
            ItemStack stack,
            Hand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        himproveme$pushSuppression();
    }

    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void himproveme$endInteractBlockSuppression(
            ServerPlayerEntity player,
            World world,
            ItemStack stack,
            Hand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        himproveme$popSuppression();
    }

    private void himproveme$pushSuppression() {
        if (PerkAccess.hasSculkInvisibility(this.player)) {
            SculkInvisibilityContext.push(this.player);
        }
    }

    private void himproveme$popSuppression() {
        if (PerkAccess.hasSculkInvisibility(this.player)) {
            SculkInvisibilityContext.pop(this.player);
        }
    }
}
