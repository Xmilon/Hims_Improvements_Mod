package net.xmilon.himproveme;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.client.AcrobatPerkClientHelper;
import net.xmilon.himproveme.client.BlowgunClientHelper;
import net.xmilon.himproveme.client.DaggerGripClientHelper;
import net.xmilon.himproveme.client.GodlyElytraClientHandler;
import net.xmilon.himproveme.client.InspectAnimationHelper;
import net.xmilon.himproveme.client.WardenPerkClientHelper;
import net.xmilon.himproveme.entity.ModEntities;
import net.xmilon.himproveme.entity.client.DodoModel;
import net.xmilon.himproveme.entity.client.DodoRenderer;
import net.xmilon.himproveme.item.ModItem;
import net.xmilon.himproveme.access.HandledScreenBundleScrollAccess;
import net.xmilon.himproveme.item.custom.BundleUpgradeHelper;
import net.xmilon.himproveme.item.custom.ContainerPreviewTooltipData;
import net.xmilon.himproveme.item.custom.LockableContainerHelper;
import net.xmilon.himproveme.leveling.ClientLevelingState;
import net.xmilon.himproveme.network.BundleScrollNetworking;
import net.xmilon.himproveme.network.BundleScrollPayload;
import net.xmilon.himproveme.network.GodlyElytraBoostPayload;
import net.xmilon.himproveme.network.ShulkerScrollPayload;
import net.xmilon.himproveme.network.SpecialAbilityTogglePayload;
import net.xmilon.himproveme.network.leveling.LevelingSyncPayload;
import net.xmilon.himproveme.network.perk.PerkBookSyncPayload;
import net.xmilon.himproveme.network.perk.VillagerTradeStatusPayload;
import net.xmilon.himproveme.perk.ClientVillagerTradeStatus;
import net.xmilon.himproveme.perk.ClientPerkBookState;
import net.xmilon.himproveme.perk.PerkBookState;
import net.xmilon.himproveme.perk.PerkBookStateHolder;
import net.xmilon.himproveme.prone.ProneStatePayload;
import net.xmilon.himproveme.item.custom.StasisBinding;
import net.xmilon.himproveme.tooltip.client.ContainerPreviewTooltipComponent;
import org.lwjgl.glfw.GLFW;
import net.xmilon.himproveme.network.EnderBundleClientReceiver;
import net.minecraft.util.Formatting;

public class TutorialModClient implements ClientModInitializer {
    private static final KeyBinding GODLY_ELYTRA_BOOST_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.himproveme.godly_elytra_boost",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.himproveme.keys"
    ));
    private static final KeyBinding SPECIAL_ABILITY_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.himproveme.special_ability",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "category.himproveme.keys"
    ));
    private static final KeyBinding PRONE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.himproveme.prone",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "category.himproveme.keys"
    ));
    private static final KeyBinding INSPECT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.himproveme.inspect",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "category.himproveme.keys"
    ));
    private static boolean clientProneActive = false;
    private static boolean proneKeyWasDown = false;

    @Override
    public void onInitializeClient() {
        GodlyElytraClientHandler.register();
        registerSpectralBowPredicates();
        registerBlowgunPredicates();
        registerPerkNetworking();
        DaggerGripClientHelper.registerReceiver();
        WardenPerkClientHelper.register();
        registerBundleScrollInput();
        EnderBundleClientReceiver.register();
        registerBreezeStaffPredicate();

        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof ContainerPreviewTooltipData previewData) {
                return new ContainerPreviewTooltipComponent(previewData);
            }
            return null;
        });

        ItemTooltipCallback.EVENT.register((stack, context, type, tooltip) -> {
            if (!stack.isOf(ModItem.BREEZE_STAFF)) {
                return;
            }
            tooltip.add(Text.translatable("item.himproveme.breeze_staff.tooltip.requires_unbind")
                    .formatted(Formatting.GRAY));
            if (!StasisBinding.isBound(stack)) {
                return;
            }
            tooltip.add(Text.translatable("item.himproveme.breeze_staff.tooltip.bound_requires_unbind")
                    .formatted(Formatting.YELLOW));
            Text modeText = StasisBinding.getModeText(stack);
            if (modeText != null) {
                tooltip.add(Text.translatable("item.himproveme.breeze_staff.mode", modeText));
            }
            Text boundText = StasisBinding.getBoundTooltip(stack);
            if (boundText != null) {
                tooltip.add(boundText);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (GODLY_ELYTRA_BOOST_KEY.wasPressed()) {
                ClientPlayNetworking.send(GodlyElytraBoostPayload.INSTANCE);
            }
            while (SPECIAL_ABILITY_KEY.wasPressed()) {
                ClientPlayNetworking.send(SpecialAbilityTogglePayload.INSTANCE);
            }
            AcrobatPerkClientHelper.tick(client);
            BlowgunClientHelper.tick(client);
            DaggerGripClientHelper.tick(client);
            InspectAnimationHelper.tick(client, INSPECT_KEY);
            WardenPerkClientHelper.tick(client);
            updateProneInput();

            if (client.player != null
                    && client.currentScreen != null
                    && !(client.currentScreen instanceof InventoryScreen)
                    && client.player.getRecipeBook().isGuiOpen(RecipeBookCategory.CRAFTING)) {
                boolean filtering = client.player.getRecipeBook().isFilteringCraftable(RecipeBookCategory.CRAFTING);
                client.player.getRecipeBook().setGuiOpen(RecipeBookCategory.CRAFTING, false);
                if (client.getNetworkHandler() != null) {
                    client.getNetworkHandler().sendPacket(new RecipeCategoryOptionsC2SPacket(RecipeBookCategory.CRAFTING, false, filtering));
                }
            }
        });

        EntityModelLayerRegistry.registerModelLayer(DodoModel.DODO, DodoModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.DODO, DodoRenderer::new);
    }

    private static void registerBundleScrollInput() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof HandledScreen<?> handledScreen)) {
                return;
            }
            ScreenMouseEvents.allowMouseScroll(screen).register((currentScreen, mouseX, mouseY, horizontalAmount, verticalAmount) ->
                    !handleBundleScroll(handledScreen, verticalAmount));
        });
    }

    private static boolean handleBundleScroll(HandledScreen<?> handledScreen, double verticalAmount) {
        int direction = Double.compare(verticalAmount, 0.0);
        if (direction == 0) return false;

        Slot focusedSlot = ((HandledScreenBundleScrollAccess) handledScreen).himproveme$getFocusedSlot();
        if (focusedSlot != null) {
            ItemStack hoveredStack = focusedSlot.getStack();
            ItemStack cursorStack = handledScreen.getScreenHandler().getCursorStack();
            if (LockableContainerHelper.canExtractFromShulker(hoveredStack, cursorStack, direction)) {
                ClientPlayNetworking.send(new ShulkerScrollPayload(handledScreen.getScreenHandler().syncId, focusedSlot.id, direction));
                return true;
            }
            if (BundleUpgradeHelper.isBundle(hoveredStack)) {
                ClientPlayNetworking.send(new BundleScrollPayload(handledScreen.getScreenHandler().syncId, focusedSlot.id, direction));
                return true;
            }
        }

        ItemStack cursorStack = handledScreen.getScreenHandler().getCursorStack();
        if (BundleUpgradeHelper.isBundle(cursorStack)) {
            ClientPlayNetworking.send(new BundleScrollPayload(handledScreen.getScreenHandler().syncId, BundleScrollNetworking.CURSOR_SLOT, direction));
            return true;
        }
        return false;
    }

    private static void registerSpectralBowPredicates() {
        ModelPredicateProviderRegistry.register(ModItem.SPECTRAL_BOW, Identifier.of("pulling"),
                (ItemStack stack, net.minecraft.client.world.ClientWorld world, LivingEntity entity, int seed) -> {
                    if (entity == null) return 0.0f;
                    return entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0f : 0.0f;
                });
        ModelPredicateProviderRegistry.register(ModItem.SPECTRAL_BOW, Identifier.of("pull"),
                (ItemStack stack, net.minecraft.client.world.ClientWorld world, LivingEntity entity, int seed) -> {
                    if (entity == null) return 0.0f;
                    if (entity.getActiveItem() != stack) return 0.0f;
                    return (stack.getMaxUseTime(entity) - entity.getItemUseTimeLeft()) / 20.0f;
                });
    }

    private static void registerBreezeStaffPredicate() {
        ModelPredicateProviderRegistry.register(ModItem.BREEZE_STAFF,
                Identifier.of(HimProveMe.MOD_ID, "breeze_staff_bound"),
                (ItemStack stack, net.minecraft.client.world.ClientWorld world, LivingEntity entity, int seed) ->
                        StasisBinding.isBound(stack) ? 1f : 0f);
    }

    private static void registerBlowgunPredicates() {
        ModelPredicateProviderRegistry.register(ModItem.BLOWGUN,
                Identifier.of(HimProveMe.MOD_ID, "held"),
                (ItemStack stack, net.minecraft.client.world.ClientWorld world, LivingEntity entity, int seed) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    return entity != null && entity == client.player && client.options.getPerspective().isFirstPerson() ? 1.0F : 0.0F;
                });
        ModelPredicateProviderRegistry.register(ModItem.BLOWGUN,
                Identifier.of(HimProveMe.MOD_ID, "armed"),
                (ItemStack stack, net.minecraft.client.world.ClientWorld world, LivingEntity entity, int seed) ->
                        entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F);
    }

    private static void registerPerkNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(PerkBookSyncPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    PerkBookState state = PerkBookState.fromNbt(payload.data());
                    ClientPerkBookState.setFromNbt(payload.data());
                    if (context.client().player instanceof PerkBookStateHolder holder) {
                        holder.himproveme$setPerkBookState(state);
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(LevelingSyncPayload.ID, (payload, context) ->
                context.client().execute(() -> ClientLevelingState.setFromNbt(payload.data())));
        ClientPlayNetworking.registerGlobalReceiver(VillagerTradeStatusPayload.ID, (payload, context) ->
                context.client().execute(() -> ClientVillagerTradeStatus.setFromNbt(payload.data())));
    }

    private static void updateProneInput() {
        if (MinecraftClient.getInstance().player == null
                || MinecraftClient.getInstance().getNetworkHandler() == null) {
            clientProneActive = false;
            proneKeyWasDown = false;
            return;
        }
        boolean proneKeyDown = PRONE_KEY.isPressed();
        if (proneKeyDown && !proneKeyWasDown) {
            clientProneActive = !clientProneActive;
            sendProneToggleRequest();
        }
        proneKeyWasDown = proneKeyDown;
    }

    public static boolean isProne() {
        return clientProneActive;
    }

    private static void sendProneToggleRequest() {
        if (MinecraftClient.getInstance().player == null
                || MinecraftClient.getInstance().getNetworkHandler() == null) {
            return;
        }
        ClientPlayNetworking.send(new ProneStatePayload(true));
    }
}