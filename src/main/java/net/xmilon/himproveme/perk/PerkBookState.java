package net.xmilon.himproveme.perk;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class PerkBookState {
    private static final String INSTANCES_KEY = "instances";
    private static final String SELECTED_INDEX_KEY = "selected_index";

    private final List<PerkInstanceState> instances = new ArrayList<>();
    private int selectedIndex;

    public enum UpgradeResult {
        SUCCESS,
        NO_SUCH_PERK,
        INSTANCE_OUT_OF_RANGE,
        PREREQUISITE_LOCKED,
        MAX_LEVEL,
        NOT_ENOUGH_LEVELS
    }

    public PerkBookState() {
        instances.add(new PerkInstanceState("Loadout 1"));
        selectedIndex = 0;
    }

    public List<PerkInstanceState> instances() {
        return instances;
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public PerkInstanceState getSelectedInstance() {
        ensureValid();
        return instances.get(selectedIndex);
    }

    public void selectInstance(int index) {
        if (index >= 0 && index < instances.size()) {
            selectedIndex = index;
        }
        ensureValid();
    }

    public boolean createInstance() {
        if (instances.size() >= PerkRegistry.MAX_INSTANCES) {
            return false;
        }
        instances.add(new PerkInstanceState("Loadout " + (instances.size() + 1)));
        selectedIndex = instances.size() - 1;
        ensureValid();
        return true;
    }

    public UpgradeResult upgrade(ServerPlayerEntity player, int instanceIndex, Identifier perkId) {
        if (instanceIndex < 0 || instanceIndex >= instances.size()) {
            return UpgradeResult.INSTANCE_OUT_OF_RANGE;
        }

        PerkDefinition definition = PerkRegistry.get(perkId);
        if (definition == null) {
            return UpgradeResult.NO_SUCH_PERK;
        }

        PerkInstanceState instance = instances.get(instanceIndex);
        int currentLevel = instance.getLevel(perkId);
        if (currentLevel >= definition.maxLevel()) {
            return UpgradeResult.MAX_LEVEL;
        }
        if (definition.hasRequirements()) {
            for (Identifier requiredPerkId : definition.requiredPerkIds()) {
                if (instance.getLevel(requiredPerkId) <= 0) {
                    return UpgradeResult.PREREQUISITE_LOCKED;
                }
            }
        }
        if (player.experienceLevel < PerkRegistry.XP_LEVEL_COST_PER_UPGRADE) {
            return UpgradeResult.NOT_ENOUGH_LEVELS;
        }

        player.addExperienceLevels(-PerkRegistry.XP_LEVEL_COST_PER_UPGRADE);
        instance.setLevel(perkId, currentLevel + 1);
        selectedIndex = instanceIndex;
        return UpgradeResult.SUCCESS;
    }

    public boolean removePerkFromAllInstances(Identifier perkId) {
        boolean changed = false;
        for (PerkInstanceState instance : instances) {
            if (instance.removePerk(perkId)) {
                changed = true;
            }
        }
        return changed;
    }

    public boolean addPerkToAllInstances(Identifier perkId) {
        PerkDefinition definition = PerkRegistry.get(perkId);
        if (definition == null) {
            return false;
        }

        boolean changed = false;
        int targetLevel = Math.max(1, definition.maxLevel());
        for (PerkInstanceState instance : instances) {
            if (instance.getLevel(perkId) < targetLevel) {
                instance.setLevel(perkId, targetLevel);
                changed = true;
            }
        }
        return changed;
    }

    public void resetAllPerks() {
        for (PerkInstanceState instance : instances) {
            instance.clearPerks();
        }
    }

    public NbtCompound toNbt() {
        ensureValid();
        NbtCompound nbt = new NbtCompound();
        NbtList list = new NbtList();
        for (PerkInstanceState instance : instances) {
            list.add(instance.toNbt());
        }
        nbt.put(INSTANCES_KEY, list);
        nbt.putInt(SELECTED_INDEX_KEY, selectedIndex);
        return nbt;
    }

    public static PerkBookState fromNbt(NbtCompound nbt) {
        PerkBookState state = new PerkBookState();
        state.instances.clear();

        if (nbt.contains(INSTANCES_KEY, NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList(INSTANCES_KEY, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                state.instances.add(PerkInstanceState.fromNbt(list.getCompound(i)));
            }
        }

        state.selectedIndex = nbt.getInt(SELECTED_INDEX_KEY);
        state.ensureValid();
        return state;
    }

    public PerkBookState copy() {
        PerkBookState copied = new PerkBookState();
        copied.instances.clear();
        for (PerkInstanceState instance : instances) {
            copied.instances.add(instance.copy());
        }
        copied.selectedIndex = selectedIndex;
        copied.ensureValid();
        return copied;
    }

    private void ensureValid() {
        if (instances.isEmpty()) {
            instances.add(new PerkInstanceState("Loadout 1"));
        }
        if (selectedIndex < 0 || selectedIndex >= instances.size()) {
            selectedIndex = 0;
        }
    }
}
