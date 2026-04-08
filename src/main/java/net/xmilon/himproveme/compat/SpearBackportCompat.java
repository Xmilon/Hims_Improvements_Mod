package net.xmilon.himproveme.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.item.ModItem;
import net.xmilon.himproveme.item.ModToolMaterials;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public final class SpearBackportCompat {
    private static final String SPEARS_MOD_ID = "spears";
    private static final Identifier ENDER_SPEAR_ITEM_ID = Identifier.of(HimProveMe.MOD_ID, "ender_spear");
    private static final Identifier DOUBLE_ENDER_SPEAR_ITEM_ID = Identifier.of(HimProveMe.MOD_ID, "double_ender_spear");

    private static final Identifier USE_EFFECTS_COMPONENT_ID = Identifier.of(SPEARS_MOD_ID, "use_effects");
    private static final Identifier ATTACK_RANGE_COMPONENT_ID = Identifier.of(SPEARS_MOD_ID, "attack_range");
    private static final Identifier KINETIC_WEAPON_COMPONENT_ID = Identifier.of(SPEARS_MOD_ID, "kinetic_weapon");
    private static final Identifier SWING_ANIMATION_COMPONENT_ID = Identifier.of(SPEARS_MOD_ID, "swing_animation");
    private static final Identifier PIERCING_WEAPON_COMPONENT_ID = Identifier.of(SPEARS_MOD_ID, "piercing_weapon");
    private static final Identifier MINIMUM_ATTACK_CHARGE_COMPONENT_ID = Identifier.of(SPEARS_MOD_ID, "minimum_attack_charge");

    private static final float ENDER_SPEAR_SWING_SECONDS = 1.2F;
    private static final float DOUBLE_ENDER_SPEAR_SWING_SECONDS = 1.2F;
    private static final float ENDER_SPEAR_CHARGE_DAMAGE_MULTIPLIER = 1.25F;
    private static final float ENDER_SPEAR_CHARGE_DELAY_SECONDS = 0.3F;
    private static final float ENDER_SPEAR_MAX_DURATION_FOR_DISMOUNT_SECONDS = 2.0F;
    private static final float ENDER_SPEAR_MIN_SPEED_FOR_DISMOUNT = 6.5F;
    private static final float ENDER_SPEAR_MAX_DURATION_FOR_CHARGE_KNOCKBACK_SECONDS = 3.0F;
    private static final float ENDER_SPEAR_MIN_SPEED_FOR_CHARGE_KNOCKBACK = 5.1F;
    private static final float ENDER_SPEAR_MAX_DURATION_FOR_CHARGE_DAMAGE_SECONDS = 8.75F;
    private static final float ENDER_SPEAR_MIN_RELATIVE_SPEED_FOR_CHARGE_DAMAGE = 4.6F;
    private static final float USE_EFFECTS_STRAFE_SPEED = 1.0F;
    private static final boolean USE_EFFECTS_ALLOW_SPRINTING = true;
    private static final boolean USE_EFFECTS_INTERACT_VIBRATIONS = false;
    private static final float ATTACK_RANGE_MIN = 2.0F;
    private static final float ATTACK_RANGE_MAX = 4.5F;
    private static final int SWING_TICKS = 24;
    private static final float PIERCING_HITBOX_MARGIN = 0.25F;
    private static final float KINETIC_HITBOX_MARGIN = 0.125F;
    private static final int KINETIC_CONTACT_COOLDOWN_TICKS = 10;
    private static final float KINETIC_FORWARD_MOVEMENT = 0.38F;
    private static final float MINIMUM_ATTACK_CHARGE = 1.0F;

    private static ComponentType<?> useEffectsComponent;
    private static ComponentType<?> attackRangeComponent;
    private static ComponentType<?> kineticWeaponComponent;
    private static ComponentType<?> swingAnimationComponent;
    private static ComponentType<?> piercingWeaponComponent;
    private static ComponentType<?> minimumAttackChargeComponent;
    private static boolean componentLookupAttempted;

    private static Method piercingStabMethod;
    private static Method attackRangeMaxReachMethod;
    private static Method registerSpearRawMethod;
    private static boolean piercingReflectionLookupAttempted;
    private static boolean attackRangeReflectionLookupAttempted;
    private static boolean spearFactoryLookupAttempted;

    private static Constructor<?> useEffectsConstructor;
    private static Constructor<?> attackRangeConstructor;
    private static Constructor<?> kineticWeaponConstructor;
    private static Constructor<?> kineticWeaponConditionConstructor;
    private static Constructor<?> swingAnimationConstructor;
    private static Constructor<?> piercingWeaponConstructor;
    private static Method spearsConfigMethod;
    private static boolean componentConstructionLookupAttempted;
    private static boolean configLookupAttempted;

    private SpearBackportCompat() {
    }

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(SPEARS_MOD_ID);
    }

    public static Item registerEnderSpearItem() {
        if (!isLoaded()) {
            return null;
        }

        Item enderSpear = registerFactoryEnderSpear();
        registerDoubleEnderSpearItem();
        return enderSpear;
    }

    public static Item registerDoubleEnderSpearItem() {
        if (!isLoaded()) {
            return null;
        }

        if (Registries.ITEM.containsId(DOUBLE_ENDER_SPEAR_ITEM_ID)) {
            ModItem.DOUBLE_ENDER_SPEAR = Registries.ITEM.get(DOUBLE_ENDER_SPEAR_ITEM_ID);
            return ModItem.DOUBLE_ENDER_SPEAR;
        }

        Method factoryMethod = getRegisterSpearRawMethod();
        if (factoryMethod == null) {
            return null;
        }

        try {
            Object result = factoryMethod.invoke(
                    null,
                    ModToolMaterials.ENDER_INGOT,
                    ENDER_SPEAR_SWING_SECONDS,
                    ENDER_SPEAR_CHARGE_DAMAGE_MULTIPLIER,
                    ENDER_SPEAR_CHARGE_DELAY_SECONDS,
                    ENDER_SPEAR_MAX_DURATION_FOR_DISMOUNT_SECONDS,
                    ENDER_SPEAR_MIN_SPEED_FOR_DISMOUNT,
                    ENDER_SPEAR_MAX_DURATION_FOR_CHARGE_KNOCKBACK_SECONDS,
                    ENDER_SPEAR_MIN_SPEED_FOR_CHARGE_KNOCKBACK,
                    ENDER_SPEAR_MAX_DURATION_FOR_CHARGE_DAMAGE_SECONDS,
                    ENDER_SPEAR_MIN_RELATIVE_SPEED_FOR_CHARGE_DAMAGE
            );
            if (result instanceof Item item) {
                ModItem.DOUBLE_ENDER_SPEAR = Registry.register(Registries.ITEM, DOUBLE_ENDER_SPEAR_ITEM_ID, item);
                HimProveMe.LOGGER.info("Registered compat item {}", DOUBLE_ENDER_SPEAR_ITEM_ID);
                return ModItem.DOUBLE_ENDER_SPEAR;
            }
        } catch (ReflectiveOperationException exception) {
            HimProveMe.LOGGER.warn("Failed to register compat double ender spear", exception);
        }

        return null;
    }

    public static boolean shouldUseOffhandAttack(PlayerEntity player) {
        return hasPiercingWeapon(player.getOffHandStack());
    }

    public static boolean hasPiercingWeapon(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        ensureEnderSpearComponents(stack);
        ComponentType<?> componentType = getPiercingWeaponComponent();
        return componentType != null && stack.get(componentType) != null;
    }

    public static float getMaxAttackReach(ItemStack stack) {
        Object attackRange = getAttackRangeComponentValue(stack);
        if (attackRange == null) {
            return isCompatSpear(stack) ? ATTACK_RANGE_MAX : 3.0f;
        }

        try {
            Method accessor = getAttackRangeMaxReachMethod(attackRange);
            if (accessor == null) {
                return isCompatSpear(stack) ? ATTACK_RANGE_MAX : 3.0f;
            }

            Object result = accessor.invoke(attackRange);
            return result instanceof Float maxReach ? maxReach : ATTACK_RANGE_MAX;
        } catch (ReflectiveOperationException exception) {
            HimProveMe.LOGGER.warn("Failed to read spear attack range", exception);
            return isCompatSpear(stack) ? ATTACK_RANGE_MAX : 3.0f;
        }
    }

    public static boolean stabWithOffhand(PlayerEntity player) {
        ItemStack offhandStack = player.getOffHandStack();
        ensureEnderSpearComponents(offhandStack);

        Object piercingWeapon = getPiercingWeaponComponentValue(offhandStack);
        if (piercingWeapon == null) {
            return false;
        }

        try {
            Method stabMethod = getPiercingStabMethod(piercingWeapon);
            if (stabMethod == null) {
                return false;
            }

            Object result = stabMethod.invoke(piercingWeapon, player, EquipmentSlot.OFFHAND);
            return result instanceof Boolean success && success;
        } catch (ReflectiveOperationException exception) {
            HimProveMe.LOGGER.warn("Failed to perform offhand spear stab", exception);
            return false;
        }
    }

    public static void ensureEnderSpearComponents(ItemStack stack) {
        if (!isCompatSpear(stack) || !isLoaded()) {
            return;
        }

        ensureComponentLookup();
        ensureComponentConstructionLookup();
        if (!canBuildCompatComponents()) {
            return;
        }

        if (useEffectsComponent != null && !stack.contains(useEffectsComponent)) {
            setComponent(stack, useEffectsComponent, createUseEffectsComponent());
        }

        if (minimumAttackChargeComponent != null && !stack.contains(minimumAttackChargeComponent)) {
            setComponent(stack, minimumAttackChargeComponent, MINIMUM_ATTACK_CHARGE);
        }

        if (attackRangeComponent != null && !stack.contains(attackRangeComponent)) {
            setComponent(stack, attackRangeComponent, createAttackRangeComponent());
        }

        if (isSpearChargeAttacksEnabled() && kineticWeaponComponent != null && !stack.contains(kineticWeaponComponent)) {
            setComponent(stack, kineticWeaponComponent, createKineticWeaponComponent());
        }

        if (isSpearStabbingAnimationEnabled()) {
            if (swingAnimationComponent != null && !stack.contains(swingAnimationComponent)) {
                setComponent(stack, swingAnimationComponent, createSwingAnimationComponent());
            }
            if (piercingWeaponComponent != null && !stack.contains(piercingWeaponComponent)) {
                setComponent(stack, piercingWeaponComponent, createPiercingWeaponComponent());
            }
        }
    }

    public static boolean isCompatEnderSpear(ItemStack stack) {
        return !stack.isEmpty() && Registries.ITEM.getId(stack.getItem()).equals(ENDER_SPEAR_ITEM_ID);
    }

    public static boolean isCompatDoubleEnderSpear(ItemStack stack) {
        return !stack.isEmpty() && Registries.ITEM.getId(stack.getItem()).equals(DOUBLE_ENDER_SPEAR_ITEM_ID);
    }

    private static boolean isCompatSpear(ItemStack stack) {
        return isCompatEnderSpear(stack) || isCompatDoubleEnderSpear(stack);
    }

    private static Item registerFactoryEnderSpear() {
        if (Registries.ITEM.containsId(ENDER_SPEAR_ITEM_ID)) {
            ModItem.ENDER_SPEAR = Registries.ITEM.get(ENDER_SPEAR_ITEM_ID);
            return ModItem.ENDER_SPEAR;
        }

        Method factoryMethod = getRegisterSpearRawMethod();
        if (factoryMethod == null) {
            return null;
        }

        try {
            Object result = factoryMethod.invoke(
                    null,
                    ModToolMaterials.ENDER_INGOT,
                    ENDER_SPEAR_SWING_SECONDS,
                    ENDER_SPEAR_CHARGE_DAMAGE_MULTIPLIER,
                    ENDER_SPEAR_CHARGE_DELAY_SECONDS,
                    ENDER_SPEAR_MAX_DURATION_FOR_DISMOUNT_SECONDS,
                    ENDER_SPEAR_MIN_SPEED_FOR_DISMOUNT,
                    ENDER_SPEAR_MAX_DURATION_FOR_CHARGE_KNOCKBACK_SECONDS,
                    ENDER_SPEAR_MIN_SPEED_FOR_CHARGE_KNOCKBACK,
                    ENDER_SPEAR_MAX_DURATION_FOR_CHARGE_DAMAGE_SECONDS,
                    ENDER_SPEAR_MIN_RELATIVE_SPEED_FOR_CHARGE_DAMAGE
            );
            if (result instanceof Item item) {
                ModItem.ENDER_SPEAR = Registry.register(Registries.ITEM, ENDER_SPEAR_ITEM_ID, item);
                HimProveMe.LOGGER.info("Registered spear compat item {}", ENDER_SPEAR_ITEM_ID);
                return ModItem.ENDER_SPEAR;
            }
        } catch (ReflectiveOperationException exception) {
            HimProveMe.LOGGER.warn("Failed to register compat ender spear", exception);
        }

        return null;
    }

    private static Item.Settings createDoubleEnderSpearSettings() {
        Item.Settings settings = new Item.Settings().attributeModifiers(createDoubleEnderSpearAttributeModifiers());
        addComponent(settings, useEffectsComponent, createUseEffectsComponent());
        addComponent(settings, minimumAttackChargeComponent, MINIMUM_ATTACK_CHARGE);
        addComponent(settings, attackRangeComponent, createAttackRangeComponent());
        if (isSpearChargeAttacksEnabled()) {
            addComponent(settings, kineticWeaponComponent, createKineticWeaponComponent());
        }

        if (isSpearStabbingAnimationEnabled()) {
            addComponent(settings, swingAnimationComponent, createSwingAnimationComponent());
            addComponent(settings, piercingWeaponComponent, createPiercingWeaponComponent());
        }

        return settings;
    }

    private static AttributeModifiersComponent createDoubleEnderSpearAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID, ModToolMaterials.ENDER_INGOT.getAttackDamage(), EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        new EntityAttributeModifier(Item.BASE_ATTACK_SPEED_MODIFIER_ID, (double) (1.0F / DOUBLE_ENDER_SPEAR_SWING_SECONDS) - 4.0D, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }

    private static Object getPiercingWeaponComponentValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        ensureEnderSpearComponents(stack);
        ComponentType<?> componentType = getPiercingWeaponComponent();
        return componentType == null ? null : stack.get(componentType);
    }

    private static Object getAttackRangeComponentValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        ensureEnderSpearComponents(stack);
        ComponentType<?> componentType = getAttackRangeComponent();
        return componentType == null ? null : stack.get(componentType);
    }

    private static ComponentType<?> getPiercingWeaponComponent() {
        ensureComponentLookup();
        return piercingWeaponComponent;
    }

    private static ComponentType<?> getAttackRangeComponent() {
        ensureComponentLookup();
        return attackRangeComponent;
    }

    private static void ensureComponentLookup() {
        if (componentLookupAttempted || !isLoaded()) {
            return;
        }

        componentLookupAttempted = true;
        useEffectsComponent = getComponent(USE_EFFECTS_COMPONENT_ID);
        attackRangeComponent = getComponent(ATTACK_RANGE_COMPONENT_ID);
        kineticWeaponComponent = getComponent(KINETIC_WEAPON_COMPONENT_ID);
        swingAnimationComponent = getComponent(SWING_ANIMATION_COMPONENT_ID);
        piercingWeaponComponent = getComponent(PIERCING_WEAPON_COMPONENT_ID);
        minimumAttackChargeComponent = getComponent(MINIMUM_ATTACK_CHARGE_COMPONENT_ID);
    }

    private static ComponentType<?> getComponent(Identifier id) {
        return Registries.DATA_COMPONENT_TYPE.containsId(id) ? Registries.DATA_COMPONENT_TYPE.get(id) : null;
    }

    private static Method getPiercingStabMethod(Object piercingWeapon) {
        ensurePiercingReflectionLookup(piercingWeapon);
        return piercingStabMethod;
    }

    private static Method getRegisterSpearRawMethod() {
        ensureSpearFactoryLookup();
        return registerSpearRawMethod;
    }

    private static Method getAttackRangeMaxReachMethod(Object attackRange) {
        ensureAttackRangeReflectionLookup(attackRange);
        return attackRangeMaxReachMethod;
    }

    private static void ensurePiercingReflectionLookup(Object componentInstance) {
        if (piercingReflectionLookupAttempted || componentInstance == null) {
            return;
        }

        piercingReflectionLookupAttempted = true;
        try {
            if (componentInstance.getClass().getName().equals("com.notunanancyowen.spears.components.PiercingWeapon")) {
                piercingStabMethod = componentInstance.getClass().getMethod("stab", LivingEntity.class, EquipmentSlot.class);
                piercingStabMethod.setAccessible(true);
            }
        } catch (ReflectiveOperationException exception) {
            HimProveMe.LOGGER.warn("Failed to initialize spear piercing reflection", exception);
        }
    }

    private static void ensureAttackRangeReflectionLookup(Object componentInstance) {
        if (attackRangeReflectionLookupAttempted || componentInstance == null) {
            return;
        }

        attackRangeReflectionLookupAttempted = true;
        try {
            if (componentInstance.getClass().getName().equals("com.notunanancyowen.spears.components.AttackRange")) {
                attackRangeMaxReachMethod = componentInstance.getClass().getMethod("maxReach");
                attackRangeMaxReachMethod.setAccessible(true);
            }
        } catch (ReflectiveOperationException exception) {
            HimProveMe.LOGGER.warn("Failed to initialize spear attack range reflection", exception);
        }
    }

    private static void ensureSpearFactoryLookup() {
        if (spearFactoryLookupAttempted || !isLoaded()) {
            return;
        }

        spearFactoryLookupAttempted = true;
        try {
            Class<?> spearsClass = Class.forName("com.notunanancyowen.spears.Spears");
            registerSpearRawMethod = spearsClass.getMethod(
                    "registerSpearRaw",
                    ToolMaterial.class,
                    float.class,
                    float.class,
                    float.class,
                    float.class,
                    float.class,
                    float.class,
                    float.class,
                    float.class,
                    float.class
            );
            registerSpearRawMethod.setAccessible(true);
        } catch (ReflectiveOperationException exception) {
            HimProveMe.LOGGER.warn("Failed to initialize spear factory reflection", exception);
        }
    }

    private static void ensureComponentConstructionLookup() {
        if (componentConstructionLookupAttempted || !isLoaded()) {
            return;
        }

        componentConstructionLookupAttempted = true;
        try {
            Class<?> useEffectsClass = Class.forName("com.notunanancyowen.spears.components.UseEffects");
            useEffectsConstructor = useEffectsClass.getConstructor(float.class, boolean.class, boolean.class);
            useEffectsConstructor.setAccessible(true);

            Class<?> attackRangeClass = Class.forName("com.notunanancyowen.spears.components.AttackRange");
            attackRangeConstructor = attackRangeClass.getConstructor(float.class, float.class);
            attackRangeConstructor.setAccessible(true);

            Class<?> kineticWeaponClass = Class.forName("com.notunanancyowen.spears.components.KineticWeapon");
            kineticWeaponConstructor = kineticWeaponClass.getConstructor(float.class, int.class, int.class, Optional.class, Optional.class, Optional.class, float.class, float.class, Optional.class, Optional.class);
            kineticWeaponConstructor.setAccessible(true);

            Class<?> kineticWeaponConditionClass = Class.forName("com.notunanancyowen.spears.components.KineticWeapon$Condition");
            kineticWeaponConditionConstructor = kineticWeaponConditionClass.getConstructor(int.class, float.class, float.class);
            kineticWeaponConditionConstructor.setAccessible(true);

            Class<?> swingAnimationClass = Class.forName("com.notunanancyowen.spears.components.SwingAnimation");
            swingAnimationConstructor = swingAnimationClass.getConstructor(int.class, String.class);
            swingAnimationConstructor.setAccessible(true);

            Class<?> piercingWeaponClass = Class.forName("com.notunanancyowen.spears.components.PiercingWeapon");
            piercingWeaponConstructor = piercingWeaponClass.getConstructor(float.class, boolean.class, boolean.class, Optional.class, Optional.class);
            piercingWeaponConstructor.setAccessible(true);
        } catch (ReflectiveOperationException exception) {
            HimProveMe.LOGGER.warn("Failed to initialize compat spear component construction", exception);
        }
    }

    private static boolean canBuildCompatComponents() {
        return useEffectsConstructor != null
                && attackRangeConstructor != null
                && swingAnimationConstructor != null
                && piercingWeaponConstructor != null
                && (!isSpearChargeAttacksEnabled() || (kineticWeaponConstructor != null && kineticWeaponConditionConstructor != null));
    }

    private static Object createUseEffectsComponent() {
        try {
            return useEffectsConstructor.newInstance(USE_EFFECTS_STRAFE_SPEED, USE_EFFECTS_ALLOW_SPRINTING, USE_EFFECTS_INTERACT_VIBRATIONS);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create spear use effects component", exception);
        }
    }

    private static Object createAttackRangeComponent() {
        try {
            return attackRangeConstructor.newInstance(ATTACK_RANGE_MIN, ATTACK_RANGE_MAX);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create spear attack range component", exception);
        }
    }

    private static Object createKineticWeaponComponent() {
        try {
            return kineticWeaponConstructor.newInstance(
                    KINETIC_HITBOX_MARGIN,
                    KINETIC_CONTACT_COOLDOWN_TICKS,
                    Math.round(ENDER_SPEAR_CHARGE_DELAY_SECONDS * 20.0F),
                    createKineticWeaponCondition(ENDER_SPEAR_MAX_DURATION_FOR_DISMOUNT_SECONDS, ENDER_SPEAR_MIN_SPEED_FOR_DISMOUNT, 0.0F),
                    createKineticWeaponCondition(ENDER_SPEAR_MAX_DURATION_FOR_CHARGE_KNOCKBACK_SECONDS, ENDER_SPEAR_MIN_SPEED_FOR_CHARGE_KNOCKBACK, 0.0F),
                    createKineticWeaponCondition(ENDER_SPEAR_MAX_DURATION_FOR_CHARGE_DAMAGE_SECONDS, 0.0F, ENDER_SPEAR_MIN_RELATIVE_SPEED_FOR_CHARGE_DAMAGE),
                    KINETIC_FORWARD_MOVEMENT,
                    ENDER_SPEAR_CHARGE_DAMAGE_MULTIPLIER,
                    getSoundEntry("item.spear.use"),
                    getSoundEntry("item.spear.hit")
            );
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create spear kinetic component", exception);
        }
    }

    private static Optional<Object> createKineticWeaponCondition(float maxDurationSeconds, float minSpeed, float minRelativeSpeed) {
        try {
            return Optional.of(kineticWeaponConditionConstructor.newInstance(Math.round(maxDurationSeconds * 20.0F), minSpeed, minRelativeSpeed));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create spear kinetic condition", exception);
        }
    }

    private static Object createSwingAnimationComponent() {
        try {
            return swingAnimationConstructor.newInstance(SWING_TICKS, "stab");
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create spear swing animation component", exception);
        }
    }

    private static Object createPiercingWeaponComponent() {
        try {
            return piercingWeaponConstructor.newInstance(
                    PIERCING_HITBOX_MARGIN,
                    true,
                    false,
                    getSoundEntry("item.spear.attack"),
                    getSoundEntry("item.spear.hit")
            );
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create spear piercing component", exception);
        }
    }

    private static Optional<RegistryEntry.Reference<SoundEvent>> getSoundEntry(String soundId) {
        return Registries.SOUND_EVENT.getEntry(Identifier.ofVanilla(soundId));
    }

    private static boolean isSpearStabbingAnimationEnabled() {
        return getConfigToggle("spear_stabbing_animation", true);
    }

    private static boolean isSpearChargeAttacksEnabled() {
        return getConfigToggle("spear_charge_attacks", true);
    }

    private static boolean getConfigToggle(String key, boolean defaultValue) {
        Map<?, ?> config = getSpearsConfig();
        Object value = config == null ? null : config.get(key);
        return value instanceof Boolean toggle ? toggle : defaultValue;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Boolean> getSpearsConfig() {
        if (!isLoaded()) {
            return null;
        }

        ensureConfigLookup();
        if (spearsConfigMethod == null) {
            return null;
        }

        try {
            Object result = spearsConfigMethod.invoke(null);
            return result instanceof Map<?, ?> map ? (Map<String, Boolean>) map : null;
        } catch (ReflectiveOperationException exception) {
            HimProveMe.LOGGER.warn("Failed to read spear config", exception);
            return null;
        }
    }

    private static void ensureConfigLookup() {
        if (configLookupAttempted || !isLoaded()) {
            return;
        }

        configLookupAttempted = true;
        try {
            Class<?> spearsClass = Class.forName("com.notunanancyowen.spears.Spears");
            spearsConfigMethod = spearsClass.getMethod("makeConfig");
            spearsConfigMethod.setAccessible(true);
        } catch (ReflectiveOperationException exception) {
            HimProveMe.LOGGER.warn("Failed to initialize spear config reflection", exception);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addComponent(Item.Settings settings, ComponentType<?> componentType, Object value) {
        if (componentType != null && value != null) {
            settings.component((ComponentType) componentType, value);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setComponent(ItemStack stack, ComponentType<?> componentType, Object value) {
        stack.set((ComponentType) componentType, value);
    }
}
