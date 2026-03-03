package net.xmilon.himproveme.perk;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PerkInstanceState {
    private static final String NAME_KEY = "name";
    private static final String PERKS_KEY = "perks";
    private static final String PERK_ID_KEY = "id";
    private static final String LEVEL_KEY = "level";

    private final String name;
    private final Map<Identifier, Integer> levelsByPerk = new HashMap<>();

    public PerkInstanceState(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public int getLevel(Identifier perkId) {
        return levelsByPerk.getOrDefault(perkId, 0);
    }

    public void setLevel(Identifier perkId, int level) {
        levelsByPerk.put(perkId, Math.max(0, level));
    }

    public boolean removePerk(Identifier perkId) {
        return levelsByPerk.remove(perkId) != null;
    }

    public void clearPerks() {
        levelsByPerk.clear();
    }

    public PerkInstanceState copy() {
        PerkInstanceState copied = new PerkInstanceState(name);
        copied.levelsByPerk.putAll(levelsByPerk);
        return copied;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put(NAME_KEY, NbtString.of(name));

        NbtList perkList = new NbtList();
        for (Map.Entry<Identifier, Integer> entry : levelsByPerk.entrySet()) {
            NbtCompound perkNbt = new NbtCompound();
            perkNbt.putString(PERK_ID_KEY, entry.getKey().toString());
            perkNbt.putInt(LEVEL_KEY, entry.getValue());
            perkList.add(perkNbt);
        }
        nbt.put(PERKS_KEY, perkList);
        return nbt;
    }

    public static PerkInstanceState fromNbt(NbtCompound nbt) {
        String name = nbt.contains(NAME_KEY, NbtElement.STRING_TYPE) ? nbt.getString(NAME_KEY) : "Loadout";
        PerkInstanceState instance = new PerkInstanceState(name);
        if (!nbt.contains(PERKS_KEY, NbtElement.LIST_TYPE)) {
            return instance;
        }

        NbtList perks = nbt.getList(PERKS_KEY, NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < perks.size(); i++) {
            NbtCompound perkNbt = perks.getCompound(i);
            if (!perkNbt.contains(PERK_ID_KEY, NbtElement.STRING_TYPE)) {
                continue;
            }
            Identifier perkId = Identifier.tryParse(perkNbt.getString(PERK_ID_KEY));
            if (perkId == null) {
                continue;
            }
            int level = Math.max(0, perkNbt.getInt(LEVEL_KEY));
            instance.setLevel(perkId, level);
        }

        return instance;
    }
}
