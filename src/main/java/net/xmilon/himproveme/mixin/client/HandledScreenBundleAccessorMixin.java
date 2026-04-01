package net.xmilon.himproveme.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.xmilon.himproveme.access.HandledScreenBundleScrollAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public abstract class HandledScreenBundleAccessorMixin implements HandledScreenBundleScrollAccess {
    @Shadow
    @Nullable
    protected Slot focusedSlot;

    @Shadow
    @Final
    protected ScreenHandler handler;

    @Override
    @Nullable
    public Slot himproveme$getFocusedSlot() {
        return this.focusedSlot;
    }

    @Override
    public ScreenHandler himproveme$getHandler() {
        return this.handler;
    }
}
