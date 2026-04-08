package net.xmilon.himproveme.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.xmilon.himproveme.combat.DualWieldCombatHelper;
import org.jetbrains.annotations.Nullable;

public final class InspectAnimationHelper {
    private static boolean keyWasDown;
    @Nullable
    private static Hand activeHand;
    private static ItemStack activeStack = ItemStack.EMPTY;
    private static InspectAnimationPresets.InspectPreset activePreset = InspectAnimationPresets.defaultPreset();
    private static int cycleTick = -1;
    private static boolean looping;
    private static int loopFeedbackTicks;

    private InspectAnimationHelper() {
    }

    public static void tick(MinecraftClient client, KeyBinding keyBinding) {
        boolean keyDown = keyBinding.isPressed();

        if (client.player == null || client.world == null || client.currentScreen != null) {
            reset();
            keyWasDown = keyDown;
            return;
        }

        ClientPlayerEntity player = client.player;
        if (isActive()) {
            if (!canContinue(player)) {
                reset();
            } else {
                cycleTick++;

                if (looping) {
                    loopFeedbackTicks++;
                    if (shouldFire(loopFeedbackTicks, activePreset.orbitParticleIntervalTicks())) {
                        spawnLoopParticles(player);
                    }
                    if (shouldFire(loopFeedbackTicks, activePreset.lightLoopSoundIntervalTicks())) {
                        playSoundCue(player, activePreset.lightLoopSound());
                    }
                    if (shouldFire(loopFeedbackTicks, activePreset.heavyLoopSoundIntervalTicks())) {
                        playSoundCue(player, activePreset.heavyLoopSound());
                    }
                }

                int cycleLength = getCycleLength(looping);
                if (cycleTick >= cycleLength) {
                    if (keyDown) {
                        cycleTick = 0;
                        if (!looping) {
                            looping = true;
                            loopFeedbackTicks = 0;
                            playSoundCue(player, activePreset.heavyLoopSound());
                        }
                    } else {
                        reset();
                    }
                }
            }
        }

        if (!isActive() && keyDown && !keyWasDown) {
            start(player);
        }

        keyWasDown = keyDown;
    }

    public static boolean isInspectable(ItemStack stack) {
        return InspectAnimationPresets.isInspectable(stack);
    }

    public static boolean isActive(LivingEntity entity, Hand hand) {
        return isActive()
                && MinecraftClient.getInstance().player == entity
                && activeHand == hand;
    }

    public static InspectAnimation getAnimation(LivingEntity entity, Hand hand, float tickDelta) {
        if (!isActive(entity, hand)) {
            return InspectAnimation.NONE;
        }

        float cycleLength = getCycleLength(looping);
        float progress = MathHelper.clamp((cycleTick + tickDelta) / cycleLength, 0.0F, 1.0F);
        float cycleAngle = progress * MathHelper.PI * 2.0F;
        float loopTime = loopFeedbackTicks + tickDelta;
        float lift = looping
                ? 0.72F + 0.14F * MathHelper.sin(cycleAngle)
                : 0.12F + 0.88F * MathHelper.sin(progress * MathHelper.PI);
        float sway = MathHelper.sin(cycleAngle);
        float snap = MathHelper.sin(cycleAngle * (looping ? 2.0F : 1.0F));
        float blur = looping ? 1.0F : 0.22F + 0.12F * MathHelper.sin(progress * MathHelper.PI);
        float spinDegrees = activePreset.spinTurn().degrees() * progress;
        float drift = looping ? MathHelper.sin(loopTime * 0.18F) : 0.0F;
        float flourish = looping ? MathHelper.cos(loopTime * 0.11F + 0.9F) : 0.0F;
        return new InspectAnimation(true, activePreset, looping, progress, lift, sway, snap, blur, drift, flourish, spinDegrees);
    }

    private static boolean isActive() {
        return activeHand != null && cycleTick >= 0;
    }

    private static void start(ClientPlayerEntity player) {
        Hand hand = findInspectableHand(player);
        if (hand == null) {
            return;
        }

        ItemStack stack = player.getStackInHand(hand);
        activeHand = hand;
        activeStack = stack.copy();
        activePreset = InspectAnimationPresets.resolve(stack);
        cycleTick = 0;
        looping = false;
        loopFeedbackTicks = 0;
        playSoundCue(player, activePreset.startSound());
    }

    private static boolean canContinue(ClientPlayerEntity player) {
        if (activeHand == null || player.isSpectator() || player.isUsingItem()) {
            return false;
        }

        if (player.getHandSwingProgress(0.0F) > 0.0F) {
            return false;
        }

        if (activeHand == Hand.OFF_HAND && DualWieldCombatHelper.isOffhandSwingActive(player)) {
            return false;
        }

        ItemStack currentStack = player.getStackInHand(activeHand);
        return isInspectable(currentStack) && ItemStack.areItemsAndComponentsEqual(currentStack, activeStack);
    }

    @Nullable
    private static Hand findInspectableHand(ClientPlayerEntity player) {
        if (canInspect(player, Hand.MAIN_HAND)) {
            return Hand.MAIN_HAND;
        }

        if (canInspect(player, Hand.OFF_HAND)) {
            return Hand.OFF_HAND;
        }

        return null;
    }

    private static boolean canInspect(ClientPlayerEntity player, Hand hand) {
        return !player.isUsingItem()
                && isInspectable(player.getStackInHand(hand))
                && (hand != Hand.OFF_HAND || !DualWieldCombatHelper.isOffhandSwingActive(player));
    }

    private static int getCycleLength(boolean currentLoopingState) {
        return currentLoopingState ? activePreset.loopSpinTicks() : activePreset.tapSpinTicks();
    }

    private static void spawnLoopParticles(ClientPlayerEntity player) {
        if (activeHand == null || activePreset.orbitParticle() == null) {
            return;
        }

        Vec3d forward = player.getRotationVec(1.0F).normalize();
        Vec3d right = forward.crossProduct(new Vec3d(0.0D, 1.0D, 0.0D));
        if (right.lengthSquared() < 1.0E-4D) {
            right = new Vec3d(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }

        int direction = player.getMainArm() == Arm.RIGHT ? 1 : -1;
        if (activeHand == Hand.OFF_HAND) {
            direction *= -1;
        }

        Vec3d up = new Vec3d(0.0D, 1.0D, 0.0D);
        Vec3d center = player.getEyePos()
                .add(forward.multiply(0.56D))
                .add(right.multiply(0.34D * direction))
                .add(up.multiply(-0.34D));

        double angle = loopFeedbackTicks * 0.82D;
        Vec3d orbit = right.multiply(Math.cos(angle) * activePreset.particleHorizontalRadius())
                .add(up.multiply(Math.sin(angle) * activePreset.particleVerticalRadius()));
        Vec3d orbitPos = center.add(orbit);
        Vec3d orbitVelocity = orbit.multiply(0.25D).add(forward.multiply(0.02D));

        player.getWorld().addParticle(
                activePreset.orbitParticle(),
                orbitPos.x,
                orbitPos.y,
                orbitPos.z,
                orbitVelocity.x,
                orbitVelocity.y,
                orbitVelocity.z
        );

        if (activePreset.accentParticle() != null && shouldFire(loopFeedbackTicks, activePreset.accentParticleIntervalTicks())) {
            player.getWorld().addParticle(activePreset.accentParticle(), center.x, center.y, center.z, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void playSoundCue(ClientPlayerEntity player, @Nullable InspectAnimationPresets.SoundCue cue) {
        if (cue == null) {
            return;
        }

        float randomizedPitch = cue.basePitch();
        if (cue.pitchVariance() > 0.0F) {
            randomizedPitch += player.getWorld().random.nextFloat() * cue.pitchVariance();
        }
        player.playSound(cue.event(), cue.volume(), randomizedPitch);
    }

    private static boolean shouldFire(int tick, int interval) {
        return interval > 0 && tick % interval == 0;
    }

    private static void reset() {
        activeHand = null;
        activeStack = ItemStack.EMPTY;
        activePreset = InspectAnimationPresets.defaultPreset();
        cycleTick = -1;
        looping = false;
        loopFeedbackTicks = 0;
    }

    public record InspectAnimation(
            boolean active,
            InspectAnimationPresets.InspectPreset preset,
            boolean looping,
            float progress,
            float lift,
            float sway,
            float snap,
            float blur,
            float drift,
            float flourish,
            float spinDegrees
    ) {
        public static final InspectAnimation NONE = new InspectAnimation(
                false,
                InspectAnimationPresets.defaultPreset(),
                false,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                0.0F
        );

        public InspectAnimationPresets.MotionStyle style() {
            return preset.motionStyle();
        }
    }
}
