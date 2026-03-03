package net.xmilon.himproveme.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.xmilon.himproveme.block.ModBlocks;

import java.util.Map;

public class ChiselItem extends Item {
    public static final Map<Block, Block> CHISEL_MAP =
            Map.of(
                    //Blocks.CRAFTING_TABLE, ModBlocks.MAGIC_TABLE,
                    Blocks.STONE, Blocks.STONE_BRICKS,
                    Blocks.COBBLESTONE, Blocks.STONE_BRICKS,
                    Blocks.COBBLED_DEEPSLATE, Blocks.DEEPSLATE_BRICKS,
                    Blocks.DEEPSLATE, Blocks.DEEPSLATE_BRICKS,
                    ModBlocks.RAW_UPGRADE_GEM_BLOCK, ModBlocks.UPGRADE_GEM_BLOCK,
                    Blocks.SAND, Blocks.GLASS,
                    Blocks.CLAY, Blocks.BRICKS,
                    Blocks.NETHERRACK, Blocks.NETHER_BRICKS,
                    Blocks.END_STONE, Blocks.END_STONE_BRICKS,
                    ModBlocks.MAGIC_BLOCK, ModBlocks.STASIS_CHAMBER_BLOCK

            );

    public ChiselItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Block clickedBlock = world.getBlockState(context.getBlockPos()).getBlock();

        if(CHISEL_MAP.containsKey(clickedBlock)) {
            if(!world.isClient()){
                world.setBlockState(context.getBlockPos(), CHISEL_MAP.get(clickedBlock).getDefaultState());

                context.getStack().damage(1, ((ServerWorld) world), ((ServerPlayerEntity) context.getPlayer()),
                    item -> context.getPlayer().sendEquipmentBreakStatus(item, EquipmentSlot.MAINHAND));

                world.playSound(null, context.getBlockPos(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS);
            }
        }


        return ActionResult.SUCCESS;
    }


}
