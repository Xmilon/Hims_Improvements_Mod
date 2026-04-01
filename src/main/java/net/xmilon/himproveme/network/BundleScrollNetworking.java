package net.xmilon.himproveme.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.xmilon.himproveme.item.custom.BundleUpgradeHelper;

public final class BundleScrollNetworking {
    public static final int CURSOR_SLOT = -1;

    private BundleScrollNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(BundleScrollPayload.ID, BundleScrollPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(BundleScrollPayload.ID, (payload, context) ->
                context.server().execute(() -> rotateSelection(context.player().currentScreenHandler, payload)));
    }

    private static void rotateSelection(ScreenHandler handler, BundleScrollPayload payload) {
        if (handler == null || handler.syncId != payload.syncId()) {
            return;
        }

        int direction = Integer.compare(payload.direction(), 0);
        if (direction == 0) {
            return;
        }

        if (payload.slotId() == CURSOR_SLOT) {
            ItemStack cursorStack = handler.getCursorStack();
            if (!BundleUpgradeHelper.isBundle(cursorStack) || !BundleUpgradeHelper.rotateSelection(cursorStack, direction)) {
                return;
            }

            handler.setCursorStack(cursorStack);
            handler.sendContentUpdates();
            return;
        }

        if (!handler.isValid(payload.slotId()) || payload.slotId() < 0) {
            return;
        }

        Slot slot = handler.getSlot(payload.slotId());
        ItemStack stack = slot.getStack();
        if (!BundleUpgradeHelper.isBundle(stack) || !BundleUpgradeHelper.rotateSelection(stack, direction)) {
            return;
        }

        slot.markDirty();
        handler.sendContentUpdates();
    }
}
