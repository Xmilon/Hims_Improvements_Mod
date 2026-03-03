package net.xmilon.himproveme.item.custom;

import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.xmilon.himproveme.block.ModBlocks;

import java.util.HashMap;
import java.util.Map;

public final class BreezeStaffBlockActions {

    private static final Map<Block, BreezeStaffBlockAction> ACTIONS = new HashMap<>();

    static {
        register(ModBlocks.STASIS_CHAMBER_BLOCK, new StasisChamberBindAction());
    }

    private BreezeStaffBlockActions() {}

    public static ActionResult handle(ItemUsageContext context) {
        Block block = context.getWorld().getBlockState(context.getBlockPos()).getBlock();
        BreezeStaffBlockAction action = ACTIONS.get(block);
        return action == null ? ActionResult.PASS : action.tryPerform(context);
    }

    public static void register(Block block, BreezeStaffBlockAction action) {
        ACTIONS.put(block, action);
    }

    public static boolean isBindableBlock(Block block) {
        return ACTIONS.containsKey(block);
    }

    public interface BreezeStaffBlockAction {
        int getXpCost();

        String getMode();

        ActionResult execute(ItemUsageContext context);

        default ActionResult tryPerform(ItemUsageContext context) {
            PlayerEntity player = context.getPlayer();
            if (player == null) {
                return ActionResult.PASS;
            }

            World world = context.getWorld();
            if (!world.isClient() && getXpCost() > 0 && player.experienceLevel < getXpCost()) {
                player.sendMessage(Text.translatable("item.himproveme.breeze_staff.not_enough_levels", getXpCost())
                        .formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }

            ActionResult result = execute(context);
            if (result != ActionResult.SUCCESS && result != ActionResult.CONSUME) {
                return result;
            }

            if (!world.isClient() && getXpCost() > 0) {
                player.addExperienceLevels(-getXpCost());
            }

            return result;
        }
    }

    private static final class StasisChamberBindAction implements BreezeStaffBlockAction {
        private static final String MODE = "teleport";

        @Override
        public int getXpCost() {
            return BreezeStaffConfig.LINK_XP_COST;
        }

        @Override
        public String getMode() {
            return MODE;
        }

        @Override
        public ActionResult execute(ItemUsageContext context) {
            PlayerEntity player = context.getPlayer();
            if (player == null || !player.isSneaking()) {
                return ActionResult.PASS;
            }
            World world = context.getWorld();
            if (world.isClient()) {
                return ActionResult.success(world.isClient());
            }

            BlockPos pos = context.getBlockPos();
            ItemStack stack = context.getStack();
            StasisBinding.bind(stack, pos, world.getRegistryKey(), MODE);
            stack.set(DataComponentTypes.CUSTOM_NAME, StasisBinding.getBoundName());
            world.playSound(null, pos, SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS, 0.6f, 1f);
            player.sendMessage(Text.translatable("item.himproveme.breeze_staff.bound_success"), true);
            return ActionResult.success(world.isClient());
        }
    }
}
