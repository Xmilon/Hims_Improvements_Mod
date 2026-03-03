package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenTooltipMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void himproveme$setPerkBookButtonTooltip(CallbackInfo ci) {
        InventoryScreen self = (InventoryScreen) (Object) this;

        for (Element child : self.children()) {
            if (!(child instanceof TexturedButtonWidget widget)) {
                continue;
            }
            if (widget.getWidth() == 20 && widget.getHeight() == 18) {
                widget.setTooltip(Tooltip.of(Text.translatable("perk.himproveme.open_book")));
                return;
            }
        }
    }
}
