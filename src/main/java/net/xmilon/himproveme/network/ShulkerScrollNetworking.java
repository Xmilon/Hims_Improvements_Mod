package net.xmilon.himproveme.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.xmilon.himproveme.item.custom.HeldShulkerBoxScreenHandler;
import net.xmilon.himproveme.item.custom.LockableContainerHelper;

public final class ShulkerScrollNetworking {
    private ShulkerScrollNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ShulkerScrollPayload.ID, ShulkerScrollPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ShulkerScrollPayload.ID, (payload, context) ->
                context.server().execute(() -> extractStack(context.player().currentScreenHandler, payload)));
    }

    private static void extractStack(ScreenHandler handler, ShulkerScrollPayload payload) {
        if (handler == null || handler.syncId != payload.syncId() || handler instanceof HeldShulkerBoxScreenHandler) {
            return;
        }

        int direction = Integer.compare(payload.direction(), 0);
        if (direction == 0 || payload.slotId() < 0 || !handler.isValid(payload.slotId())) {
            return;
        }

        Slot slot = handler.getSlot(payload.slotId());
        ItemStack shulkerStack = slot.getStack();
        ItemStack newCursorStack = LockableContainerHelper.extractFromShulkerToCursor(shulkerStack, handler.getCursorStack(), direction);
        if (newCursorStack == null) {
            return;
        }

        handler.setCursorStack(newCursorStack);
        slot.markDirty();
        handler.sendContentUpdates();
    }
}
