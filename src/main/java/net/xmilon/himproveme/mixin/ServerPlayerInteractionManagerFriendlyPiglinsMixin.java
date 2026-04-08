package net.xmilon.himproveme.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.xmilon.himproveme.perk.NetherPerkHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerFriendlyPiglinsMixin {
    @Shadow
    protected ServerPlayerEntity player;

    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void himproveme$markPiglinChestTrespass(
            ServerPlayerEntity player,
            World world,
            ItemStack stack,
            Hand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        if (cir.getReturnValue().isAccepted()) {
            NetherPerkHelper.maybeAlertPiglinsForInteraction(player, world, hitResult.getBlockPos());
        }
    }

    @Inject(method = "tryBreakBlock", at = @At("RETURN"))
    private void himproveme$markPiglinGoldTrespass(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            NetherPerkHelper.maybeAlertPiglinsForInteraction(this.player, this.player.getWorld(), pos);
        }
    }
}
