package net.xmilon.himproveme.util;

import net.xmilon.himproveme.HimProveMe;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> NEEDS_ENDER_TOOL = createTag("needs_ender_tool");
        public static final TagKey<Block> INCORRECT_FOR_ENDER_TOOL = createTag("incorrect_for_ender_tool");

        private static TagKey<Block> createTag(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of(HimProveMe.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> TRANSFORMABLE_ITEMS = createTag("transformable_items");
        public static final TagKey<Item> DUAL_WIELDABLE = createTag("dual_wieldable");
        public static final TagKey<Item> INSPECT_SPINNABLE = createTag("inspect_spinnable");
        public static final TagKey<Item> INSPECT_Y_SPIN = createTag("inspect_y_spin");
        public static final TagKey<Item> INSPECT_HALF_SPIN = createTag("inspect_half_spin");
        public static final TagKey<Item> INSPECT_KARAMBIT_SPIN = createTag("inspect_karambit_spin");
        public static final TagKey<Item> INSPECT_HEAVY_SPIN = createTag("inspect_heavy_spin");
        public static final TagKey<Item> INSPECT_STAFF_SPIN = createTag("inspect_staff_spin");
        public static final TagKey<Item> RAVAGER_TAMING_FOOD = createTag("ravager_taming_food");
        public static final TagKey<Item> RAVAGER_STORAGE_ATTACHMENTS = createTag("ravager_storage_attachments");

        private static TagKey<Item> createTag(String name) {
            return TagKey.of(RegistryKeys.ITEM, Identifier.of(HimProveMe.MOD_ID, name));
        }
    }
}
