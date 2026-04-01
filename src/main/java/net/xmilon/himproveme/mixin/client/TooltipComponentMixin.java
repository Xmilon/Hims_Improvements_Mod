package net.xmilon.himproveme.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.tooltip.TooltipData;
import net.xmilon.himproveme.item.custom.ContainerPreviewTooltipData;
import net.xmilon.himproveme.tooltip.client.ContainerPreviewTooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(TooltipComponent.class)
public interface TooltipComponentMixin {
    @Inject(method = "of(Lnet/minecraft/item/tooltip/TooltipData;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;", at = @At("HEAD"), cancellable = true)
    private static void himproveme$createPreviewComponent(TooltipData data, CallbackInfoReturnable<TooltipComponent> cir) {
        if (data instanceof ContainerPreviewTooltipData previewData) {
            cir.setReturnValue(new ContainerPreviewTooltipComponent(previewData));
        }
    }
}
