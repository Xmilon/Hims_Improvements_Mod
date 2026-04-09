package net.xmilon.himproveme.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.xmilon.himproveme.network.warden.WardenAfflictionSyncPayload;
import net.xmilon.himproveme.network.warden.WardenSepukuPayload;
import net.xmilon.himproveme.perk.warden.AfflictionProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Owns all client-only Warden perk presentation state: synced control pressure, hallucinations and sepuku animation timing.
 */
public final class WardenPerkClientHelper {
    private static final int HALLUCINATION_LIFETIME_TICKS = 26;
    private static final int HALLUCINATION_COOLDOWN_TICKS = 24;
    private static final byte[] IDENTITY_MAPPING = new byte[]{0, 1, 2, 3};

    private static boolean registered;
    private static ClientAfflictionSnapshot snapshot = ClientAfflictionSnapshot.inactive();
    private static final List<Hallucination> hallucinations = new ArrayList<>();
    private static final Map<UUID, SepukuAnimationState> sepukuStates = new HashMap<>();
    private static int nextHallucinationTick;

    private WardenPerkClientHelper() {
    }

    /**
     * Registers packet receivers and the AFTER_ENTITIES render hook once on the client.
     */
    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        ClientPlayNetworking.registerGlobalReceiver(WardenAfflictionSyncPayload.ID, (payload, context) ->
                context.client().execute(() -> snapshot = ClientAfflictionSnapshot.fromPayload(payload))
        );
        ClientPlayNetworking.registerGlobalReceiver(WardenSepukuPayload.ID, (payload, context) ->
                context.client().execute(() -> sepukuStates.put(payload.entityUuid(), new SepukuAnimationState(payload.durationTicks(), payload.durationTicks())))
        );
        WorldRenderEvents.AFTER_ENTITIES.register(WardenPerkClientHelper::renderHallucinations);
    }

    /**
     * Advances local countdowns, hallucination spawning, and cleanup from the current synced Warden affliction snapshot.
     */
    public static void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            snapshot = ClientAfflictionSnapshot.inactive();
            hallucinations.clear();
            sepukuStates.clear();
            nextHallucinationTick = 0;
            return;
        }

        tickSepukuAnimations();
        tickHallucinations(client);
    }

    /**
     * Applies the client-side stunned tunnel vision without touching vanilla camera math for other states.
     */
    public static double modifyFov(double originalFov) {
        if (!snapshot.active || snapshot.profile != AfflictionProfile.STUNNED) {
            return originalFov;
        }

        double reduction = snapshot.controlChaos ? 18.0D : 10.0D;
        return Math.max(50.0D, originalFov - reduction);
    }

    /**
     * Returns the mouse-look multiplier used by the Mouse mixin while the player is stunned.
     */
    public static double getMouseLookMultiplier() {
        if (!snapshot.active || snapshot.profile != AfflictionProfile.STUNNED) {
            return 1.0D;
        }

        return snapshot.controlChaos ? 0.45D : 0.70D;
    }

    /**
     * Applies the synced movement remap or axis inversion after vanilla input polling has populated the current frame.
     */
    public static void applyMovementDisruption(Input input) {
        if (!snapshot.active) {
            return;
        }

        if (snapshot.profile == AfflictionProfile.STUNNED && snapshot.controlChaos) {
            boolean[] original = new boolean[]{
                    input.pressingForward,
                    input.pressingBack,
                    input.pressingLeft,
                    input.pressingRight
            };
            input.pressingForward = original[MathHelper.clamp(snapshot.movementMapping[0], 0, 3)];
            input.pressingBack = original[MathHelper.clamp(snapshot.movementMapping[1], 0, 3)];
            input.pressingLeft = original[MathHelper.clamp(snapshot.movementMapping[2], 0, 3)];
            input.pressingRight = original[MathHelper.clamp(snapshot.movementMapping[3], 0, 3)];
        }

        if (snapshot.profile == AfflictionProfile.FRENZY) {
            if (snapshot.invertForwardAxis) {
                boolean originalForward = input.pressingForward;
                input.pressingForward = input.pressingBack;
                input.pressingBack = originalForward;
                input.movementForward = -input.movementForward;
            }
            if (snapshot.invertSidewaysAxis) {
                boolean originalLeft = input.pressingLeft;
                input.pressingLeft = input.pressingRight;
                input.pressingRight = originalLeft;
                input.movementSideways = -input.movementSideways;
            }
        }

        float forwardScale = Math.max(1.0F, Math.abs(input.movementForward));
        float sidewaysScale = Math.max(1.0F, Math.abs(input.movementSideways));
        input.movementForward = movementValue(input.pressingForward, input.pressingBack) * forwardScale;
        input.movementSideways = movementValue(input.pressingLeft, input.pressingRight) * sidewaysScale;
    }

    /**
     * Returns true when sprinting should be blocked outright for the local player.
     */
    public static boolean shouldBlockSprint() {
        return snapshot.active && snapshot.profile == AfflictionProfile.STUNNED && snapshot.controlChaos;
    }

    /**
     * Computes the normalized sepuku animation progress for any entity currently tracked by the client.
     */
    public static float getSepukuProgress(LivingEntity entity) {
        SepukuAnimationState state = sepukuStates.get(entity.getUuid());
        if (state == null || state.durationTicks <= 0) {
            return 0.0F;
        }

        return 1.0F - (state.remainingTicks / (float) state.durationTicks);
    }

    /**
     * Applies a ritual self-strike pose to bipedal models while the synced sepuku countdown is active.
     */
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
        if (progress <= 0.0F) {
            return;
        }

        float drawPhase = Math.min(progress / 0.45F, 1.0F);
        float strikePhase = progress <= 0.45F ? 0.0F : Math.min((progress - 0.45F) / 0.55F, 1.0F);
        float easedStrike = strikePhase * strikePhase;

        // Pose notes for a future custom model layer: torso leans in, sword arm raises across the chest, then snaps down.
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

    /**
     * Decrements active sepuku animations and removes completed entries so tracked entities do not leak client memory.
     */
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

    /**
     * Spawns short-lived phantom Creepers around a frenzied player and advances their approach animation each tick.
     */
    private static void tickHallucinations(MinecraftClient client) {
        for (Hallucination hallucination : hallucinations) {
            hallucination.age++;
        }
        hallucinations.removeIf(hallucination -> hallucination.age >= hallucination.lifetimeTicks);

        if (!snapshot.active
                || snapshot.profile != AfflictionProfile.FRENZY
                || snapshot.barPercent >= 100.0F) {
            hallucinations.clear();
            return;
        }

        if (client.player.age < nextHallucinationTick) {
            return;
        }

        spawnHallucination(client);
        nextHallucinationTick = client.player.age + HALLUCINATION_COOLDOWN_TICKS + client.world.random.nextInt(12);
    }

    /**
     * Creates one fake Creeper approach vector and plays a local-only priming sound to sell the hallucination.
     */
    private static void spawnHallucination(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            return;
        }

        float angle = client.world.random.nextFloat() * MathHelper.TAU;
        double distance = 8.0D + client.world.random.nextDouble() * 6.0D;
        double startX = player.getX() + Math.cos(angle) * distance;
        double startZ = player.getZ() + Math.sin(angle) * distance;
        double startY = player.getY();
        float yaw = (float) Math.toDegrees(Math.atan2(player.getZ() - startZ, player.getX() - startX)) - 90.0F;

        hallucinations.add(new Hallucination(new Vec3d(startX, startY, startZ), yaw, HALLUCINATION_LIFETIME_TICKS));
        client.world.playSound(
                player,
                startX,
                startY,
                startZ,
                SoundEvents.ENTITY_CREEPER_PRIMED,
                SoundCategory.HOSTILE,
                0.22F,
                0.95F + client.world.random.nextFloat() * 0.15F
        );
    }

    /**
     * Renders all active phantom Creepers as pure client-side visuals without ever adding real entities to the world.
     */
    private static void renderHallucinations(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || hallucinations.isEmpty()) {
            return;
        }

        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        Vec3d cameraPos = context.camera().getPos();

        for (Hallucination hallucination : hallucinations) {
            float progress = hallucination.age / (float) hallucination.lifetimeTicks;
            Vec3d targetPos = client.player.getPos();
            Vec3d hallucinationPos = hallucination.startPos.lerp(targetPos, progress)
                    .add(0.0D, Math.sin(progress * Math.PI) * 0.15D, 0.0D);

            CreeperEntity fakeCreeper = new CreeperEntity(EntityType.CREEPER, client.world);
            fakeCreeper.setPosition(hallucinationPos);
            fakeCreeper.setYaw(hallucination.yaw);
            fakeCreeper.age = client.player.age;

            context.matrixStack().push();
            dispatcher.render(
                    fakeCreeper,
                    hallucinationPos.x - cameraPos.x,
                    hallucinationPos.y - cameraPos.y,
                    hallucinationPos.z - cameraPos.z,
                    hallucination.yaw,
                    0.0F,
                    context.matrixStack(),
                    context.consumers(),
                    LightmapTextureManager.MAX_LIGHT_COORDINATE
            );
            context.matrixStack().pop();
        }
    }

    /**
     * Converts a forward/back or left/right key pair into the signed movement value used by KeyboardInput.
     */
    private static float movementValue(boolean negative, boolean positive) {
        if (negative == positive) {
            return 0.0F;
        }
        return negative ? 1.0F : -1.0F;
    }

    /**
     * Local-only copy of the currently active affliction state for the local player.
     */
    private static final class ClientAfflictionSnapshot {
        private final boolean active;
        private final AfflictionProfile profile;
        private final float barPercent;
        private final boolean controlChaos;
        private final int[] movementMapping;
        private final boolean invertForwardAxis;
        private final boolean invertSidewaysAxis;

        private ClientAfflictionSnapshot(
                boolean active,
                AfflictionProfile profile,
                float barPercent,
                boolean controlChaos,
                int[] movementMapping,
                boolean invertForwardAxis,
                boolean invertSidewaysAxis
        ) {
            this.active = active;
            this.profile = profile;
            this.barPercent = barPercent;
            this.controlChaos = controlChaos;
            this.movementMapping = movementMapping;
            this.invertForwardAxis = invertForwardAxis;
            this.invertSidewaysAxis = invertSidewaysAxis;
        }

        /**
         * Rebuilds the local snapshot from the network payload while sanitizing malformed mapping indexes.
         */
        private static ClientAfflictionSnapshot fromPayload(WardenAfflictionSyncPayload payload) {
            if (!payload.active() || payload.profileOrdinal() < 0 || payload.profileOrdinal() >= AfflictionProfile.values().length) {
                return inactive();
            }

            int[] mapping = new int[4];
            byte[] payloadMapping = payload.movementMapping();
            for (int index = 0; index < mapping.length; index++) {
                byte mappedIndex = index < payloadMapping.length ? payloadMapping[index] : IDENTITY_MAPPING[index];
                mapping[index] = MathHelper.clamp(mappedIndex, 0, 3);
            }

            return new ClientAfflictionSnapshot(
                    true,
                    AfflictionProfile.values()[payload.profileOrdinal()],
                    payload.barPercent(),
                    payload.controlChaos(),
                    mapping,
                    payload.invertForwardAxis(),
                    payload.invertSidewaysAxis()
            );
        }

        /**
         * Returns the empty snapshot used whenever the server tells the client no affliction is active.
         */
        private static ClientAfflictionSnapshot inactive() {
            return new ClientAfflictionSnapshot(false, AfflictionProfile.BLEEDING, 0.0F, false, new int[]{0, 1, 2, 3}, false, false);
        }
    }

    /**
     * Tracks one fake Creeper approach path from its spawn point until the illusion fades out.
     */
    private static final class Hallucination {
        private final Vec3d startPos;
        private final float yaw;
        private final int lifetimeTicks;
        private int age;

        private Hallucination(Vec3d startPos, float yaw, int lifetimeTicks) {
            this.startPos = startPos;
            this.yaw = yaw;
            this.lifetimeTicks = lifetimeTicks;
        }
    }

    /**
     * Keeps the remaining tick count for one sepuku animation broadcast so renderers can interpolate a pose.
     */
    private static final class SepukuAnimationState {
        private final int durationTicks;
        private int remainingTicks;

        private SepukuAnimationState(int durationTicks, int remainingTicks) {
            this.durationTicks = durationTicks;
            this.remainingTicks = remainingTicks;
        }
    }
}
