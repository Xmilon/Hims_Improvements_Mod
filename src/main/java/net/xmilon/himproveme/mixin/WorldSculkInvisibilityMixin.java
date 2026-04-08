package net.xmilon.himproveme.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.xmilon.himproveme.perk.PerkAccess;
import net.xmilon.himproveme.perk.SculkInvisibilityContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class WorldSculkInvisibilityMixin {
    @Inject(
            method = "emitGameEvent(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/world/event/GameEvent$Emitter;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void himproveme$suppressSculkVibrationsAtVec(
            RegistryEntry<GameEvent> event,
            Vec3d emitterPos,
            GameEvent.Emitter emitter,
            CallbackInfo ci
    ) {
        if (himproveme$shouldSuppress(emitter)) {
            ci.cancel();
        }
    }

    private boolean himproveme$shouldSuppress(GameEvent.Emitter emitter) {
        Entity source = emitter.sourceEntity();
        if (source instanceof PlayerEntity player && PerkAccess.hasSculkInvisibility(player)) {
            return true;
        }

        if (source instanceof Ownable ownable
                && ownable.getOwner() instanceof PlayerEntity owner
                && PerkAccess.hasSculkInvisibility(owner)) {
            return true;
        }

        return SculkInvisibilityContext.isActive();
    }
}
