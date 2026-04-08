package net.xmilon.himproveme.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.combat.DualWieldCombatHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudDualWieldMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private static Identifier CROSSHAIR_ATTACK_INDICATOR_FULL_TEXTURE;

    @Shadow
    @Final
    private static Identifier CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_TEXTURE;

    @Shadow
    @Final
    private static Identifier CROSSHAIR_ATTACK_INDICATOR_PROGRESS_TEXTURE;

    @Shadow
    @Final
    private static Identifier HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE;

    @Shadow
    @Final
    private static Identifier HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE;

    @Inject(method = "renderCrosshair", at = @At("TAIL"))
    private void himproveme$renderOffhandCrosshairAttackIndicator(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ClientPlayerEntity player = this.client.player;
        if (player == null
                || this.client.options.getAttackIndicator().getValue() != AttackIndicator.CROSSHAIR
                || !DualWieldCombatHelper.canUseOffhandAttack(player)) {
            return;
        }

        float cooldownProgress = DualWieldCombatHelper.getOffhandAttackCooldownProgress(player, 0.0f);
        int x = context.getScaledWindowWidth() / 2 - 8;
        int y = context.getScaledWindowHeight() / 2 - 13;

        RenderSystem.enableBlend();
        if (cooldownProgress >= 1.0f && himproveme$hasLivingTarget()) {
            context.drawGuiTexture(CROSSHAIR_ATTACK_INDICATOR_FULL_TEXTURE, x, y, 16, 16);
        } else if (cooldownProgress < 1.0f) {
            int filledWidth = (int) (cooldownProgress * 17.0f);
            context.drawGuiTexture(CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_TEXTURE, x, y, 16, 4);
            context.drawGuiTexture(CROSSHAIR_ATTACK_INDICATOR_PROGRESS_TEXTURE, 16, 4, 0, 0, x, y, filledWidth, 4);
        }
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    private void himproveme$renderOffhandHotbarAttackIndicator(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ClientPlayerEntity player = this.client.player;
        if (player == null
                || this.client.options.getAttackIndicator().getValue() != AttackIndicator.HOTBAR
                || !DualWieldCombatHelper.canUseOffhandAttack(player)) {
            return;
        }

        float cooldownProgress = DualWieldCombatHelper.getOffhandAttackCooldownProgress(player, 0.0f);
        if (cooldownProgress >= 1.0f) {
            return;
        }

        int centerX = context.getScaledWindowWidth() / 2;
        int y = context.getScaledWindowHeight() - 20;
        Arm offhandArm = player.getMainArm().getOpposite();
        int x = offhandArm == Arm.RIGHT ? centerX + 97 : centerX - 113;
        int filledHeight = (int) (cooldownProgress * 19.0f);

        RenderSystem.enableBlend();
        context.drawGuiTexture(HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE, x, y, 18, 18);
        context.drawGuiTexture(HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE, 18, 18, 0, 18 - filledHeight, x, y + 18 - filledHeight, 18, filledHeight);
        RenderSystem.disableBlend();
    }

    private boolean himproveme$hasLivingTarget() {
        Entity targetedEntity = this.client.targetedEntity;
        return targetedEntity instanceof LivingEntity livingEntity && livingEntity.isAlive();
    }
}
