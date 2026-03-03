package net.xmilon.himproveme.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.xmilon.himproveme.item.custom.SpectralProjectileAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin implements SpectralProjectileAccess {
    @Shadow
    protected boolean inGround;

    @Unique
    private boolean himproveme$spectralProjectile;
    @Unique
    private double himproveme$startX;
    @Unique
    private double himproveme$startY;
    @Unique
    private double himproveme$startZ;
    @Unique
    private double himproveme$maxDistance;

    @Override
    public void himproveme$markSpectral(double startX, double startY, double startZ, double maxDistance) {
        this.himproveme$spectralProjectile = true;
        this.himproveme$startX = startX;
        this.himproveme$startY = startY;
        this.himproveme$startZ = startZ;
        this.himproveme$maxDistance = maxDistance;
    }

    @Override
    public boolean himproveme$isSpectral() {
        return this.himproveme$spectralProjectile;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void himproveme$applySpectralArrowBehavior(CallbackInfo ci) {
        if (!this.himproveme$spectralProjectile) {
            return;
        }

        PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;
        this.inGround = false;
        if (self.squaredDistanceTo(this.himproveme$startX, this.himproveme$startY, this.himproveme$startZ)
                > this.himproveme$maxDistance * this.himproveme$maxDistance) {
            self.discard();
        }
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;raycast(Lnet/minecraft/world/RaycastContext;)Lnet/minecraft/util/hit/BlockHitResult;"
            )
    )
    private BlockHitResult himproveme$ignoreBlockRaycastForSpectral(World world, RaycastContext context) {
        if (!this.himproveme$spectralProjectile) {
            return world.raycast(context);
        }

        return BlockHitResult.createMissed(
                context.getEnd(),
                Direction.getFacing(context.getEnd().x - context.getStart().x, context.getEnd().y - context.getStart().y, context.getEnd().z - context.getStart().z),
                BlockPos.ofFloored(context.getEnd())
        );
    }

    @Inject(method = "onBlockHit", at = @At("HEAD"), cancellable = true)
    private void himproveme$passThroughBlocks(BlockHitResult blockHitResult, CallbackInfo ci) {
        if (!this.himproveme$spectralProjectile) {
            return;
        }

        ci.cancel();
    }

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    private void himproveme$playOwnerHitFeedback(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (!this.himproveme$spectralProjectile) {
            return;
        }

        if (!(entityHitResult.getEntity() instanceof LivingEntity)) {
            return;
        }

        PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;
        if (!(self.getOwner() instanceof ServerPlayerEntity ownerPlayer)) {
            return;
        }

        ownerPlayer.playSoundToPlayer(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.7f, 1.15f);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void himproveme$writeSpectralData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("HimProveMeSpectralProjectile", this.himproveme$spectralProjectile);
        if (!this.himproveme$spectralProjectile) {
            return;
        }

        nbt.putDouble("HimProveMeSpectralStartX", this.himproveme$startX);
        nbt.putDouble("HimProveMeSpectralStartY", this.himproveme$startY);
        nbt.putDouble("HimProveMeSpectralStartZ", this.himproveme$startZ);
        nbt.putDouble("HimProveMeSpectralMaxDistance", this.himproveme$maxDistance);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void himproveme$readSpectralData(NbtCompound nbt, CallbackInfo ci) {
        this.himproveme$spectralProjectile = nbt.getBoolean("HimProveMeSpectralProjectile");
        if (!this.himproveme$spectralProjectile) {
            return;
        }

        this.himproveme$startX = nbt.getDouble("HimProveMeSpectralStartX");
        this.himproveme$startY = nbt.getDouble("HimProveMeSpectralStartY");
        this.himproveme$startZ = nbt.getDouble("HimProveMeSpectralStartZ");
        this.himproveme$maxDistance = nbt.getDouble("HimProveMeSpectralMaxDistance");
    }
}
