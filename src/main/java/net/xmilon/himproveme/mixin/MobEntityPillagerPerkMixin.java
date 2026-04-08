package net.xmilon.himproveme.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.xmilon.himproveme.access.DomesticatedRavagerAccess;
import net.xmilon.himproveme.perk.DomesticatedRavagerHelper;
import net.xmilon.himproveme.perk.DomesticatedRavagerTrackedData;
import net.xmilon.himproveme.perk.PerkAccess;
import net.xmilon.himproveme.perk.PillagerPerkHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(MobEntity.class)
public abstract class MobEntityPillagerPerkMixin implements DomesticatedRavagerAccess {
    @Unique
    private static final String HIMPROVEME_RAVAGER_DOMESTICATED_KEY = "HimProveMeDomesticatedRavager";
    @Unique
    private static final String HIMPROVEME_RAVAGER_OWNER_KEY = "HimProveMeDomesticatedRavagerOwner";
    @Unique
    private static final String HIMPROVEME_RAVAGER_INVENTORY_KEY = "HimProveMeDomesticatedRavagerInventory";
    @Unique
    private static final String HIMPROVEME_RAVAGER_ATTACHMENT_0_KEY = "HimProveMeDomesticatedRavagerAttachment0";
    @Unique
    private static final String HIMPROVEME_RAVAGER_ATTACHMENT_1_KEY = "HimProveMeDomesticatedRavagerAttachment1";

    @Unique
    private final SimpleInventory himproveme$domesticatedRavagerInventory =
            new SimpleInventory(DomesticatedRavagerHelper.TOTAL_STORAGE_SLOTS);
    @Unique
    private ItemStack himproveme$storageAttachment0 = ItemStack.EMPTY;
    @Unique
    private ItemStack himproveme$storageAttachment1 = ItemStack.EMPTY;
    @Unique
    private boolean himproveme$storageDropped;

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void himproveme$readDomesticatedRavagerData(NbtCompound nbt, CallbackInfo ci) {
        if (!((Object) this instanceof RavagerEntity)) {
            return;
        }

        himproveme$setDomesticated(nbt.getBoolean(HIMPROVEME_RAVAGER_DOMESTICATED_KEY));
        himproveme$setOwnerUuid(nbt.containsUuid(HIMPROVEME_RAVAGER_OWNER_KEY) ? nbt.getUuid(HIMPROVEME_RAVAGER_OWNER_KEY) : null);
        himproveme$storageAttachment0 = DomesticatedRavagerHelper.readStack(((MobEntity) (Object) this).getRegistryManager(), nbt, HIMPROVEME_RAVAGER_ATTACHMENT_0_KEY);
        himproveme$storageAttachment1 = DomesticatedRavagerHelper.readStack(((MobEntity) (Object) this).getRegistryManager(), nbt, HIMPROVEME_RAVAGER_ATTACHMENT_1_KEY);

        if (nbt.contains(HIMPROVEME_RAVAGER_INVENTORY_KEY, NbtElement.LIST_TYPE)) {
            himproveme$domesticatedRavagerInventory.readNbtList(
                    nbt.getList(HIMPROVEME_RAVAGER_INVENTORY_KEY, NbtElement.COMPOUND_TYPE),
                    ((MobEntity) (Object) this).getRegistryManager()
            );
        } else {
            himproveme$domesticatedRavagerInventory.clear();
        }
        himproveme$storageDropped = false;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void himproveme$writeDomesticatedRavagerData(NbtCompound nbt, CallbackInfo ci) {
        if (!((Object) this instanceof RavagerEntity)) {
            return;
        }

        nbt.putBoolean(HIMPROVEME_RAVAGER_DOMESTICATED_KEY, himproveme$isDomesticated());
        if (himproveme$getOwnerUuid() != null) {
            nbt.putUuid(HIMPROVEME_RAVAGER_OWNER_KEY, himproveme$getOwnerUuid());
        }
        nbt.put(HIMPROVEME_RAVAGER_INVENTORY_KEY, himproveme$domesticatedRavagerInventory.toNbtList(((MobEntity) (Object) this).getRegistryManager()));
        if (!himproveme$storageAttachment0.isEmpty()) {
            nbt.put(HIMPROVEME_RAVAGER_ATTACHMENT_0_KEY, himproveme$storageAttachment0.encodeAllowEmpty(((MobEntity) (Object) this).getRegistryManager()));
        }
        if (!himproveme$storageAttachment1.isEmpty()) {
            nbt.put(HIMPROVEME_RAVAGER_ATTACHMENT_1_KEY, himproveme$storageAttachment1.encodeAllowEmpty(((MobEntity) (Object) this).getRegistryManager()));
        }
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void himproveme$handleDomesticatedRavagerInteractions(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        MobEntity self = (MobEntity) (Object) this;
        if (!(self instanceof RavagerEntity ravager)) {
            return;
        }

        ItemStack stack = player.getStackInHand(hand);
        boolean owner = himproveme$isOwnedBy(player);
        boolean canTame = !himproveme$isDomesticated()
                && PerkAccess.hasDomesticatedRavanger(player)
                && DomesticatedRavagerHelper.isTamingFood(stack);

        if (self.getWorld().isClient) {
            if (canTame
                    || owner && (player.shouldCancelInteraction()
                    || DomesticatedRavagerHelper.isTamingFood(stack)
                    || DomesticatedRavagerHelper.isStorageAttachment(stack)
                    || !ravager.hasPassengers())) {
                cir.setReturnValue(ActionResult.SUCCESS);
            }
            return;
        }

        if (!(self.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        if (canTame) {
            DomesticatedRavagerHelper.tame(serverWorld, ravager, this, player);
            stack.decrementUnlessCreative(1, player);
            ravager.playSound(SoundEvents.ENTITY_RAVAGER_AMBIENT, 1.0f, 1.15f);
            player.sendMessage(Text.translatable("ability.himproveme.ravager.tamed"), true);
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (!himproveme$isDomesticated() || !owner) {
            return;
        }

        if (DomesticatedRavagerHelper.isTamingFood(stack) && ravager.getHealth() < ravager.getMaxHealth()) {
            ravager.heal(8.0f);
            stack.decrementUnlessCreative(1, player);
            ravager.playSound(SoundEvents.ENTITY_RAVAGER_AMBIENT, 0.85f, 1.25f);
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (DomesticatedRavagerHelper.isStorageAttachment(stack)) {
            if (DomesticatedRavagerHelper.tryAddStorage(this, stack)) {
                stack.decrementUnlessCreative(1, player);
                ravager.playSound(SoundEvents.BLOCK_CHEST_OPEN, 0.8f, 1.0f);
                player.sendMessage(Text.translatable("ability.himproveme.ravager.container_added"), true);
            } else {
                player.sendMessage(Text.translatable("ability.himproveme.ravager.storage_full"), true);
            }
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (player.shouldCancelInteraction()) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                if (DomesticatedRavagerHelper.hasStorage(this)) {
                    DomesticatedRavagerHelper.openStorage(serverPlayer, ravager, this);
                } else {
                    player.sendMessage(Text.translatable("ability.himproveme.ravager.no_storage"), true);
                }
            }
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (!ravager.hasPassengers() && player.startRiding(ravager)) {
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
    private void himproveme$allowOwnerToControlDomesticatedRavager(CallbackInfoReturnable<LivingEntity> cir) {
        MobEntity self = (MobEntity) (Object) this;
        if (!(self instanceof RavagerEntity) || !himproveme$isDomesticated()) {
            return;
        }

        Entity firstPassenger = self.getFirstPassenger();
        if (firstPassenger instanceof PlayerEntity player && himproveme$isOwnedBy(player)) {
            cir.setReturnValue(player);
        }
    }

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void himproveme$keepRaidersNeutralUntilProvoked(@Nullable LivingEntity target, CallbackInfo ci) {
        MobEntity self = (MobEntity) (Object) this;
        if (self instanceof RaiderEntity raider && target != null && PillagerPerkHelper.shouldRaidersIgnore(raider, target)) {
            ci.cancel();
            return;
        }

        if (self instanceof RavagerEntity && himproveme$isDomesticated() && DomesticatedRavagerHelper.isInvalidTarget(this, target)) {
            ci.cancel();
        }
    }

    @Inject(method = "dropLoot", at = @At("TAIL"))
    private void himproveme$dropPillagerPerkLoot(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        MobEntity self = (MobEntity) (Object) this;
        if (self instanceof IllagerEntity illager) {
            PillagerPerkHelper.dropLuckyTotems(illager, source);
        }

        if (self instanceof RavagerEntity ravager
                && self.getWorld() instanceof ServerWorld serverWorld
                && himproveme$isDomesticated()
                && !himproveme$storageDropped) {
            himproveme$storageDropped = true;
            DomesticatedRavagerHelper.dropStorage(serverWorld, ravager, this);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void himproveme$updateDomesticatedRavagerAssist(CallbackInfo ci) {
        MobEntity self = (MobEntity) (Object) this;
        if (!(self instanceof RavagerEntity ravager) || !himproveme$isDomesticated()) {
            return;
        }

        LivingEntity target = ravager.getTarget();
        if (DomesticatedRavagerHelper.isInvalidTarget(this, target)) {
            ravager.setTarget(null);
            ravager.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
        }

        DomesticatedRavagerHelper.updateAssistTarget(ravager, this);
    }

    @Override
    public boolean himproveme$isDomesticated() {
        return ((MobEntity) (Object) this) instanceof RavagerEntity
                && ((MobEntity) (Object) this).getDataTracker().get(DomesticatedRavagerTrackedData.DOMESTICATED);
    }

    @Override
    public void himproveme$setDomesticated(boolean domesticated) {
        if ((MobEntity) (Object) this instanceof RavagerEntity) {
            ((MobEntity) (Object) this).getDataTracker().set(DomesticatedRavagerTrackedData.DOMESTICATED, domesticated);
        }
    }

    @Override
    public @Nullable UUID himproveme$getOwnerUuid() {
        if (!((MobEntity) (Object) this instanceof RavagerEntity)) {
            return null;
        }
        return ((MobEntity) (Object) this).getDataTracker().get(DomesticatedRavagerTrackedData.OWNER_UUID).orElse(null);
    }

    @Override
    public void himproveme$setOwnerUuid(@Nullable UUID ownerUuid) {
        if ((MobEntity) (Object) this instanceof RavagerEntity) {
            ((MobEntity) (Object) this).getDataTracker().set(DomesticatedRavagerTrackedData.OWNER_UUID, Optional.ofNullable(ownerUuid));
        }
    }

    @Override
    public boolean himproveme$isOwnedBy(PlayerEntity player) {
        UUID ownerUuid = himproveme$getOwnerUuid();
        return ownerUuid != null && ownerUuid.equals(player.getUuid());
    }

    @Override
    public SimpleInventory himproveme$getDomesticatedInventory() {
        return himproveme$domesticatedRavagerInventory;
    }

    @Override
    public ItemStack himproveme$getStorageAttachment(int slot) {
        return switch (slot) {
            case 0 -> himproveme$storageAttachment0;
            case 1 -> himproveme$storageAttachment1;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public void himproveme$setStorageAttachment(int slot, ItemStack stack) {
        switch (slot) {
            case 0 -> himproveme$storageAttachment0 = stack;
            case 1 -> himproveme$storageAttachment1 = stack;
            default -> {
            }
        }
    }

    @Override
    public int himproveme$getStorageAttachmentCount() {
        int count = 0;
        if (!himproveme$storageAttachment0.isEmpty()) {
            count++;
        }
        if (!himproveme$storageAttachment1.isEmpty()) {
            count++;
        }
        return count;
    }
}
