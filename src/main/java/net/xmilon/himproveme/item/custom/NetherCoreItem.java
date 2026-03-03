package net.xmilon.himproveme.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class NetherCoreItem extends SmithingTemplateItem {
    public NetherCoreItem() {
        super(
                Text.literal("Applies to equipment"),
                Text.literal("Ingredients"),
                Text.literal("Nether Core Template"),
                Text.literal("Insert equipment"),
                Text.literal("Insert material"),
                List.of(),
                List.of()
        );
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (world.isClient() || !(entity instanceof PlayerEntity player)) {
            return;
        }

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 0, true, false, true));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack).copy().formatted(Formatting.GOLD);
    }
}
