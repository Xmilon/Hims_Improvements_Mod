package net.xmilon.himproveme.item.custom;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public final class StasisBinding {

    private static final ComponentType<NbtComponent> CUSTOM_DATA = DataComponentTypes.CUSTOM_DATA;
    private static final String BINDING_KEY = "himproveme_stasis_binding";
    private static final String POS_KEY = "Pos";
    private static final String DIM_KEY = "Dimension";
    private static final String MODE_KEY = "Mode";

    private StasisBinding() {}

    public static boolean isBound(ItemStack stack) {
        return getBindingCompound(stack) != null;
    }

    public static void bind(ItemStack stack, BlockPos pos, RegistryKey<World> dimension, String mode) {
        NbtComponent.set(CUSTOM_DATA, stack, compound -> {
            NbtCompound binding = new NbtCompound();
            binding.putLong(POS_KEY, pos.asLong());
            binding.putString(DIM_KEY, dimension.getValue().toString());
            binding.putString(MODE_KEY, mode == null ? "" : mode);
            compound.put(BINDING_KEY, binding);
        });
    }

    public static void clear(ItemStack stack) {
        NbtComponent.set(CUSTOM_DATA, stack, compound -> compound.remove(BINDING_KEY));
    }

    public static BlockPos getBoundPos(ItemStack stack) {
        NbtCompound binding = getBindingCompound(stack);
        return binding == null ? null : BlockPos.fromLong(binding.getLong(POS_KEY));
    }

    public static RegistryKey<World> getBoundDimension(ItemStack stack) {
        NbtCompound binding = getBindingCompound(stack);
        if (binding == null) return null;
        String dim = binding.getString(DIM_KEY);
        return dim.isEmpty() ? null : RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dim));
    }

    public static String getMode(ItemStack stack) {
        NbtCompound binding = getBindingCompound(stack);
        if (binding == null) {
            return null;
        }
        String mode = binding.getString(MODE_KEY);
        return mode.isEmpty() ? null : mode;
    }

    public static Text getModeText(ItemStack stack) {
        String mode = getMode(stack);
        if (mode == null) {
            return null;
        }
        return Text.translatable("item.himproveme.breeze_staff.mode." + mode);
    }

    public static Text getBoundTooltip(ItemStack stack) {
        BlockPos pos = getBoundPos(stack);
        RegistryKey<World> dim = getBoundDimension(stack);
        if (pos == null || dim == null) {
            return null;
        }
        return Text.translatable("item.himproveme.breeze_staff.bound", pos.getX(), pos.getY(), pos.getZ(), dim.getValue().toString())
                .formatted(Formatting.GRAY);
    }

    public static Text getBoundName() {
        return Text.translatable("item.himproveme.breeze_staff.name").formatted(Formatting.LIGHT_PURPLE);
    }

    private static NbtCompound getBindingCompound(ItemStack stack) {
        NbtComponent custom = stack.getOrDefault(CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound root = custom.getNbt();
        if (root.contains(BINDING_KEY, NbtElement.COMPOUND_TYPE)) {
            return root.getCompound(BINDING_KEY);
        }
        return null;
    }
}
