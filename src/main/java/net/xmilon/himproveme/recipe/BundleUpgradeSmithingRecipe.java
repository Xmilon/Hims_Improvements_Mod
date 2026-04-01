package net.xmilon.himproveme.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.xmilon.himproveme.item.ModItem;
import net.xmilon.himproveme.item.custom.BundleUpgradeHelper;

public final class BundleUpgradeSmithingRecipe implements SmithingRecipe {
    private static final BundleUpgradeSmithingRecipe INSTANCE = new BundleUpgradeSmithingRecipe();
    private static final Ingredient TEMPLATE = Ingredient.ofItems(ModItem.UPGRADE_GEM);
    private static final Ingredient BASE = Ingredient.ofItems(Items.BUNDLE);
    private static final Ingredient ADDITION = Ingredient.ofItems(Items.DIAMOND);
    private static final ItemStack PREVIEW_RESULT = BundleUpgradeHelper.createPreviewStack(2);

    @Override
    public boolean matches(SmithingRecipeInput input, World world) {
        return TEMPLATE.test(input.template())
                && BASE.test(input.base())
                && ADDITION.test(input.addition())
                && BundleUpgradeHelper.canUpgrade(input.base());
    }

    @Override
    public ItemStack craft(SmithingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack base = input.base();
        if (!BundleUpgradeHelper.canUpgrade(base)) {
            return ItemStack.EMPTY;
        }

        ItemStack result = base.copy();
        result.setCount(1);
        BundleUpgradeHelper.setLevel(result, BundleUpgradeHelper.getLevel(base) + 1);
        return result;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return PREVIEW_RESULT.copy();
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return DefaultedList.copyOf(Ingredient.EMPTY, TEMPLATE, BASE, ADDITION);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean testTemplate(ItemStack stack) {
        return TEMPLATE.test(stack);
    }

    @Override
    public boolean testBase(ItemStack stack) {
        return BASE.test(stack) && BundleUpgradeHelper.canUpgrade(stack);
    }

    @Override
    public boolean testAddition(ItemStack stack) {
        return ADDITION.test(stack);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BUNDLE_UPGRADE_SMITHING;
    }

    public static class Serializer implements RecipeSerializer<BundleUpgradeSmithingRecipe> {
        private static final MapCodec<BundleUpgradeSmithingRecipe> CODEC = MapCodec.unit(INSTANCE);
        private static final PacketCodec<RegistryByteBuf, BundleUpgradeSmithingRecipe> PACKET_CODEC = PacketCodec.ofStatic(
                Serializer::write,
                Serializer::read
        );

        @Override
        public MapCodec<BundleUpgradeSmithingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, BundleUpgradeSmithingRecipe> packetCodec() {
            return PACKET_CODEC;
        }

        private static BundleUpgradeSmithingRecipe read(RegistryByteBuf buf) {
            return INSTANCE;
        }

        private static void write(RegistryByteBuf buf, BundleUpgradeSmithingRecipe recipe) {
        }
    }
}
