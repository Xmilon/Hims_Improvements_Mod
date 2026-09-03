package net.xmilon.himproveme.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Hand;
import net.xmilon.himproveme.access.DualWieldAttackAccess;
import net.xmilon.himproveme.combat.DualWieldAttackContext;
import net.xmilon.himproveme.combat.DualWieldCombatHelper;
import net.xmilon.himproveme.item.custom.DaggerItem;
import net.xmilon.himproveme.item.custom.GodlyElytraItem;
import net.xmilon.himproveme.perk.PerkAccess;
import net.xmilon.himproveme.prone.ProneNetworking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.entity.player.PlayerEntity.class)
public abstract class PlayerEntityMixin implements DualWieldAttackAccess {
    @Unique
    private static final SoundEvent HIMPROVEME_DAGGER_HIT_SOUND = SoundEvent.of(Identifier.of("himproveme", "item.dagger.hit"));
    @Unique
    private static final SoundEvent HIMPROVEME_DAGGER_AIR_SOUND = SoundEvent.of(Identifier.of("himproveme", "item.dagger.air"));
    @Unique
    private static final float HIMPROVEME_DAGGER_HIT_VOLUME = 0.72f;
    @Unique
    private static final float HIMPROVEME_DAGGER_AIR_VOLUME = 0.42f;

    @Unique
    private boolean himproveme$safeLevitationArmed;
    @Unique
    private int himproveme$offhandLastAttackedTicks = DualWieldCombatHelper.getReadyOffhandTicks();
    @Unique
    private boolean himproveme$offhandSwinging;
    @Unique
    private int himproveme$offhandSwingTicks;
    @Unique
    private float himproveme$lastOffhandSwingProgress;
    @Unique
    private float himproveme$offhandSwingProgress;
    @Unique
    private int himproveme$bashAttackLockTicks;

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

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void himproveme$updateSafeLevitationBeforeMovement(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!PerkAccess.hasSafeLevitation(self)) {
            himproveme$clearSafeLevitation();
            return;
        }

        boolean hasLevitation = self.hasStatusEffect(StatusEffects.LEVITATION);
        if (hasLevitation) {
            himproveme$safeLevitationArmed = true;
        }

        if (!hasLevitation && self.isOnGround() && self.fallDistance <= 0.0f) {
            himproveme$clearSafeLevitation();
        }
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void himproveme$updateSafeLevitationAfterMovement(CallbackInfo ci) {
        if (!himproveme$safeLevitationArmed) {
            return;
        }

        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self.isTouchingWater() || self.isInLava()) {
            himproveme$clearSafeLevitation();
            return;
        }

        if ((self.horizontalCollision || self.verticalCollision) && !self.isOnGround()) {
            himproveme$clearSafeLevitation();
        }
    }

    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void himproveme$consumeSafeLevitationLanding(float fallDistance, float damageMultiplier, net.minecraft.entity.damage.DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (!himproveme$safeLevitationArmed) {
            return;
        }

        himproveme$clearSafeLevitation();
        cir.setReturnValue(false);
    }

    @Inject(method = "checkFallFlying", at = @At("RETURN"), cancellable = true)
    private void himproveme$checkGodlyElytraFallFlying(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;

        PlayerEntity self = (PlayerEntity)(Object)this;
        if (self.isOnGround() || self.isFallFlying() || self.isTouchingWater() || self.hasStatusEffect(StatusEffects.LEVITATION)) return;

        ItemStack chest = self.getEquippedStack(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof GodlyElytraItem) {
            self.startFallFlying();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void himproveme$tickOffhandState(CallbackInfo ci) {
        if (himproveme$offhandLastAttackedTicks < Integer.MAX_VALUE) {
            himproveme$offhandLastAttackedTicks++;
        }
        if (himproveme$bashAttackLockTicks > 0) {
            himproveme$bashAttackLockTicks--;
        }

        himproveme$lastOffhandSwingProgress = himproveme$offhandSwingProgress;
        if (himproveme$offhandSwinging) {
            himproveme$offhandSwingTicks++;
            if (himproveme$offhandSwingTicks >= himproveme$getOffhandSwingDuration()) {
                himproveme$offhandSwingTicks = 0;
                himproveme$offhandSwinging = false;
            }
        } else {
            himproveme$offhandSwingTicks = 0;
        }

        himproveme$offhandSwingProgress = (float) himproveme$offhandSwingTicks / (float) himproveme$getOffhandSwingDuration();
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void himproveme$limitDaggerReach(Entity target, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        ItemStack attackingStack = DualWieldAttackContext.isOffhandAttack(self) ? self.getOffHandStack() : self.getMainHandStack();
        if (DaggerItem.isDagger(attackingStack) && !DualWieldCombatHelper.isTargetInRange(self, attackingStack, target)) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getWeaponStack()Lnet/minecraft/item/ItemStack;")
    )
    private ItemStack himproveme$useOffhandWeaponStackDuringDualWieldAttack(PlayerEntity player) {
        if (DualWieldAttackContext.isOffhandAttack(player)) {
            return player.getOffHandStack();
        }
        return player.getWeaponStack();
    }

    @Redirect(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeValue(Lnet/minecraft/registry/entry/RegistryEntry;)D")
    )
    private double himproveme$useOffhandAttackDamage(PlayerEntity player, RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute) {
        if (DualWieldAttackContext.isOffhandAttack(player) && attribute.equals(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
            return DualWieldCombatHelper.getOffhandAttackDamage(player);
        }
        return player.getAttributeValue(attribute);
    }

    @Redirect(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttackCooldownProgress(F)F")
    )
    private float himproveme$useOffhandAttackCooldownProgress(PlayerEntity player, float baseTime) {
        if (DualWieldAttackContext.isOffhandAttack(player)) {
            return DualWieldCombatHelper.getOffhandAttackCooldownProgress(player, baseTime);
        }
        return player.getAttackCooldownProgress(baseTime);
    }

    @Redirect(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;resetLastAttackedTicks()V")
    )
    private void himproveme$keepMainHandCooldownWhenOffhandAttacking(PlayerEntity player) {
        if (DualWieldAttackContext.isOffhandAttack(player)) {
            DualWieldCombatHelper.resetOffhandAttackTicks(player);
            return;
        }
        player.resetLastAttackedTicks();
    }

    @Redirect(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;")
    )
    private ItemStack himproveme$useOffhandSwordForSweepingCheck(PlayerEntity player, Hand hand) {
        if (DualWieldAttackContext.isOffhandAttack(player) && hand == Hand.MAIN_HAND) {
            return player.getOffHandStack();
        }
        return player.getStackInHand(hand);
    }

    @ModifyArgs(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"
            )
    )
    private void himproveme$replaceDaggerAttackSounds(Args args) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        ItemStack attackingStack = DualWieldAttackContext.isOffhandAttack(self) ? self.getOffHandStack() : self.getMainHandStack();
        SoundEvent sound = args.get(4);
        if (DaggerItem.isDagger(attackingStack) && himproveme$isDaggerAttackSound(sound)) {
            boolean airSwing = sound == SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE;
            args.set(4, airSwing ? HIMPROVEME_DAGGER_AIR_SOUND : HIMPROVEME_DAGGER_HIT_SOUND);
            args.set(6, airSwing ? HIMPROVEME_DAGGER_AIR_VOLUME : HIMPROVEME_DAGGER_HIT_VOLUME);
            float pitch = args.get(7);
            args.set(7, pitch * 1.25f);
        }
    }

    @Unique
    private void himproveme$clearSafeLevitation() {
        himproveme$safeLevitationArmed = false;
    }

    @Override
    public int himproveme$getOffhandLastAttackedTicks() {
        return himproveme$offhandLastAttackedTicks;
    }

    @Override
    public void himproveme$resetOffhandLastAttackedTicks() {
        himproveme$offhandLastAttackedTicks = 0;
    }

    @Override
    public void himproveme$startOffhandSwing() {
        if (!himproveme$offhandSwinging
                || himproveme$offhandSwingTicks >= himproveme$getOffhandSwingDuration() / 2
                || himproveme$offhandSwingTicks < 0) {
            himproveme$offhandSwingTicks = -1;
            himproveme$offhandSwinging = true;
        }
    }

    @Override
    public boolean himproveme$isOffhandSwingActive() {
        return himproveme$offhandSwinging;
    }

    @Override
    public float himproveme$getOffhandSwingProgress(float tickDelta) {
        float progressDelta = himproveme$offhandSwingProgress - himproveme$lastOffhandSwingProgress;
        if (progressDelta < 0.0f) {
            progressDelta += 1.0f;
        }

        return himproveme$lastOffhandSwingProgress + progressDelta * tickDelta;
    }

    @Override
    public void himproveme$setBashAttackLockTicks(int ticks) {
        himproveme$bashAttackLockTicks = Math.max(himproveme$bashAttackLockTicks, ticks);
    }

    @Override
    public boolean himproveme$hasBashAttackLock() {
        return himproveme$bashAttackLockTicks > 0;
    }

    @Unique
    private int himproveme$getOffhandSwingDuration() {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (StatusEffectUtil.hasHaste(self)) {
            return 6 - (1 + StatusEffectUtil.getHasteAmplifier(self));
        }

        if (self.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            return 6 + (1 + self.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) * 2;
        }

        return 6;
    }

    @Unique
    private boolean himproveme$isDaggerAttackSound(SoundEvent sound) {
        return sound == SoundEvents.ENTITY_PLAYER_ATTACK_STRONG
                || sound == SoundEvents.ENTITY_PLAYER_ATTACK_WEAK
                || sound == SoundEvents.ENTITY_PLAYER_ATTACK_CRIT
                || sound == SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK
                || sound == SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP
                || sound == SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE;
    }
}
