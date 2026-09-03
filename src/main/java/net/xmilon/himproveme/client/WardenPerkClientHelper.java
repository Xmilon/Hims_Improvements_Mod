package net.xmilon.himproveme.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.xmilon.himproveme.effect.ModStatusEffects;
import net.xmilon.himproveme.network.warden.WardenSepukuPayload;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class WardenPerkClientHelper {
    private static boolean registered;
    private static final Map<UUID, SepukuAnimationState> sepukuStates = new HashMap<>();

    private WardenPerkClientHelper() {
    }

    public static void register() {
        if (registered) return;
        registered = true;

        ClientPlayNetworking.registerGlobalReceiver(WardenSepukuPayload.ID, (payload, context) ->
                context.client().execute(() -> sepukuStates.put(payload.entityUuid(), new SepukuAnimationState(payload.durationTicks(), payload.durationTicks())))
        );
        WorldRenderEvents.AFTER_ENTITIES.register(WardenPerkClientHelper::renderFrenzyOverlay);
    }

    public static void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            sepukuStates.clear();
            return;
        }
        tickSepukuAnimations();
    }

    public static float getSepukuProgress(LivingEntity entity) {
        SepukuAnimationState state = sepukuStates.get(entity.getUuid());
        if (state == null || state.durationTicks <= 0) return 0.0F;
        return 1.0F - (state.remainingTicks / (float) state.durationTicks);
    }

    public static void applySepukuPose(
            LivingEntity entity,
            ModelPart head,
            ModelPart body,
            ModelPart rightArm,
            ModelPart leftArm,
            ModelPart rightLeg,
            ModelPart leftLeg
    ) {
        float progress = getSepukuProgress(entity);
        if (progress <= 0.0F) return;

        float drawPhase = Math.min(progress / 0.45F, 1.0F);
        float strikePhase = progress <= 0.45F ? 0.0F : Math.min((progress - 0.45F) / 0.55F, 1.0F);
        float easedStrike = strikePhase * strikePhase;

        body.pitch += 0.12F + drawPhase * 0.22F + easedStrike * 0.36F;
        body.yaw += MathHelper.sin(progress * (float) Math.PI) * 0.12F;
        head.pitch += 0.18F + easedStrike * 0.32F;
        head.yaw *= 0.35F;

        rightArm.yaw -= 0.25F + drawPhase * 0.55F;
        rightArm.roll += 0.08F + drawPhase * 0.18F;
        rightArm.pitch = -1.15F - drawPhase * 0.45F + easedStrike * 2.25F;

        leftArm.yaw += 0.28F;
        leftArm.roll -= 0.12F;
        leftArm.pitch = -0.55F - drawPhase * 0.20F + easedStrike * 0.85F;

        rightLeg.pitch -= 0.10F + progress * 0.10F;
        leftLeg.pitch += 0.05F + progress * 0.08F;
    }

    private static void tickSepukuAnimations() {
        Iterator<Map.Entry<UUID, SepukuAnimationState>> iterator = sepukuStates.entrySet().iterator();
        while (iterator.hasNext()) {
            SepukuAnimationState state = iterator.next().getValue();
            state.remainingTicks = Math.max(0, state.remainingTicks - 1);
            if (state.remainingTicks <= 0) {
                iterator.remove();
            }
        }
    }

    private static void renderFrenzyOverlay(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !client.player.hasStatusEffect(ModStatusEffects.FRENZY)) return;

        MatrixStack matrixStack = context.matrixStack();
        matrixStack.push();
        matrixStack.loadIdentity();

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 0.0F, 0.15F);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        buffer.vertex(matrix, -1.0F, -1.0F, 0.0F);
        buffer.vertex(matrix, 1.0F, -1.0F, 0.0F);
        buffer.vertex(matrix, 1.0F, 1.0F, 0.0F);
        buffer.vertex(matrix, -1.0F, 1.0F, 0.0F);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.pop();
    }

    private static final class SepukuAnimationState {
        private final int durationTicks;
        private int remainingTicks;

        private SepukuAnimationState(int durationTicks, int remainingTicks) {
            this.durationTicks = durationTicks;
            this.remainingTicks = remainingTicks;
        }
    }
}
