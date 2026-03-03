package net.xmilon.himproveme.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.xmilon.himproveme.perk.PerkBookStateHolder;
import net.xmilon.himproveme.util.KeepXpManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityPerkCopyMixin {
    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void himproveme$copyPerkState(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        PerkBookStateHolder self = (PerkBookStateHolder) this;
        PerkBookStateHolder oldHolder = (PerkBookStateHolder) oldPlayer;
        self.himproveme$setPerkBookState(oldHolder.himproveme$getPerkBookState().copy());
        KeepXpManager.apply((ServerPlayerEntity) (Object) this, oldPlayer, alive);
    }
}
