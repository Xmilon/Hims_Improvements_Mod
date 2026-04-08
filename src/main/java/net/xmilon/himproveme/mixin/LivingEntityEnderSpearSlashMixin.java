package net.xmilon.himproveme.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.xmilon.himproveme.access.EnderSpearSlashAccess;
import net.xmilon.himproveme.combat.EnderSpearSlashHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityEnderSpearSlashMixin implements EnderSpearSlashAccess {
    @Unique
    private int himproveme$mainHandEnderSpearSlashAge = -1;
    @Unique
    private int himproveme$offHandEnderSpearSlashAge = -1;

    @Inject(method = "tick", at = @At("TAIL"))
    private void himproveme$tickEnderSpearSlashAnimation(CallbackInfo ci) {
        this.himproveme$mainHandEnderSpearSlashAge = this.himproveme$tickSlashAge(this.himproveme$mainHandEnderSpearSlashAge);
        this.himproveme$offHandEnderSpearSlashAge = this.himproveme$tickSlashAge(this.himproveme$offHandEnderSpearSlashAge);
    }

    @Inject(method = "handleStatus", at = @At("HEAD"))
    private void himproveme$startEnderSpearSlashFromStatus(byte status, CallbackInfo ci) {
        if (status == EnderSpearSlashHelper.MAIN_HAND_STATUS) {
            this.himproveme$startEnderSpearSlash(Hand.MAIN_HAND);
        } else if (status == EnderSpearSlashHelper.OFF_HAND_STATUS) {
            this.himproveme$startEnderSpearSlash(Hand.OFF_HAND);
        }
    }

    @Override
    public void himproveme$startEnderSpearSlash(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            if (this.himproveme$mainHandEnderSpearSlashAge >= 0 && this.himproveme$mainHandEnderSpearSlashAge < 3) {
                return;
            }
            this.himproveme$mainHandEnderSpearSlashAge = 0;
            return;
        }

        if (this.himproveme$offHandEnderSpearSlashAge >= 0 && this.himproveme$offHandEnderSpearSlashAge < 3) {
            return;
        }
        this.himproveme$offHandEnderSpearSlashAge = 0;
    }

    @Override
    public boolean himproveme$isEnderSpearSlashActive(Hand hand) {
        return this.himproveme$getSlashAge(hand) >= 0;
    }

    @Override
    public float himproveme$getEnderSpearSlashProgress(Hand hand, float tickDelta) {
        int age = this.himproveme$getSlashAge(hand);
        if (age < 0) {
            return 0.0F;
        }
        return Math.min((age + tickDelta) / (float) EnderSpearSlashHelper.ANIMATION_TICKS, 1.0F);
    }

    @Unique
    private int himproveme$tickSlashAge(int age) {
        if (age < 0) {
            return -1;
        }

        int nextAge = age + 1;
        return nextAge >= EnderSpearSlashHelper.ANIMATION_TICKS ? -1 : nextAge;
    }

    @Unique
    private int himproveme$getSlashAge(Hand hand) {
        return hand == Hand.MAIN_HAND ? this.himproveme$mainHandEnderSpearSlashAge : this.himproveme$offHandEnderSpearSlashAge;
    }
}
