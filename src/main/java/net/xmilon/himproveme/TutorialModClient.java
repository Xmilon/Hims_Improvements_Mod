package net.xmilon.himproveme;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import java.util.List;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.entity.ModEntities;
import net.xmilon.himproveme.entity.client.DodoModel;
import net.xmilon.himproveme.entity.client.DodoRenderer;
import net.xmilon.himproveme.item.ModItem;
import net.xmilon.himproveme.network.GodlyElytraBoostPayload;
import net.xmilon.himproveme.network.SpecialAbilityTogglePayload;
import net.xmilon.himproveme.network.perk.PerkBookSyncPayload;
import net.xmilon.himproveme.perk.ClientPerkBookState;
import net.xmilon.himproveme.prone.ProneStatePayload;
import net.xmilon.himproveme.item.custom.StasisBinding;
import org.lwjgl.glfw.GLFW;
import net.xmilon.himproveme.network.EnderBundleClientReceiver;
import net.minecraft.util.Formatting;

public class TutorialModClient implements ClientModInitializer{
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
    private static boolean clientProneActive = false;
    private static boolean proneKeyWasDown = false;

    @Override
    public void onInitializeClient() {
        registerSpectralBowPredicates();
        registerPerkNetworking();
        EnderBundleClientReceiver.register();
        registerBreezeStaffPredicate();
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

        //Register Dodo entity
        EntityModelLayerRegistry.registerModelLayer(DodoModel.DODO, DodoModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.DODO, DodoRenderer::new);
    }

    private static void registerSpectralBowPredicates() {
        ModelPredicateProviderRegistry.register(ModItem.SPECTRAL_BOW, Identifier.of("pulling"),
                (ItemStack stack, net.minecraft.client.world.ClientWorld world, LivingEntity entity, int seed) -> {
                    if (entity == null) {
                        return 0.0f;
                    }
                    return entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0f : 0.0f;
                });

        ModelPredicateProviderRegistry.register(ModItem.SPECTRAL_BOW, Identifier.of("pull"),
                (ItemStack stack, net.minecraft.client.world.ClientWorld world, LivingEntity entity, int seed) -> {
                    if (entity == null) {
                        return 0.0f;
                    }
                    if (entity.getActiveItem() != stack) {
                        return 0.0f;
                    }
                    return (stack.getMaxUseTime(entity) - entity.getItemUseTimeLeft()) / 20.0f;
                });
    }

    private static void registerBreezeStaffPredicate() {
        ModelPredicateProviderRegistry.register(ModItem.BREEZE_STAFF,
                Identifier.of(HimProveMe.MOD_ID, "breeze_staff_bound"),
                (ItemStack stack, net.minecraft.client.world.ClientWorld world, LivingEntity entity, int seed) ->
                        StasisBinding.isBound(stack) ? 1f : 0f);
    }

    private static void registerPerkNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(PerkBookSyncPayload.ID, (payload, context) ->
                context.client().execute(() -> ClientPerkBookState.setFromNbt(payload.data())));
    }

    private static void updateProneInput() {
        if (net.minecraft.client.MinecraftClient.getInstance().player == null
                || net.minecraft.client.MinecraftClient.getInstance().getNetworkHandler() == null) {
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
        if (net.minecraft.client.MinecraftClient.getInstance().player == null
                || net.minecraft.client.MinecraftClient.getInstance().getNetworkHandler() == null) {
            return;
        }
        ClientPlayNetworking.send(new ProneStatePayload(true));
    }
}
