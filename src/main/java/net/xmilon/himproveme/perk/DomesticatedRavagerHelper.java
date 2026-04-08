package net.xmilon.himproveme.perk;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.xmilon.himproveme.access.DomesticatedRavagerAccess;
import net.xmilon.himproveme.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class DomesticatedRavagerHelper {
    public static final int MAX_STORAGE_ATTACHMENTS = 2;
    public static final int STORAGE_SLOTS_PER_ATTACHMENT = 27;
    public static final int TOTAL_STORAGE_SLOTS = MAX_STORAGE_ATTACHMENTS * STORAGE_SLOTS_PER_ATTACHMENT;

    private DomesticatedRavagerHelper() {
    }

    public static boolean isTamingFood(ItemStack stack) {
        return stack.isIn(ModTags.Items.RAVAGER_TAMING_FOOD);
    }

    public static boolean isStorageAttachment(ItemStack stack) {
        return stack.isIn(ModTags.Items.RAVAGER_STORAGE_ATTACHMENTS);
    }

    public static void tame(ServerWorld world, RavagerEntity ravager, DomesticatedRavagerAccess access, PlayerEntity player) {
        access.himproveme$setDomesticated(true);
        access.himproveme$setOwnerUuid(player.getUuid());
        ravager.setPersistent();
        ravager.setTarget(null);
        ravager.setRaid(null);
        ravager.setAbleToJoinRaid(false);
        world.sendEntityStatus(ravager, (byte) 7);
    }

    public static boolean isInvalidTarget(DomesticatedRavagerAccess access, @Nullable LivingEntity target) {
        if (target == null) {
            return false;
        }

        if (target == (Object) access) {
            return true;
        }

        UUID ownerUuid = access.himproveme$getOwnerUuid();
        if (ownerUuid != null && ownerUuid.equals(target.getUuid())) {
            return true;
        }

        if (target instanceof RavagerEntity otherRavager) {
            DomesticatedRavagerAccess otherAccess = (DomesticatedRavagerAccess) otherRavager;
            return otherAccess.himproveme$isDomesticated() && ownerUuid != null && ownerUuid.equals(otherAccess.himproveme$getOwnerUuid());
        }

        return false;
    }

    public static boolean tryAddStorage(DomesticatedRavagerAccess access, ItemStack stack) {
        if (!isStorageAttachment(stack)) {
            return false;
        }

        for (int i = 0; i < MAX_STORAGE_ATTACHMENTS; i++) {
            if (access.himproveme$getStorageAttachment(i).isEmpty()) {
                access.himproveme$setStorageAttachment(i, stack.copyWithCount(1));
                return true;
            }
        }
        return false;
    }

    public static void openStorage(ServerPlayerEntity player, RavagerEntity ravager, DomesticatedRavagerAccess access) {
        int rows = getStorageRows(access);
        if (rows <= 0) {
            return;
        }

        Inventory storageView = new RavagerStorageView(ravager, access.himproveme$getDomesticatedInventory(), rows * 9);
        NamedScreenHandlerFactory factory = new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, openedPlayer) -> createScreenHandler(syncId, playerInventory, storageView, rows),
                ravager.getDisplayName()
        );
        player.openHandledScreen(factory);
    }

    public static void dropStorage(ServerWorld world, RavagerEntity ravager, DomesticatedRavagerAccess access) {
        SimpleInventory inventory = access.himproveme$getDomesticatedInventory();
        int storageSize = getStorageSlotCount(access);
        for (int i = 0; i < storageSize; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                ravager.dropStack(stack.copy());
                inventory.setStack(i, ItemStack.EMPTY);
            }
        }

        for (int i = 0; i < MAX_STORAGE_ATTACHMENTS; i++) {
            ItemStack attachment = access.himproveme$getStorageAttachment(i);
            if (!attachment.isEmpty()) {
                ravager.dropStack(attachment.copy());
                access.himproveme$setStorageAttachment(i, ItemStack.EMPTY);
            }
        }
    }

    public static void updateAssistTarget(RavagerEntity ravager, DomesticatedRavagerAccess access) {
        if (!access.himproveme$isDomesticated() || !(ravager.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        ServerPlayerEntity owner = getOwner(serverWorld, access);
        if (owner == null || !owner.isAlive()) {
            return;
        }

        LivingEntity currentTarget = ravager.getTarget();
        if (currentTarget != null) {
            if (currentTarget.isAlive() && !isInvalidTarget(access, currentTarget)) {
                return;
            }
            ravager.setTarget(null);
        }

        LivingEntity attacker = owner.getAttacker();
        if (isValidAssistTarget(owner, attacker)) {
            ravager.setTarget(attacker);
            return;
        }

        LivingEntity attacking = owner.getAttacking();
        if (isValidAssistTarget(owner, attacking)) {
            ravager.setTarget(attacking);
        }
    }

    public static boolean hasStorage(DomesticatedRavagerAccess access) {
        return access.himproveme$getStorageAttachmentCount() > 0;
    }

    public static int getStorageSlotCount(DomesticatedRavagerAccess access) {
        return access.himproveme$getStorageAttachmentCount() * STORAGE_SLOTS_PER_ATTACHMENT;
    }

    public static int getStorageRows(DomesticatedRavagerAccess access) {
        return access.himproveme$getStorageAttachmentCount() * 3;
    }

    @Nullable
    public static ServerPlayerEntity getOwner(ServerWorld world, DomesticatedRavagerAccess access) {
        UUID ownerUuid = access.himproveme$getOwnerUuid();
        if (ownerUuid == null) {
            return null;
        }
        return world.getServer().getPlayerManager().getPlayer(ownerUuid);
    }

    public static ItemStack readStack(RegistryWrapper.WrapperLookup registryLookup, net.minecraft.nbt.NbtCompound nbt, String key) {
        if (!nbt.contains(key)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.fromNbt(registryLookup, nbt.get(key)).orElse(ItemStack.EMPTY);
    }

    private static boolean isValidAssistTarget(PlayerEntity owner, @Nullable LivingEntity target) {
        return target != null
                && target.isAlive()
                && target != owner
                && !owner.isTeammate(target);
    }

    private static ScreenHandler createScreenHandler(int syncId, net.minecraft.entity.player.PlayerInventory playerInventory, Inventory inventory, int rows) {
        return switch (MathHelper.clamp(rows, 3, 6)) {
            case 3 -> GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, inventory);
            case 6 -> GenericContainerScreenHandler.createGeneric9x6(syncId, playerInventory, inventory);
            default -> GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, inventory);
        };
    }

    private static final class RavagerStorageView implements Inventory {
        private final RavagerEntity ravager;
        private final SimpleInventory backing;
        private final int size;

        private RavagerStorageView(RavagerEntity ravager, SimpleInventory backing, int size) {
            this.ravager = ravager;
            this.backing = backing;
            this.size = size;
        }

        @Override
        public int size() {
            return this.size;
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < this.size; i++) {
                if (!this.backing.getStack(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getStack(int slot) {
            return this.backing.getStack(slot);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            return this.backing.removeStack(slot, amount);
        }

        @Override
        public ItemStack removeStack(int slot) {
            return this.backing.removeStack(slot);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            this.backing.setStack(slot, stack);
        }

        @Override
        public void markDirty() {
            this.backing.markDirty();
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return this.ravager.isAlive() && this.ravager.squaredDistanceTo(player) <= 64.0;
        }

        @Override
        public void clear() {
            for (int i = 0; i < this.size; i++) {
                this.backing.setStack(i, ItemStack.EMPTY);
            }
        }
    }
}
