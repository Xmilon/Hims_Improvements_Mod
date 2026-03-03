package net.xmilon.himproveme.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.xmilon.himproveme.item.ModItem;

public class MagicBlock extends Block {

    public MagicBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                                 BlockHitResult hit) {

        world.playSound(player, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 4f, 1f);

        return ActionResult.SUCCESS;
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {

        if(entity instanceof ItemEntity ItemEntity){
            if(ItemEntity.getStack().getItem() == Items.BUNDLE){
                ItemEntity.setStack(new ItemStack(ModItem.ENDER_BUNDLE, ItemEntity.getStack().getCount()));
                world.playSound(entity, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 4f, 3f);
            }else

            if(ItemEntity.getStack().getItem() == Items.ENDER_EYE){
                ItemEntity.setStack(new ItemStack(ModItem.RAW_ENDER_ESSENCE, ItemEntity.getStack().getCount()));
                world.playSound(entity, pos, SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.BLOCKS, 4f, 1.8f);

            }

        }

        super.onSteppedOn(world, pos, state, entity);
    }
}
