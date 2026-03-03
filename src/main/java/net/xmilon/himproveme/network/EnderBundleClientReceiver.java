package net.xmilon.himproveme.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import java.util.Objects;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class EnderBundleClientReceiver {

    // Cached ender chest contents — updated every time the server sends a sync packet
    private static final List<ItemStack> CACHED_CONTENTS = new ArrayList<>();

    private EnderBundleClientReceiver() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(EnderBundleSyncPayload.ID, (payload, context) -> {
            // Parse the NBT on the client thread
            context.client().execute(() -> {
                CACHED_CONTENTS.clear();

                NbtList itemList = payload.data().getList("Items", NbtElement.COMPOUND_TYPE);
                // Build a slot -> stack map first so we preserve empty slots
                ItemStack[] slots = new ItemStack[27];
                for (int i = 0; i < 27; i++) slots[i] = ItemStack.EMPTY;

                for (int i = 0; i < itemList.size(); i++) {
                    NbtCompound entry = itemList.getCompound(i);
                    int slot = entry.getByte("Slot") & 0xFF;
                    if (slot < 27) {
                        NbtCompound itemNbt = entry.getCompound("Item");
                        // fromNbt returns Optional in 1.21.1
                        ItemStack.fromNbt(Objects.requireNonNull(context.client().player).getRegistryManager(), itemNbt)
                                .ifPresent(stack -> slots[slot] = stack);
                    }
                }

                for (ItemStack stack : slots) {
                    CACHED_CONTENTS.add(stack);
                }
            });
        });
    }

    /**
     * Returns the last known ender chest contents sent by the server.
     * Always up to date — synced on join and on every Ender Bundle open.
     */
    public static List<ItemStack> getCachedContents() {
        return CACHED_CONTENTS;
    }
}