package net.xmilon.himproveme.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public final class EnderBundleNetworking {

    private EnderBundleNetworking() {}

    public static void register() {
        PayloadTypeRegistry.playS2C().register(EnderBundleSyncPayload.ID, EnderBundleSyncPayload.CODEC);

        // Sync ender chest to client when they join the world
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                sync(handler.player)
        );
    }

    /**
     * Reads the player's ender chest on the server and sends all 27 slots to the client.
     * Call this whenever the ender chest contents might have changed.
     */
    public static void sync(ServerPlayerEntity player) {
        EnderChestInventory enderInv = player.getEnderChestInventory();
        NbtCompound data = new NbtCompound();
        NbtList itemList = new NbtList();

        for (int i = 0; i < enderInv.size(); i++) {
            ItemStack stack = enderInv.getStack(i);
            if (!stack.isEmpty()) {
                NbtCompound entry = new NbtCompound();
                entry.putByte("Slot", (byte) i);
                entry.put("Item", stack.encode(player.getRegistryManager()));
                itemList.add(entry);
            }
        }

        data.put("Items", itemList);
        ServerPlayNetworking.send(player, new EnderBundleSyncPayload(data));
    }
}