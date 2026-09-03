package net.xmilon.himproveme.mixin;

import net.minecraft.entity.player.HungerManager;
import net.xmilon.himproveme.access.HungerManagerAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HungerManager.class)
public interface HungerManagerSaturationMixin extends HungerManagerAccess {
    @Accessor("saturationLevel")
    void himproveme$setSaturationLevel(float saturation);
}
