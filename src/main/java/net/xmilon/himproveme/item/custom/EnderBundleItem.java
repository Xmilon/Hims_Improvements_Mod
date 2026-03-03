package net.xmilon.himproveme.item.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.xmilon.himproveme.network.EnderBundleClientReceiver;
import net.xmilon.himproveme.network.EnderBundleNetworking;

import java.util.List;

public class EnderBundleItem extends Item {

    private static final int USE_COOLDOWN = 20; // 1 second

    public EnderBundleItem(Settings settings) {
        super(settings);
    }

    // -------------------------------------------------------
    // RIGHT CLICK — open ender chest as a GUI, then re-sync
    // -------------------------------------------------------
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient()) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) user;
            EnderChestInventory enderInv = serverPlayer.getEnderChestInventory();

            float bundlePitch = 0.9f + world.random.nextFloat() * 0.2f;
            float chestPitch = 1.2f + world.random.nextFloat() * 0.4f;
            playOpeningSounds(world, user.getBlockPos(), bundlePitch, chestPitch);

            NamedScreenHandlerFactory factory = new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInv, player) ->
                            GenericContainerScreenHandler.createGeneric9x3(syncId, playerInv, enderInv),
                    Text.literal("Ender Bundle").formatted(Formatting.DARK_PURPLE, Formatting.BOLD)
            );

            EnderBundleSoundTracker.markPendingClose(serverPlayer, chestPitch);
            user.openHandledScreen(factory);
            user.getItemCooldownManager().set(this, USE_COOLDOWN);

            // Re-sync after opening so tooltip stays fresh after changes
            EnderBundleNetworking.sync(serverPlayer);
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    private void playOpeningSounds(World world, BlockPos pos, float bundlePitch, float chestPitch) {
        world.playSound(
                null,
                pos,
                SoundEvents.ITEM_BUNDLE_INSERT,
                SoundCategory.PLAYERS,
                0.8f,
                bundlePitch
        );
        world.playSound(
                null,
                pos,
                SoundEvents.BLOCK_ENDER_CHEST_OPEN,
                SoundCategory.PLAYERS,
                1f,
                chestPitch
        );
    }

    // -------------------------------------------------------
    // TOOLTIP — shift to preview ender chest contents
    // -------------------------------------------------------
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        if (Screen.hasShiftDown()) {
            appendEnderChestContents(tooltip);
        } else {
            tooltip.add(Text.empty());
            tooltip.add(
                    Text.literal("▶ ").formatted(Formatting.DARK_PURPLE)
                            .append(Text.literal("Hold Shift to preview contents").formatted(Formatting.GRAY, Formatting.ITALIC))
            );
        }
    }

    // -------------------------------------------------------
    // Helper — read from the client-side cache
    // -------------------------------------------------------
    private void appendEnderChestContents(List<Text> tooltip) {
        List<ItemStack> contents = EnderBundleClientReceiver.getCachedContents();

        if (contents.isEmpty()) {
            tooltip.add(Text.empty());
            tooltip.add(Text.literal("── Ender Chest ──").formatted(Formatting.DARK_PURPLE));
            tooltip.add(Text.literal("  Empty").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            return;
        }

        long filledSlots = contents.stream().filter(s -> !s.isEmpty()).count();

        tooltip.add(Text.empty());
        tooltip.add(Text.literal("── Ender Chest ──").formatted(Formatting.DARK_PURPLE));

        if (filledSlots == 0) {
            tooltip.add(Text.literal("  Empty").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            return;
        }

        int shown = 0;
        for (ItemStack entry : contents) {
            if (entry.isEmpty()) continue;
            if (shown >= 5) {
                tooltip.add(Text.literal("  + " + (filledSlots - 5) + " more...").formatted(Formatting.DARK_GRAY));
                break;
            }
            tooltip.add(
                    Text.literal("  • ").formatted(Formatting.DARK_PURPLE)
                            .append(entry.getName().copy().formatted(Formatting.WHITE))
                            .append(Text.literal(" ×" + entry.getCount()).formatted(Formatting.GRAY))
            );
            shown++;
        }
    }
}
