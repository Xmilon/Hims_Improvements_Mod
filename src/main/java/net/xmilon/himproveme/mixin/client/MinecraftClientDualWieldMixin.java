package net.xmilon.himproveme.mixin.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.village.Merchant;
import net.xmilon.himproveme.client.BlowgunClientHelper;
import net.xmilon.himproveme.client.DaggerGripClientHelper;
import net.xmilon.himproveme.combat.DualWieldCombatHelper;
import net.xmilon.himproveme.combat.ShieldBashHelper;
import net.xmilon.himproveme.compat.SpearBackportCompat;
import net.xmilon.himproveme.item.custom.DaggerItem;
import net.xmilon.himproveme.network.DualWieldAttackPayload;
import net.xmilon.himproveme.network.ShieldBashPayload;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientDualWieldMixin {
    @Shadow
    @Final
    public GameRenderer gameRenderer;

    @Shadow
    @Final
    public GameOptions options;

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Nullable
    public HitResult crosshairTarget;

    @Shadow
    public ClientPlayerInteractionManager interactionManager;

    @Shadow
    public ClientWorld world;

    @Shadow
    private int itemUseCooldown;

    @Unique
    private boolean himproveme$offhandUseConsumed;
    @Unique
    private boolean himproveme$bashAttackConsumed;

    @Inject(
            method = "doAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void himproveme$handleShieldBashAndOffhandPrimaryAttack(CallbackInfoReturnable<Boolean> cir) {
        if (this.player == null) {
            return;
        }

        if (BlowgunClientHelper.tryHandleAttack((MinecraftClient) (Object) this)) {
            cir.setReturnValue(true);
            return;
        }

        if (DaggerGripClientHelper.shouldSuppressAttack(
                this.player,
                this.crosshairTarget,
                this.options.attackKey.isPressed(),
                this.options.useKey.isPressed()
        )) {
            cir.setReturnValue(true);
            return;
        }

        if (DualWieldCombatHelper.isBashAttackLocked(this.player)) {
            cir.setReturnValue(true);
            return;
        }

        if (this.himproveme$bashAttackConsumed) {
            cir.setReturnValue(true);
            return;
        }

        Hand bashHand = ShieldBashHelper.getActiveBashHand(this.player);
        if (bashHand != null) {
            himproveme$triggerShieldBash(bashHand, himproveme$getCrosshairEntityIdOrMiss(ShieldBashPayload.SWING_ONLY_ENTITY_ID));
            cir.setReturnValue(true);
            return;
        }

        if (SpearBackportCompat.shouldUseOffhandAttack(this.player)
                && DualWieldCombatHelper.shouldUseOffhandAttackAsPrimary(this.player)) {
            if (!DualWieldCombatHelper.isOffhandAttackReady(this.player)) {
                cir.setReturnValue(false);
                return;
            }

            himproveme$triggerOffhandAttack(himproveme$getCrosshairEntityIdOrMiss(DualWieldAttackPayload.SWING_ONLY_ENTITY_ID));
            cir.setReturnValue(true);
            return;
        }

        if (this.crosshairTarget instanceof EntityHitResult entityHitResult
                && DaggerItem.isDagger(this.player.getMainHandStack())
                && !DualWieldCombatHelper.isTargetInRange(this.player, this.player.getMainHandStack(), entityHitResult.getEntity())) {
            cir.setReturnValue(false);
            return;
        }

        if (DualWieldCombatHelper.canUseOffhandAttack(this.player)
                && (this.player.getMainHandStack().isEmpty() || ShieldBashHelper.isShield(this.player.getMainHandStack()))) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void himproveme$handleShieldBashWhileUsingShield(CallbackInfo ci) {
        if (this.player == null || !this.player.isUsingItem()) {
            return;
        }

        if (ShieldBashHelper.isShield(this.player.getActiveItem()) && !this.player.isSneaking()) {
            this.interactionManager.stopUsingItem(this.player);
            return;
        }

        Hand bashHand = ShieldBashHelper.getActiveBashHand(this.player);
        if (bashHand == null) {
            return;
        }

        while (this.options.attackKey.wasPressed()) {
            himproveme$triggerShieldBash(bashHand, himproveme$getCrosshairEntityIdOrMiss(ShieldBashPayload.SWING_ONLY_ENTITY_ID));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void himproveme$resetOffhandUseLatch(CallbackInfo ci) {
        if (this.player != null
                && this.player.isUsingItem()
                && ShieldBashHelper.isShield(this.player.getActiveItem())
                && !this.player.isSneaking()) {
            this.interactionManager.stopUsingItem(this.player);
        }

        if (!this.options.useKey.isPressed()) {
            this.himproveme$offhandUseConsumed = false;
        }

        if (!this.options.attackKey.isPressed()) {
            this.himproveme$bashAttackConsumed = false;
        }
    }

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void himproveme$handleOffhandUseFallback(CallbackInfo ci) {
        ci.cancel();

        if (this.interactionManager.isBreakingBlock()) {
            return;
        }

        this.itemUseCooldown = 4;
        if (this.player == null || this.player.isRiding()) {
            return;
        }

        if (this.player.isSneaking() && himproveme$trySneakUiInteraction()) {
            return;
        }

        if (DualWieldCombatHelper.canUseOffhandAttack(this.player) && !this.player.isSneaking()) {
            if (himproveme$consumeOffhandUsePress()) {
                himproveme$triggerOffhandAttack(himproveme$getOffhandAttackEntityId());
            }
            return;
        }

        for (Hand hand : himproveme$getUseOrder()) {
            ItemStack itemStack = this.player.getStackInHand(hand);
            if (!itemStack.isItemEnabled(this.world.getEnabledFeatures())) {
                return;
            }

            if (this.crosshairTarget != null) {
                switch (this.crosshairTarget.getType()) {
                    case ENTITY -> {
                        EntityHitResult entityHitResult = (EntityHitResult) this.crosshairTarget;
                        Entity entity = entityHitResult.getEntity();
                        if (!this.world.getWorldBorder().contains(entity.getBlockPos())) {
                            return;
                        }

                        ActionResult actionResult = this.interactionManager.interactEntityAtLocation(this.player, entity, entityHitResult, hand);
                        if (!actionResult.isAccepted()) {
                            actionResult = this.interactionManager.interactEntity(this.player, entity, hand);
                        }

                        if (actionResult.isAccepted()) {
                            if (actionResult.shouldSwingHand()) {
                                this.player.swingHand(hand);
                            }
                            return;
                        }
                    }
                    case BLOCK -> {
                        BlockHitResult blockHitResult = (BlockHitResult) this.crosshairTarget;
                        int count = itemStack.getCount();
                        ActionResult actionResult = this.interactionManager.interactBlock(this.player, hand, blockHitResult);
                        if (actionResult.isAccepted()) {
                            if (actionResult.shouldSwingHand()) {
                                this.player.swingHand(hand);
                                if (!itemStack.isEmpty() && (itemStack.getCount() != count || this.interactionManager.hasCreativeInventory())) {
                                    this.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                                }
                            }
                            return;
                        }

                        if (actionResult == ActionResult.FAIL) {
                            return;
                        }
                    }
                    case MISS -> {
                    }
                }
            }

            if (!itemStack.isEmpty()) {
                if (ShieldBashHelper.isShield(itemStack) && !ShieldBashHelper.canRaiseShield(this.player, hand)) {
                    continue;
                }

                ActionResult actionResult = this.interactionManager.interactItem(this.player, hand);
                if (actionResult.isAccepted()) {
                    if (actionResult.shouldSwingHand()) {
                        this.player.swingHand(hand);
                    }

                    this.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                    return;
                }
            }
        }

        if (!DualWieldCombatHelper.canUseOffhandAttack(this.player) || !himproveme$consumeOffhandUsePress()) {
            return;
        }

        himproveme$triggerOffhandAttack(himproveme$getOffhandAttackEntityId());
    }

    @Unique
    private boolean himproveme$trySneakUiInteraction() {
        if (this.crosshairTarget == null || !himproveme$isSneakUiTarget()) {
            return false;
        }

        for (Hand hand : himproveme$getUseOrder()) {
            if (himproveme$tryInteractCurrentTarget(hand)) {
                return true;
            }
        }

        return true;
    }

    @Unique
    private boolean himproveme$isSneakUiTarget() {
        if (this.player == null || this.crosshairTarget == null) {
            return false;
        }

        return switch (this.crosshairTarget.getType()) {
            case BLOCK -> {
                BlockHitResult blockHitResult = (BlockHitResult) this.crosshairTarget;
                yield this.world.getBlockState(blockHitResult.getBlockPos())
                        .createScreenHandlerFactory(this.world, blockHitResult.getBlockPos()) != null;
            }
            case ENTITY -> {
                Entity entity = ((EntityHitResult) this.crosshairTarget).getEntity();
                yield entity instanceof Merchant
                        || entity instanceof NamedScreenHandlerFactory
                        || entity instanceof AbstractHorseEntity;
            }
            case MISS -> false;
        };
    }

    @Unique
    private boolean himproveme$tryInteractCurrentTarget(Hand hand) {
        if (this.player == null || this.crosshairTarget == null) {
            return false;
        }

        return switch (this.crosshairTarget.getType()) {
            case ENTITY -> {
                EntityHitResult entityHitResult = (EntityHitResult) this.crosshairTarget;
                Entity entity = entityHitResult.getEntity();
                if (!this.world.getWorldBorder().contains(entity.getBlockPos())) {
                    yield true;
                }

                ActionResult actionResult = this.interactionManager.interactEntityAtLocation(this.player, entity, entityHitResult, hand);
                if (!actionResult.isAccepted()) {
                    actionResult = this.interactionManager.interactEntity(this.player, entity, hand);
                }

                if (actionResult.isAccepted() && actionResult.shouldSwingHand()) {
                    this.player.swingHand(hand);
                }
                yield actionResult.isAccepted();
            }
            case BLOCK -> {
                BlockHitResult blockHitResult = (BlockHitResult) this.crosshairTarget;
                ItemStack itemStack = this.player.getStackInHand(hand);
                int count = itemStack.getCount();
                ActionResult actionResult = this.interactionManager.interactBlock(this.player, hand, blockHitResult);
                if (actionResult.isAccepted() && actionResult.shouldSwingHand()) {
                    this.player.swingHand(hand);
                    if (!itemStack.isEmpty() && (itemStack.getCount() != count || this.interactionManager.hasCreativeInventory())) {
                        this.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                    }
                }
                yield actionResult != ActionResult.PASS;
            }
            case MISS -> false;
        };
    }

    private void himproveme$triggerOffhandAttack(int entityId) {
        if (this.player == null) {
            return;
        }

        DualWieldCombatHelper.startOffhandAttack(this.player);
        himproveme$startLocalThirdPersonOffhandSwing();
        ClientPlayNetworking.send(new DualWieldAttackPayload(entityId));
    }

    @Unique
    private void himproveme$triggerShieldBash(Hand hand, int entityId) {
        if (this.player == null) {
            return;
        }

        ItemStack shieldStack = this.player.getStackInHand(hand);
        int level = ShieldBashHelper.getBashingLevel(this.player, shieldStack);
        if (level <= 0) {
            return;
        }

        if (hand == Hand.OFF_HAND) {
            DualWieldCombatHelper.startOffhandSwing(this.player);
            himproveme$startLocalThirdPersonOffhandSwing();
        }
        this.himproveme$bashAttackConsumed = true;
        DualWieldCombatHelper.lockAttacksAfterBash(this.player);
        this.player.getItemCooldownManager().set(shieldStack.getItem(), ShieldBashHelper.getBashCooldownTicks(level));
        ClientPlayNetworking.send(new ShieldBashPayload(hand, entityId));
    }

    @Unique
    private int himproveme$getCrosshairEntityIdOrMiss(int missEntityId) {
        if (this.crosshairTarget instanceof EntityHitResult entityHitResult
                && DualWieldCombatHelper.isValidOffhandTarget(this.player, entityHitResult.getEntity())) {
            return entityHitResult.getEntity().getId();
        }

        return missEntityId;
    }

    @Unique
    private boolean himproveme$consumeOffhandUsePress() {
        if (this.himproveme$offhandUseConsumed) {
            return false;
        }

        this.himproveme$offhandUseConsumed = true;
        return true;
    }

    @Unique
    private int himproveme$getOffhandAttackEntityId() {
        if (this.crosshairTarget instanceof EntityHitResult entityHitResult
                && DualWieldCombatHelper.isValidOffhandTarget(this.player, entityHitResult.getEntity())
                && DualWieldCombatHelper.isOffhandTargetInRange(this.player, entityHitResult.getEntity())) {
            return entityHitResult.getEntity().getId();
        }

        return DualWieldAttackPayload.SWING_ONLY_ENTITY_ID;
    }

    @Unique
    private void himproveme$startLocalThirdPersonOffhandSwing() {
        if (this.player == null || this.options.getPerspective().isFirstPerson()) {
            return;
        }

        this.player.swingHand(Hand.OFF_HAND);
    }

    @Unique
    private Hand[] himproveme$getUseOrder() {
        if (this.player == null) {
            return Hand.values();
        }

        if (ShieldBashHelper.canRaiseShield(this.player, Hand.OFF_HAND)) {
            return new Hand[]{Hand.OFF_HAND, Hand.MAIN_HAND};
        }

        if (ShieldBashHelper.canRaiseShield(this.player, Hand.MAIN_HAND)) {
            return new Hand[]{Hand.MAIN_HAND, Hand.OFF_HAND};
        }

        return Hand.values();
    }
}
