package net.xmilon.himproveme.perk;

import net.minecraft.nbt.NbtCompound;

public final class ClientPerkBookState {
    private static PerkBookState state = new PerkBookState();

    private ClientPerkBookState() {
    }

    public static synchronized PerkBookState getSnapshot() {
        return state.copy();
    }

    public static synchronized void setFromNbt(NbtCompound nbt) {
        state = PerkBookState.fromNbt(nbt);
    }
}
