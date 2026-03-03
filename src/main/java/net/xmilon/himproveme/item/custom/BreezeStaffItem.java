package net.xmilon.himproveme.item.custom;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.xmilon.himproveme.item.custom.ability.SpecialAbilityItem;

public class BreezeStaffItem extends Item implements SpecialAbilityItem {

    public BreezeStaffItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack stack = context.getStack();
        Block targetBlock = context.getWorld().getBlockState(context.getBlockPos()).getBlock();
        if (StasisBinding.isBound(stack) && BreezeStaffBlockActions.isBindableBlock(targetBlock)) {
            if (!context.getWorld().isClient() && context.getPlayer() != null) {
                context.getPlayer().sendMessage(Text.translatable("item.himproveme.breeze_staff.link_requires_unbind")
                        .formatted(Formatting.YELLOW), true);
            }
            return ActionResult.FAIL;
        }

        ActionResult result = BreezeStaffBlockActions.handle(context);
        if (result != ActionResult.PASS) {
            context.getStack().setDamage(0);
            return result;
        }
        return super.useOnBlock(context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) {
            return TypedActionResult.success(stack, world.isClient());
        }

        if (!StasisBinding.isBound(stack)) {
            user.sendMessage(Text.translatable("item.himproveme.breeze_staff.requires_special_block"), true);
            return TypedActionResult.fail(stack);
        }

        user.sendMessage(Text.translatable("item.himproveme.breeze_staff.use_special_ability_to_teleport"), true);
        return TypedActionResult.fail(stack);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return StasisBinding.isBound(stack) || super.isItemBarVisible(stack);
    }

    @Override
    public void onSpecialAbilityTick(ServerPlayerEntity player, ItemStack stack) {
        // No ongoing effect for Breeze Staff's special ability.
    }

    @Override
    public void onSpecialAbilityActivated(ServerPlayerEntity player, ItemStack stack) {
        if (!StasisBinding.isBound(stack)) {
            player.sendMessage(Text.translatable("item.himproveme.breeze_staff.unbind_not_bound")
                    .formatted(Formatting.GRAY), true);
            return;
        }

        attemptTeleport(player, stack);
    }

    @Override
    public boolean keepsAbilityActive() {
        return false;
    }

    private static boolean attemptTeleport(ServerPlayerEntity player, ItemStack stack) {
        BlockPos targetPos = StasisBinding.getBoundPos(stack);
        RegistryKey<World> targetDim = StasisBinding.getBoundDimension(stack);
        if (targetPos == null || targetDim == null) {
            player.sendMessage(Text.translatable("item.himproveme.breeze_staff.requires_special_block"), true);
            return false;
        }

        if (!player.getWorld().getRegistryKey().equals(targetDim)) {
            player.sendMessage(Text.translatable("item.himproveme.breeze_staff.wrong_dimension"), true);
            return false;
        }

        if (player.experienceLevel < BreezeStaffConfig.TELEPORT_XP_COST) {
            player.sendMessage(Text.translatable("item.himproveme.breeze_staff.teleport_not_enough_levels", BreezeStaffConfig.TELEPORT_XP_COST)
                    .formatted(Formatting.RED), true);
            return false;
        }

        ServerWorld targetWorld = player.getServer().getWorld(targetDim);
        if (targetWorld == null) {
            return false;
        }

        player.addExperienceLevels(-BreezeStaffConfig.TELEPORT_XP_COST);

        player.teleport(targetWorld,
                targetPos.getX() + 0.5,
                targetPos.getY() + 1.0,
                targetPos.getZ() + 0.5,
                player.getYaw(),
                player.getPitch());

        targetWorld.playSound(null, targetPos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f);
        player.sendMessage(Text.translatable("item.himproveme.breeze_staff.teleported")
                .formatted(Formatting.DARK_PURPLE), true);
        StasisBinding.clear(stack);
        return true;
    }
}
