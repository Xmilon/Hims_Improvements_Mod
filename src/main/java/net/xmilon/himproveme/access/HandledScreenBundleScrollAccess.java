package net.xmilon.himproveme.access;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

public interface HandledScreenBundleScrollAccess {
    @Nullable
    Slot himproveme$getFocusedSlot();

    ScreenHandler himproveme$getHandler();
}
