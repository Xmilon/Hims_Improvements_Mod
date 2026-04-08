package net.xmilon.himproveme.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.xmilon.himproveme.item.custom.BlowgunProjectileAccess;
import net.xmilon.himproveme.item.custom.SpectralProjectileAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin implements SpectralProjectileAccess, BlowgunProjectileAccess {
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
    @Unique
    private boolean himproveme$blowgunProjectile;
    @Unique
    private double himproveme$blowgunStartX;
    @Unique
    private double himproveme$blowgunStartY;
    @Unique
    private double himproveme$blowgunStartZ;
    @Unique
    private double himproveme$blowgunStraightDistance;
    @Unique
    private double himproveme$blowgunMaxDistance;

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

    @Override
    public void himproveme$markBlowgun(double startX, double startY, double startZ, double straightDistance, double maxDistance) {
        this.himproveme$blowgunProjectile = true;
        this.himproveme$blowgunStartX = startX;
        this.himproveme$blowgunStartY = startY;
        this.himproveme$blowgunStartZ = startZ;
        this.himproveme$blowgunStraightDistance = straightDistance;
        this.himproveme$blowgunMaxDistance = maxDistance;
    }

    @Override
    public boolean himproveme$isBlowgunProjectile() {
        return this.himproveme$blowgunProjectile;
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

    @Inject(method = "tick", at = @At("TAIL"))
    private void himproveme$applyBlowgunArrowBehavior(CallbackInfo ci) {
        if (!this.himproveme$blowgunProjectile) {
            return;
        }

        PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;
        if (this.inGround) {
            return;
        }

        double squaredDistance = self.squaredDistanceTo(
                this.himproveme$blowgunStartX,
                this.himproveme$blowgunStartY,
                this.himproveme$blowgunStartZ
        );
        if (squaredDistance > this.himproveme$blowgunMaxDistance * this.himproveme$blowgunMaxDistance) {
            self.discard();
            return;
        }

        Vec3d velocity = self.getVelocity();
        if (squaredDistance <= this.himproveme$blowgunStraightDistance * this.himproveme$blowgunStraightDistance) {
            self.setVelocity(
                    velocity.x * 0.997D,
                    Math.max(-0.018D, velocity.y + 0.028D),
                    velocity.z * 0.997D
            );
        } else {
            self.setVelocity(
                    velocity.x * 0.915D,
                    velocity.y - 0.095D,
                    velocity.z * 0.915D
            );
        }

        if (self.getWorld() instanceof ServerWorld serverWorld && self.age % 4 == 0) {
            serverWorld.spawnParticles(ParticleTypes.POOF, self.getX(), self.getY(), self.getZ(), 1, 0.01D, 0.01D, 0.01D, 0.001D);
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
        nbt.putBoolean("HimProveMeBlowgunProjectile", this.himproveme$blowgunProjectile);

        if (this.himproveme$spectralProjectile) {
            nbt.putDouble("HimProveMeSpectralStartX", this.himproveme$startX);
            nbt.putDouble("HimProveMeSpectralStartY", this.himproveme$startY);
            nbt.putDouble("HimProveMeSpectralStartZ", this.himproveme$startZ);
            nbt.putDouble("HimProveMeSpectralMaxDistance", this.himproveme$maxDistance);
        }

        if (!this.himproveme$blowgunProjectile) {
            return;
        }

        nbt.putDouble("HimProveMeBlowgunStartX", this.himproveme$blowgunStartX);
        nbt.putDouble("HimProveMeBlowgunStartY", this.himproveme$blowgunStartY);
        nbt.putDouble("HimProveMeBlowgunStartZ", this.himproveme$blowgunStartZ);
        nbt.putDouble("HimProveMeBlowgunStraightDistance", this.himproveme$blowgunStraightDistance);
        nbt.putDouble("HimProveMeBlowgunMaxDistance", this.himproveme$blowgunMaxDistance);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void himproveme$readSpectralData(NbtCompound nbt, CallbackInfo ci) {
        this.himproveme$spectralProjectile = nbt.getBoolean("HimProveMeSpectralProjectile");
        if (this.himproveme$spectralProjectile) {
            this.himproveme$startX = nbt.getDouble("HimProveMeSpectralStartX");
            this.himproveme$startY = nbt.getDouble("HimProveMeSpectralStartY");
            this.himproveme$startZ = nbt.getDouble("HimProveMeSpectralStartZ");
            this.himproveme$maxDistance = nbt.getDouble("HimProveMeSpectralMaxDistance");
        }

        this.himproveme$blowgunProjectile = nbt.getBoolean("HimProveMeBlowgunProjectile");
        if (!this.himproveme$blowgunProjectile) {
            return;
        }

        this.himproveme$blowgunStartX = nbt.getDouble("HimProveMeBlowgunStartX");
        this.himproveme$blowgunStartY = nbt.getDouble("HimProveMeBlowgunStartY");
        this.himproveme$blowgunStartZ = nbt.getDouble("HimProveMeBlowgunStartZ");
        this.himproveme$blowgunStraightDistance = nbt.getDouble("HimProveMeBlowgunStraightDistance");
        this.himproveme$blowgunMaxDistance = nbt.getDouble("HimProveMeBlowgunMaxDistance");
    }
}
