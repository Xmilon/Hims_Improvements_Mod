package net.xmilon.himproveme.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.xmilon.himproveme.item.custom.BundleUpgradeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackBundleSanitizerMixin {
    @Inject(method = "encode(Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;Lnet/minecraft/nbt/NbtElement;)Lnet/minecraft/nbt/NbtElement;", at = @At("HEAD"))
    private void himproveme$normalizeBundleContentsBeforeEncode(CallbackInfoReturnable<NbtElement> cir) {
        BundleUpgradeHelper.normalizeContents((ItemStack) (Object) this);
    }

    @Inject(method = "encode(Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/nbt/NbtElement;", at = @At("HEAD"))
    private void himproveme$normalizeBundleContentsBeforeEncodeStart(CallbackInfoReturnable<NbtElement> cir) {
        BundleUpgradeHelper.normalizeContents((ItemStack) (Object) this);
    }
}
