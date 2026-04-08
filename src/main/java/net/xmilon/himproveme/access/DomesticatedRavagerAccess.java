package net.xmilon.himproveme.access;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface DomesticatedRavagerAccess {
    boolean himproveme$isDomesticated();

    void himproveme$setDomesticated(boolean domesticated);

    @Nullable
    UUID himproveme$getOwnerUuid();

    void himproveme$setOwnerUuid(@Nullable UUID ownerUuid);

    boolean himproveme$isOwnedBy(PlayerEntity player);

    SimpleInventory himproveme$getDomesticatedInventory();

    ItemStack himproveme$getStorageAttachment(int slot);

    void himproveme$setStorageAttachment(int slot, ItemStack stack);

    int himproveme$getStorageAttachmentCount();
}
