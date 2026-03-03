package net.xmilon.himproveme.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.xmilon.himproveme.item.ModItem;
import net.xmilon.himproveme.prone.ProneNetworking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.entity.player.PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Redirect(
            method = "checkFallFlying",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private boolean himproveme$allowGodlyElytraInStartCheck(ItemStack stack, net.minecraft.item.Item item) {
        return stack.isOf(Items.ELYTRA) || stack.isOf(ModItem.GODLY_ELYTRA);
    }

    @Redirect(
            method = "checkFallFlying",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ElytraItem;isUsable(Lnet/minecraft/item/ItemStack;)Z")
    )
    private boolean himproveme$allowGodlyElytraUsable(ItemStack stack) {
        if (stack.isOf(ModItem.GODLY_ELYTRA)) {
            return true;
        }
        return net.minecraft.item.ElytraItem.isUsable(stack);
    }

    @Inject(method = "updatePose", at = @At("HEAD"), cancellable = true)
    private void himproveme$lockPronePose(CallbackInfo ci) {
        net.minecraft.entity.player.PlayerEntity self = (net.minecraft.entity.player.PlayerEntity) (Object) this;
        boolean proneLocked = self instanceof ServerPlayerEntity serverPlayer && ProneNetworking.isProne(serverPlayer.getUuid());
        if (proneLocked) {
            self.setSwimming(true);
            self.setPose(net.minecraft.entity.EntityPose.SWIMMING);
            self.setSprinting(false);
            ci.cancel();
        }
    }
}
