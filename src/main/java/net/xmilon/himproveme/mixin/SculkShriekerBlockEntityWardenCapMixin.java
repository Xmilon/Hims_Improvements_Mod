package net.xmilon.himproveme.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SculkShriekerBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.xmilon.himproveme.world.ancientcity.AncientCityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Prevents shriekers from spawning a second Warden while one is already alive inside the same Ancient City.
 */
@Mixin(SculkShriekerBlockEntity.class)
public abstract class SculkShriekerBlockEntityWardenCapMixin {
    /**
     * Calls the original private Warden spawn attempt when the Ancient City cap does not block it.
     */
    @Invoker("trySpawnWarden")
    protected abstract boolean himproveme$invokeTrySpawnWarden(ServerWorld world);

    /**
     * Injects the Ancient City one-Warden cap into the shrieker warning flow without affecting vanilla spawns elsewhere.
     */
    @Redirect(
            method = "warn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/SculkShriekerBlockEntity;trySpawnWarden(Lnet/minecraft/server/world/ServerWorld;)Z"
            )
    )
    private boolean himproveme$enforceAncientCityWardenCap(SculkShriekerBlockEntity instance, ServerWorld world) {
        if (AncientCityManager.shouldBlockCityWardenSpawn(world, ((BlockEntity) (Object) this).getPos())) {
            return false;
        }

        return himproveme$invokeTrySpawnWarden(world);
    }
}
