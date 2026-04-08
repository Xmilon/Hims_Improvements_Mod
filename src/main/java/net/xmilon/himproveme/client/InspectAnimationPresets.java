package net.xmilon.himproveme.client;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.xmilon.himproveme.item.ModItem;
import net.xmilon.himproveme.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InspectAnimationPresets {
    private static final Map<Item, InspectPreset> ITEM_PRESETS = new LinkedHashMap<>();
    private static final List<TagFallback> TAG_FALLBACKS = new ArrayList<>();
    private static final InspectPreset DEFAULT_PRESET = createHandleRollPreset();

    static {
        registerDefaults();
    }

    private InspectAnimationPresets() {
    }

    public static boolean isInspectable(ItemStack stack) {
        return !stack.isEmpty()
                && (ITEM_PRESETS.containsKey(stack.getItem()) || stack.isIn(ModTags.Items.INSPECT_SPINNABLE));
    }

    public static InspectPreset resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return DEFAULT_PRESET;
        }

        InspectPreset explicitPreset = ITEM_PRESETS.get(stack.getItem());
        if (explicitPreset != null) {
            return explicitPreset;
        }

        for (TagFallback fallback : TAG_FALLBACKS) {
            if (stack.isIn(fallback.tag())) {
                return fallback.preset();
            }
        }

        return DEFAULT_PRESET;
    }

    public static InspectPreset defaultPreset() {
        return DEFAULT_PRESET;
    }

    private static void registerDefaults() {
        // Explicit item mappings live here.
        // Add brand-new inspectable items in this block and they will work even if they are not in a tag yet.
        registerItem(ModItem.BLOWGUN, createBlowgunBambooPreset());
        registerItem(ModItem.BREEZE_STAFF, createBreezeStaffPreset());
        registerItem(ModItem.ENDER_STAFF, createEnderStaffPreset());
        registerItem(ModItem.MEGA_ROCKET, createMegaRocketPreset());

        // Tag fallbacks stay useful for broad item families that already share the same feel.
        registerTagFallback(ModTags.Items.INSPECT_HALF_SPIN, createHalfRollPreset());
        registerTagFallback(ModTags.Items.INSPECT_KARAMBIT_SPIN, createKarambitPreset());
        registerTagFallback(ModTags.Items.INSPECT_Y_SPIN, createYTwirlPreset());
        registerTagFallback(ModTags.Items.INSPECT_HEAVY_SPIN, createHeavyArcPreset());
        registerTagFallback(ModTags.Items.INSPECT_STAFF_SPIN, createStaffFlipPreset());
    }

    private static void registerItem(Item item, InspectPreset preset) {
        ITEM_PRESETS.put(item, preset);
    }

    private static void registerTagFallback(TagKey<Item> tag, InspectPreset preset) {
        TAG_FALLBACKS.add(new TagFallback(tag, preset));
    }

    private static InspectPreset createHandleRollPreset() {
        return InspectPreset.builder("handle_roll")
                .motionStyle(MotionStyle.HANDLE_ROLL)
                .spinTurn(SpinTurn.FULL)
                .tapSpinTicks(14)
                .loopSpinTicks(8)
                .orbitParticle(ParticleTypes.CLOUD)
                .accentParticle(ParticleTypes.SWEEP_ATTACK)
                .orbitParticleIntervalTicks(3)
                .accentParticleIntervalTicks(9)
                .lightLoopSoundIntervalTicks(6)
                .heavyLoopSoundIntervalTicks(12)
                .particleHorizontalRadius(0.10D)
                .particleVerticalRadius(0.22D)
                .startSound(cue(SoundEvents.ITEM_TRIDENT_THROW.value(), 0.20F, 1.34F, 0.06F))
                .lightLoopSound(cue(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.14F, 0.96F, 0.16F))
                .heavyLoopSound(cue(SoundEvents.ITEM_TRIDENT_RIPTIDE_1.value(), 0.10F, 1.35F, 0.12F))
                .build();
    }

    private static InspectPreset createHalfRollPreset() {
        return InspectPreset.builder("half_roll")
                .motionStyle(MotionStyle.HALF_ROLL)
                .spinTurn(SpinTurn.HALF)
                .tapSpinTicks(7)
                .loopSpinTicks(4)
                .orbitParticle(ParticleTypes.CLOUD)
                .accentParticle(ParticleTypes.SWEEP_ATTACK)
                .orbitParticleIntervalTicks(3)
                .accentParticleIntervalTicks(9)
                .lightLoopSoundIntervalTicks(6)
                .heavyLoopSoundIntervalTicks(12)
                .particleHorizontalRadius(0.16D)
                .particleVerticalRadius(0.18D)
                .startSound(cue(SoundEvents.ITEM_TRIDENT_THROW.value(), 0.20F, 1.22F, 0.06F))
                .lightLoopSound(cue(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.14F, 1.00F, 0.16F))
                .heavyLoopSound(cue(SoundEvents.ITEM_TRIDENT_RIPTIDE_1.value(), 0.10F, 1.28F, 0.12F))
                .build();
    }

    private static InspectPreset createKarambitPreset() {
        return InspectPreset.builder("karambit_ring")
                .motionStyle(MotionStyle.KARAMBIT_RING)
                .spinTurn(SpinTurn.FULL)
                .tapSpinTicks(14)
                .loopSpinTicks(8)
                .orbitParticle(ParticleTypes.CLOUD)
                .accentParticle(ParticleTypes.SWEEP_ATTACK)
                .orbitParticleIntervalTicks(3)
                .accentParticleIntervalTicks(6)
                .lightLoopSoundIntervalTicks(6)
                .heavyLoopSoundIntervalTicks(12)
                .particleHorizontalRadius(0.09D)
                .particleVerticalRadius(0.03D)
                .startSound(cue(SoundEvents.ITEM_TRIDENT_THROW.value(), 0.18F, 1.62F, 0.06F))
                .lightLoopSound(cue(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.13F, 1.08F, 0.14F))
                .heavyLoopSound(cue(SoundEvents.ITEM_TRIDENT_RIPTIDE_1.value(), 0.08F, 1.44F, 0.10F))
                .build();
    }

    private static InspectPreset createYTwirlPreset() {
        return InspectPreset.builder("y_twirl")
                .motionStyle(MotionStyle.Y_TWIRL)
                .spinTurn(SpinTurn.FULL)
                .tapSpinTicks(14)
                .loopSpinTicks(8)
                .orbitParticle(ParticleTypes.CLOUD)
                .accentParticle(ParticleTypes.SWEEP_ATTACK)
                .orbitParticleIntervalTicks(3)
                .accentParticleIntervalTicks(9)
                .lightLoopSoundIntervalTicks(6)
                .heavyLoopSoundIntervalTicks(12)
                .particleHorizontalRadius(0.20D)
                .particleVerticalRadius(0.08D)
                .startSound(cue(SoundEvents.ITEM_TRIDENT_THROW.value(), 0.20F, 1.50F, 0.06F))
                .lightLoopSound(cue(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.14F, 1.04F, 0.16F))
                .heavyLoopSound(cue(SoundEvents.ITEM_TRIDENT_RIPTIDE_1.value(), 0.10F, 1.42F, 0.12F))
                .build();
    }

    private static InspectPreset createHeavyArcPreset() {
        return InspectPreset.builder("heavy_arc")
                .motionStyle(MotionStyle.HEAVY_ARC)
                .spinTurn(SpinTurn.FULL)
                .tapSpinTicks(14)
                .loopSpinTicks(8)
                .orbitParticle(ParticleTypes.CLOUD)
                .accentParticle(ParticleTypes.SWEEP_ATTACK)
                .orbitParticleIntervalTicks(3)
                .accentParticleIntervalTicks(9)
                .lightLoopSoundIntervalTicks(6)
                .heavyLoopSoundIntervalTicks(12)
                .particleHorizontalRadius(0.18D)
                .particleVerticalRadius(0.24D)
                .startSound(cue(SoundEvents.ITEM_TRIDENT_THROW.value(), 0.20F, 1.08F, 0.06F))
                .lightLoopSound(cue(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.15F, 0.92F, 0.14F))
                .heavyLoopSound(cue(SoundEvents.ITEM_TRIDENT_RIPTIDE_1.value(), 0.11F, 1.28F, 0.10F))
                .build();
    }

    private static InspectPreset createStaffFlipPreset() {
        return InspectPreset.builder("staff_flip")
                .motionStyle(MotionStyle.STAFF_FLIP)
                .spinTurn(SpinTurn.FULL)
                .tapSpinTicks(14)
                .loopSpinTicks(8)
                .orbitParticle(ParticleTypes.CLOUD)
                .accentParticle(ParticleTypes.SWEEP_ATTACK)
                .orbitParticleIntervalTicks(3)
                .accentParticleIntervalTicks(9)
                .lightLoopSoundIntervalTicks(6)
                .heavyLoopSoundIntervalTicks(12)
                .particleHorizontalRadius(0.12D)
                .particleVerticalRadius(0.28D)
                .startSound(cue(SoundEvents.ITEM_TRIDENT_THROW.value(), 0.20F, 1.26F, 0.06F))
                .lightLoopSound(cue(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.14F, 0.94F, 0.16F))
                .heavyLoopSound(cue(SoundEvents.ITEM_TRIDENT_RIPTIDE_1.value(), 0.10F, 1.34F, 0.12F))
                .build();
    }

    private static InspectPreset createBlowgunBambooPreset() {
        return InspectPreset.builder("blowgun_bamboo_flow")
                .motionStyle(MotionStyle.BAMBOO_FLOW) // Dedicated tube-handling motion so the blowgun stops feeling like a staff.
                .spinTurn(SpinTurn.FULL) // A full roll works well for a long hollow tube.
                .tapSpinTicks(18) // Slower opening flourish so the bamboo shape reads clearly.
                .loopSpinTicks(12) // Slightly slower loop keeps the inspect calm and deliberate.
                .orbitParticle(ParticleTypes.CLOUD) // Soft air wisps sell the hollow-bamboo breath vibe.
                .accentParticle(null) // No sword-swipe flash; it looked too metallic and too aggressive.
                .orbitParticleIntervalTicks(4) // Fewer particles keeps the inspect subtle.
                .accentParticleIntervalTicks(0) // Zero disables the accent particle entirely.
                .lightLoopSoundIntervalTicks(8) // Spread the soft bamboo taps out so the inspect breathes.
                .heavyLoopSoundIntervalTicks(16) // A deeper knock should only land every now and then.
                .particleHorizontalRadius(0.07D) // Tight orbit because the blowgun is slim.
                .particleVerticalRadius(0.10D) // Small vertical drift instead of a wide pole flourish.
                .startSound(cue(SoundEvents.BLOCK_BAMBOO_WOOD_HIT, 0.20F, 1.12F, 0.05F)) // Dry bamboo clack when the inspect starts.
                .lightLoopSound(cue(SoundEvents.BLOCK_BAMBOO_HIT, 0.12F, 1.20F, 0.06F)) // Light rolling taps for the loop body.
                .heavyLoopSound(cue(SoundEvents.BLOCK_BAMBOO_WOOD_HIT, 0.16F, 0.96F, 0.05F)) // A deeper knock punctuates the longer loop.
                .build();
    }

    private static InspectPreset createBreezeStaffPreset() {
        return InspectPreset.builder("breeze_staff_gale")
                .motionStyle(MotionStyle.STAFF_FLIP)
                .spinTurn(SpinTurn.FULL)
                .tapSpinTicks(14)
                .loopSpinTicks(8)
                .orbitParticle(ParticleTypes.CLOUD)
                .accentParticle(null)
                .orbitParticleIntervalTicks(3)
                .accentParticleIntervalTicks(0)
                .lightLoopSoundIntervalTicks(7)
                .heavyLoopSoundIntervalTicks(14)
                .particleHorizontalRadius(0.14D)
                .particleVerticalRadius(0.30D)
                .startSound(cue(SoundEvents.ENTITY_BREEZE_INHALE, 0.22F, 1.08F, 0.05F))
                .lightLoopSound(cue(SoundEvents.ENTITY_BREEZE_IDLE_AIR, 0.14F, 1.16F, 0.08F))
                .heavyLoopSound(cue(SoundEvents.ENTITY_BREEZE_WHIRL, 0.10F, 1.04F, 0.06F))
                .build();
    }

    private static InspectPreset createEnderStaffPreset() {
        return InspectPreset.builder("ender_staff_void_arc")
                .motionStyle(MotionStyle.Y_TWIRL)
                .spinTurn(SpinTurn.FULL)
                .tapSpinTicks(16)
                .loopSpinTicks(9)
                .orbitParticle(ParticleTypes.PORTAL)
                .accentParticle(null)
                .orbitParticleIntervalTicks(3)
                .accentParticleIntervalTicks(0)
                .lightLoopSoundIntervalTicks(8)
                .heavyLoopSoundIntervalTicks(16)
                .particleHorizontalRadius(0.12D)
                .particleVerticalRadius(0.12D)
                .startSound(cue(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 0.20F, 1.10F, 0.04F))
                .lightLoopSound(cue(SoundEvents.ENTITY_ENDERMAN_AMBIENT, 0.10F, 1.30F, 0.05F))
                .heavyLoopSound(cue(SoundEvents.ENTITY_ENDERMAN_STARE, 0.06F, 1.08F, 0.04F))
                .build();
    }

    private static InspectPreset createMegaRocketPreset() {
        return InspectPreset.builder("mega_rocket_showboat")
                .motionStyle(MotionStyle.HEAVY_ARC) // The rocket should feel weighty, not knife-like.
                .spinTurn(SpinTurn.FULL) // Full arc keeps the inspect flashy enough for an explosive utility item.
                .tapSpinTicks(16) // Slightly longer wind-up gives the rocket some heft.
                .loopSpinTicks(10) // The loop is still brisk, but heavier than a sword twirl.
                .orbitParticle(ParticleTypes.FLAME) // Tiny flame hints make the rocket feel primed.
                .accentParticle(ParticleTypes.SMOKE) // Smoke puffs ground the effect without becoming a full explosion.
                .orbitParticleIntervalTicks(4) // Keep the fire sparse so it stays readable in first person.
                .accentParticleIntervalTicks(12) // Occasional smoke beats are enough.
                .lightLoopSoundIntervalTicks(8) // Small fizz sound cadence.
                .heavyLoopSoundIntervalTicks(16) // Save the larger rocket flourish for bigger beats.
                .particleHorizontalRadius(0.11D) // Medium orbit feels like presenting the rocket instead of spinning a blade.
                .particleVerticalRadius(0.20D) // Slight lift sells the rocket body swinging through an arc.
                .startSound(cue(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.18F, 1.12F, 0.05F)) // Initial ignition pop.
                .lightLoopSound(cue(SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT, 0.12F, 1.18F, 0.06F)) // Light crackle while looping.
                .heavyLoopSound(cue(SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.10F, 1.08F, 0.04F)) // Decorative sparkle for the strong beat.
                .build();
    }

    private static SoundCue cue(SoundEvent event, float volume, float basePitch, float pitchVariance) {
        return new SoundCue(event, volume, basePitch, pitchVariance);
    }

    public enum MotionStyle {
        HANDLE_ROLL,
        HALF_ROLL,
        Y_TWIRL,
        KARAMBIT_RING,
        HEAVY_ARC,
        STAFF_FLIP,
        BAMBOO_FLOW
    }

    public enum SpinTurn {
        FULL(360.0F),
        HALF(180.0F);

        private final float degrees;

        SpinTurn(float degrees) {
            this.degrees = degrees;
        }

        public float degrees() {
            return degrees;
        }
    }

    public static final class SoundCue {
        private final SoundEvent event;
        private final float volume;
        private final float basePitch;
        private final float pitchVariance;

        private SoundCue(SoundEvent event, float volume, float basePitch, float pitchVariance) {
            this.event = event;
            this.volume = volume;
            this.basePitch = basePitch;
            this.pitchVariance = pitchVariance;
        }

        public SoundEvent event() {
            return event;
        }

        public float volume() {
            return volume;
        }

        public float basePitch() {
            return basePitch;
        }

        public float pitchVariance() {
            return pitchVariance;
        }
    }

    public static final class InspectPreset {
        private final String debugName;
        private final MotionStyle motionStyle;
        private final SpinTurn spinTurn;
        private final int tapSpinTicks;
        private final int loopSpinTicks;
        @Nullable
        private final ParticleEffect orbitParticle;
        @Nullable
        private final ParticleEffect accentParticle;
        private final int orbitParticleIntervalTicks;
        private final int accentParticleIntervalTicks;
        private final int lightLoopSoundIntervalTicks;
        private final int heavyLoopSoundIntervalTicks;
        private final double particleHorizontalRadius;
        private final double particleVerticalRadius;
        @Nullable
        private final SoundCue startSound;
        @Nullable
        private final SoundCue lightLoopSound;
        @Nullable
        private final SoundCue heavyLoopSound;

        private InspectPreset(Builder builder) {
            this.debugName = builder.debugName;
            this.motionStyle = builder.motionStyle;
            this.spinTurn = builder.spinTurn;
            this.tapSpinTicks = builder.tapSpinTicks;
            this.loopSpinTicks = builder.loopSpinTicks;
            this.orbitParticle = builder.orbitParticle;
            this.accentParticle = builder.accentParticle;
            this.orbitParticleIntervalTicks = builder.orbitParticleIntervalTicks;
            this.accentParticleIntervalTicks = builder.accentParticleIntervalTicks;
            this.lightLoopSoundIntervalTicks = builder.lightLoopSoundIntervalTicks;
            this.heavyLoopSoundIntervalTicks = builder.heavyLoopSoundIntervalTicks;
            this.particleHorizontalRadius = builder.particleHorizontalRadius;
            this.particleVerticalRadius = builder.particleVerticalRadius;
            this.startSound = builder.startSound;
            this.lightLoopSound = builder.lightLoopSound;
            this.heavyLoopSound = builder.heavyLoopSound;
        }

        public static Builder builder(String debugName) {
            return new Builder(debugName);
        }

        public String debugName() {
            return debugName;
        }

        public MotionStyle motionStyle() {
            return motionStyle;
        }

        public SpinTurn spinTurn() {
            return spinTurn;
        }

        public int tapSpinTicks() {
            return tapSpinTicks;
        }

        public int loopSpinTicks() {
            return loopSpinTicks;
        }

        @Nullable
        public ParticleEffect orbitParticle() {
            return orbitParticle;
        }

        @Nullable
        public ParticleEffect accentParticle() {
            return accentParticle;
        }

        public int orbitParticleIntervalTicks() {
            return orbitParticleIntervalTicks;
        }

        public int accentParticleIntervalTicks() {
            return accentParticleIntervalTicks;
        }

        public int lightLoopSoundIntervalTicks() {
            return lightLoopSoundIntervalTicks;
        }

        public int heavyLoopSoundIntervalTicks() {
            return heavyLoopSoundIntervalTicks;
        }

        public double particleHorizontalRadius() {
            return particleHorizontalRadius;
        }

        public double particleVerticalRadius() {
            return particleVerticalRadius;
        }

        @Nullable
        public SoundCue startSound() {
            return startSound;
        }

        @Nullable
        public SoundCue lightLoopSound() {
            return lightLoopSound;
        }

        @Nullable
        public SoundCue heavyLoopSound() {
            return heavyLoopSound;
        }

        public static final class Builder {
            // Debug name used to identify the preset quickly when you revisit this file later.
            private final String debugName;

            // Main renderer pose family used by the first-person, third-person, and arm-pose mixins.
            private MotionStyle motionStyle = MotionStyle.HANDLE_ROLL;

            // Total rotation produced by one inspect cycle.
            private SpinTurn spinTurn = SpinTurn.FULL;

            // Length of the one-shot inspect flourish before the loop can begin.
            private int tapSpinTicks = 14;

            // Length of one continuous loop while the inspect key is held.
            private int loopSpinTicks = 8;

            // Particle orbit that circles around the item during the loop.
            @Nullable
            private ParticleEffect orbitParticle = ParticleTypes.CLOUD;

            // Optional extra particle that fires from the center of the inspect.
            @Nullable
            private ParticleEffect accentParticle = ParticleTypes.SWEEP_ATTACK;

            // Tick cadence for the orbit particle.
            private int orbitParticleIntervalTicks = 3;

            // Tick cadence for the center accent particle. Set to zero to disable it.
            private int accentParticleIntervalTicks = 9;

            // Tick cadence for the lighter loop sound layer.
            private int lightLoopSoundIntervalTicks = 6;

            // Tick cadence for the heavier punctuation sound layer.
            private int heavyLoopSoundIntervalTicks = 12;

            // Horizontal orbit radius used for loop particles.
            private double particleHorizontalRadius = 0.10D;

            // Vertical orbit radius used for loop particles.
            private double particleVerticalRadius = 0.22D;

            // Sound played when the inspect begins.
            @Nullable
            private SoundCue startSound;

            // Softer repeating sound while the inspect loops.
            @Nullable
            private SoundCue lightLoopSound;

            // Stronger repeating sound while the inspect loops.
            @Nullable
            private SoundCue heavyLoopSound;

            private Builder(String debugName) {
                this.debugName = debugName;
            }

            /**
             * Chooses the movement family applied by the render mixins.
             * Add a new enum value plus matching renderer math when you want a totally new motion language.
             */
            public Builder motionStyle(MotionStyle motionStyle) {
                this.motionStyle = motionStyle;
                return this;
            }

            /**
             * Controls whether the inspect completes a full 360-degree roll or a smaller 180-degree half-roll.
             */
            public Builder spinTurn(SpinTurn spinTurn) {
                this.spinTurn = spinTurn;
                return this;
            }

            /**
             * Sets the number of ticks used by the initial non-looping flourish.
             */
            public Builder tapSpinTicks(int tapSpinTicks) {
                this.tapSpinTicks = tapSpinTicks;
                return this;
            }

            /**
             * Sets the number of ticks used by one looping cycle after the flourish finishes.
             */
            public Builder loopSpinTicks(int loopSpinTicks) {
                this.loopSpinTicks = loopSpinTicks;
                return this;
            }

            /**
             * Sets the particle that orbits around the item while the inspect loops.
             * Use {@code null} if the preset should not emit an orbit particle.
             */
            public Builder orbitParticle(@Nullable ParticleEffect orbitParticle) {
                this.orbitParticle = orbitParticle;
                return this;
            }

            /**
             * Sets the accent particle spawned from the center of the inspect.
             * Use {@code null} if the preset should not emit a center accent.
             */
            public Builder accentParticle(@Nullable ParticleEffect accentParticle) {
                this.accentParticle = accentParticle;
                return this;
            }

            /**
             * Controls how often the orbit particle is emitted during the loop.
             */
            public Builder orbitParticleIntervalTicks(int orbitParticleIntervalTicks) {
                this.orbitParticleIntervalTicks = orbitParticleIntervalTicks;
                return this;
            }

            /**
             * Controls how often the accent particle is emitted during the loop.
             * Set to zero when the accent particle should stay disabled.
             */
            public Builder accentParticleIntervalTicks(int accentParticleIntervalTicks) {
                this.accentParticleIntervalTicks = accentParticleIntervalTicks;
                return this;
            }

            /**
             * Controls how often the light loop sound is played.
             */
            public Builder lightLoopSoundIntervalTicks(int lightLoopSoundIntervalTicks) {
                this.lightLoopSoundIntervalTicks = lightLoopSoundIntervalTicks;
                return this;
            }

            /**
             * Controls how often the heavy loop sound is played.
             */
            public Builder heavyLoopSoundIntervalTicks(int heavyLoopSoundIntervalTicks) {
                this.heavyLoopSoundIntervalTicks = heavyLoopSoundIntervalTicks;
                return this;
            }

            /**
             * Horizontal radius used by the orbit particle ring.
             */
            public Builder particleHorizontalRadius(double particleHorizontalRadius) {
                this.particleHorizontalRadius = particleHorizontalRadius;
                return this;
            }

            /**
             * Vertical radius used by the orbit particle ring.
             */
            public Builder particleVerticalRadius(double particleVerticalRadius) {
                this.particleVerticalRadius = particleVerticalRadius;
                return this;
            }

            /**
             * Sound played once when the inspect begins.
             */
            public Builder startSound(@Nullable SoundCue startSound) {
                this.startSound = startSound;
                return this;
            }

            /**
             * Softer loop sound that fills the space between heavier accents.
             */
            public Builder lightLoopSound(@Nullable SoundCue lightLoopSound) {
                this.lightLoopSound = lightLoopSound;
                return this;
            }

            /**
             * Heavier loop sound used to punctuate the inspect.
             */
            public Builder heavyLoopSound(@Nullable SoundCue heavyLoopSound) {
                this.heavyLoopSound = heavyLoopSound;
                return this;
            }

            public InspectPreset build() {
                return new InspectPreset(this);
            }
        }
    }

    private record TagFallback(TagKey<Item> tag, InspectPreset preset) {
    }
}
