package net.xmilon.himproveme.item.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-only helper that reads the local player's ender chest inventory.
 * Only ever called from appendTooltip() which only runs on the client.
 */
@Environment(EnvType.CLIENT)
public class EnderBundleClientHelper {

    public static List<ItemStack> getLocalEnderContents() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.player == null) {
            return null;
        }

        var enderInv = client.player.getEnderChestInventory();
        List<ItemStack> contents = new ArrayList<>();

        for (int i = 0; i < enderInv.size(); i++) {
            contents.add(enderInv.getStack(i));
        }

        return contents;
    }
}