package net.xmilon.himproveme.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PlayerEntity.class)
public abstract class PlayerExperienceMixin {
    private static final int CONSTANT_XP_PER_LEVEL = 18; // matches the xp needed between level 3 and 4 in vanilla

    @Overwrite
    public int getNextLevelExperience() {
        return CONSTANT_XP_PER_LEVEL;
    }
}
