package net.xmilon.himproveme.mixin;

import net.minecraft.entity.ItemEntity;
import net.xmilon.himproveme.item.ModItem;
import net.xmilon.himproveme.item.custom.DeadNetherCoreItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void himproveme$handleVoidDeadNetherCore(CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;

        if (itemEntity.getWorld().isClient()) {
            return;
        }

        itemEntity.setGlowing(itemEntity.getStack().isOf(ModItem.NETHER_CORE));

        if (!itemEntity.getStack().isOf(ModItem.DEAD_NETHER_CORE)) {
            return;
        }

        if (itemEntity.getY() > itemEntity.getWorld().getBottomY() - 1) {
            return;
        }

        DeadNetherCoreItem.triggerCoreRebirth(itemEntity);
        itemEntity.discard();
        ci.cancel();
    }
}
