package net.xmilon.himproveme.mixin;

import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.util.math.MathHelper;
import net.xmilon.himproveme.access.BundleContentsLevelAccess;
import net.xmilon.himproveme.item.custom.BundleUpgradeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BundleContentsComponent.class)
public class BundleContentsComponentMixin implements BundleContentsLevelAccess {
    @Unique
    private int himproveme$bundleLevel = BundleUpgradeHelper.MIN_LEVEL;

    @Override
    public int himproveme$getBundleLevel() {
        return this.himproveme$bundleLevel;
    }

    @Override
    public void himproveme$setBundleLevel(int level) {
        this.himproveme$bundleLevel = MathHelper.clamp(level, BundleUpgradeHelper.MIN_LEVEL, BundleUpgradeHelper.MAX_LEVEL);
    }
}
