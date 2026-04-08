package net.xmilon.himproveme.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class MegaRocketItem extends Item {
    public MegaRocketItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (player.isSneaking()) {
            ActionResult result = handleSneakInteraction(world, player, stack, hand);
            return result.isAccepted() ? TypedActionResult.success(stack, world.isClient()) : TypedActionResult.fail(stack);
        }

        if (!player.isFallFlying()) {
            return TypedActionResult.pass(stack);
        }

        if (!canLaunch(stack)) {
            sendNoPowerMessage(player, stack);
            return TypedActionResult.fail(stack);
        }

        if (!world.isClient()) {
            int launchLevel = MegaRocketHelper.getActiveLevel(stack);
            FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(world, MegaRocketHelper.createLaunchStack(launchLevel), player);
            world.spawnEntity(fireworkRocketEntity);
            if (!player.getAbilities().creativeMode) {
                MegaRocketHelper.consumeSelectedPower(stack);
            }
            player.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (player != null && player.isSneaking()) {
            return handleSneakInteraction(world, player, stack, context.getHand());
        }

        if (!canLaunch(stack)) {
            if (player != null) {
                sendNoPowerMessage(player, stack);
            }
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            Vec3d hitPos = context.getHitPos();
            Direction side = context.getSide();
            ItemStack launchStack = MegaRocketHelper.createLaunchStack(MegaRocketHelper.getActiveLevel(stack));

            FireworkRocketEntity fireworkRocketEntity;
            if (player != null) {
                fireworkRocketEntity = new FireworkRocketEntity(
                        world,
                        player,
                        hitPos.x + side.getOffsetX() * 0.15,
                        hitPos.y + side.getOffsetY() * 0.15,
                        hitPos.z + side.getOffsetZ() * 0.15,
                        launchStack
                );
                player.incrementStat(Stats.USED.getOrCreateStat(this));
            } else {
                fireworkRocketEntity = new FireworkRocketEntity(
                        world,
                        hitPos.x + side.getOffsetX() * 0.15,
                        hitPos.y + side.getOffsetY() * 0.15,
                        hitPos.z + side.getOffsetZ() * 0.15,
                        launchStack
                );
            }

            world.spawnEntity(fireworkRocketEntity);
            if (player == null || !player.getAbilities().creativeMode) {
                MegaRocketHelper.consumeSelectedPower(stack);
            }
        }

        return ActionResult.success(world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        int totalPower = MegaRocketHelper.getTotalPower(stack);
        int activeLevel = MegaRocketHelper.getActiveLevel(stack);

        tooltip.add(Text.translatable("item.himproveme.mega_rocket.power", totalPower).formatted(Formatting.GRAY));
        if (activeLevel > 0) {
            tooltip.add(Text.translatable(
                    "item.himproveme.mega_rocket.selected",
                    Text.translatable("item.himproveme.mega_rocket.level", activeLevel)
            ).formatted(Formatting.GRAY));
        } else {
            tooltip.add(Text.translatable(
                    "item.himproveme.mega_rocket.selected",
                    Text.translatable("item.himproveme.mega_rocket.empty")
            ).formatted(Formatting.GRAY));
        }
        tooltip.add(Text.translatable("item.himproveme.mega_rocket.hint.cycle").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.translatable("item.himproveme.mega_rocket.hint.add").formatted(Formatting.DARK_GRAY));
    }

    private ActionResult handleSneakInteraction(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack otherHandStack = player.getStackInHand(otherHand);

        if (MegaRocketHelper.isFireworkRocket(otherHandStack)) {
            if (!world.isClient()) {
                int addedPower = MegaRocketHelper.getPowerFromRocketStack(otherHandStack);
                MegaRocketHelper.addPower(stack, addedPower);
                if (!player.getAbilities().creativeMode) {
                    otherHandStack.decrement(otherHandStack.getCount());
                }
                player.sendMessage(Text.translatable(
                        "item.himproveme.mega_rocket.added_power",
                        addedPower,
                        MegaRocketHelper.getTotalPower(stack)
                ).formatted(Formatting.GREEN), true);
            }
            return ActionResult.success(world.isClient());
        }

        int nextLevel = MegaRocketHelper.getHighestSelectableLevel(stack) <= 0 ? 0 : (world.isClient() ? MegaRocketHelper.getSelectedLevel(stack) : MegaRocketHelper.cycleSelectedLevel(stack));
        if (nextLevel <= 0) {
            if (!world.isClient()) {
                player.sendMessage(Text.translatable("item.himproveme.mega_rocket.no_power").formatted(Formatting.RED), true);
            }
            return ActionResult.FAIL;
        }

        if (!world.isClient()) {
            player.sendMessage(Text.translatable("item.himproveme.mega_rocket.mode_changed", nextLevel).formatted(Formatting.YELLOW), true);
        }
        return ActionResult.success(world.isClient());
    }

    private static boolean canLaunch(ItemStack stack) {
        int activeLevel = MegaRocketHelper.getActiveLevel(stack);
        return activeLevel > 0 && MegaRocketHelper.getTotalPower(stack) >= MegaRocketHelper.getLevelCost(activeLevel);
    }

    private static void sendNoPowerMessage(PlayerEntity player, ItemStack stack) {
        if (player.getWorld().isClient()) {
            return;
        }

        if (MegaRocketHelper.getTotalPower(stack) <= 0) {
            player.sendMessage(Text.translatable("item.himproveme.mega_rocket.no_power").formatted(Formatting.RED), true);
            return;
        }

        int activeLevel = MegaRocketHelper.getActiveLevel(stack);
        player.sendMessage(Text.translatable(
                "item.himproveme.mega_rocket.not_enough_power",
                MegaRocketHelper.getLevelCost(activeLevel),
                activeLevel
        ).formatted(Formatting.RED), true);
    }
}
