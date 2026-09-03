package net.xmilon.himproveme.mixin;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.xmilon.himproveme.effect.ModStatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityGoldenFoodMixin {
    @Inject(method = "eatFood", at = @At("HEAD"))
    private void himproveme$removeEffectsWithGoldenFood(World world, ItemStack stack, FoodComponent foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self.getWorld().isClient()) return;

        if (stack.isOf(Items.GOLDEN_APPLE) || stack.isOf(Items.ENCHANTED_GOLDEN_APPLE) || stack.isOf(Items.GOLDEN_CARROT)) {
            self.removeStatusEffect(ModStatusEffects.BLEEDING);
            self.removeStatusEffect(ModStatusEffects.STUNNED);
        }
    }
}
