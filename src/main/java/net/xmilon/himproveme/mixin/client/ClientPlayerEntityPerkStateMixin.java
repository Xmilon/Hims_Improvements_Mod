package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.xmilon.himproveme.perk.ClientPerkBookState;
import net.xmilon.himproveme.perk.PerkBookStateHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityPerkStateMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void himproveme$syncPerkState(CallbackInfo ci) {
        if ((Object) this instanceof PerkBookStateHolder holder) {
            holder.himproveme$setPerkBookState(ClientPerkBookState.getSnapshot());
        }
    }
}
