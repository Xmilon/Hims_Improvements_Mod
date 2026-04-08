package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.xmilon.himproveme.access.LevelingStateHolder;
import net.xmilon.himproveme.leveling.ClientLevelingState;
import net.xmilon.himproveme.leveling.LevelingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityLevelingStateMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void himproveme$syncLevelingState(CallbackInfo ci) {
        if (!((Object) this instanceof LevelingStateHolder holder)) {
            return;
        }

        ClientLevelingState.Snapshot snapshot = ClientLevelingState.getSnapshot();
        LevelingState state = new LevelingState();
        state.setLevel(snapshot.level());
        state.setCurrentLevelXp(snapshot.currentLevelXp());
        state.setTotalLevelingXp(snapshot.totalLevelingXp());
        state.setTotalPlayTicks(snapshot.totalPlayTicks());
        holder.himproveme$setLevelingState(state);
    }
}
