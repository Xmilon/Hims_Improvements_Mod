package net.xmilon.himproveme.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.xmilon.himproveme.access.DualWieldTargetAccess;
import net.xmilon.himproveme.item.ModItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements DualWieldTargetAccess {
    @Shadow
    public int hurtTime;

    @Shadow
    public int maxHurtTime;

    @Shadow
    protected float lastDamageTaken;

    @Redirect(
            method = "tickFallFlying",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private boolean himproveme$allowGodlyElytraInFlightCheck(ItemStack stack, net.minecraft.item.Item item) {
        return stack.isOf(Items.ELYTRA) || stack.isOf(ModItem.GODLY_ELYTRA);
    }

    @Redirect(
            method = "tickFallFlying",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ElytraItem;isUsable(Lnet/minecraft/item/ItemStack;)Z")
    )
    private boolean himproveme$allowGodlyElytraUsable(ItemStack stack) {
        if (stack.isOf(ModItem.GODLY_ELYTRA)) {
            return true;
        }
        return net.minecraft.item.ElytraItem.isUsable(stack);
    }

    @Redirect(
            method = "tickFallFlying",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V")
    )
    private void himproveme$preventGodlyElytraDamage(ItemStack stack, int amount, LivingEntity entity, EquipmentSlot slot) {
        if (stack.isOf(ModItem.GODLY_ELYTRA)) {
            return;
        }
        stack.damage(amount, entity, slot);
    }

    @Override
    public void himproveme$resetDualWieldDamageImmunity() {
        this.hurtTime = 0;
        this.maxHurtTime = 0;
        this.lastDamageTaken = 0.0f;
    }
}
