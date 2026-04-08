package net.xmilon.himproveme.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.xmilon.himproveme.access.LevelingStateHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityLevelingCopyMixin {
    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void himproveme$copyLevelingState(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        LevelingStateHolder self = (LevelingStateHolder) this;
        LevelingStateHolder oldHolder = (LevelingStateHolder) oldPlayer;
        self.himproveme$setLevelingState(oldHolder.himproveme$getLevelingState().copy());
    }
}
