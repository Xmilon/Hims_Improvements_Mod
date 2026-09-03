package net.xmilon.himproveme.perk.warden;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.effect.ModStatusEffects;
import net.xmilon.himproveme.item.ModItem;
import net.xmilon.himproveme.network.warden.WardenSepukuPayload;
import net.xmilon.himproveme.perk.PerkAccess;
import net.xmilon.himproveme.perk.PerkInstanceState;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public final class WardenPerkHelper {
    public static final Identifier WARDEN_BLEEDING = Identifier.of(HimProveMe.MOD_ID, "warden_bleeding");
    public static final Identifier WARDEN_STUNNED = Identifier.of(HimProveMe.MOD_ID, "warden_stunned");
    public static final Identifier WARDEN_FRENZY = Identifier.of(HimProveMe.MOD_ID, "warden_frenzy");
    public static final java.util.List<Identifier> WARDEN_PERKS = java.util.List.of(WARDEN_BLEEDING, WARDEN_STUNNED, WARDEN_FRENZY);

    private static final Identifier HEALTH_PENALTY_MODIFIER_ID = Identifier.of(HimProveMe.MOD_ID, "warden_perk_health_penalty");
    private static final double HEALTH_PENALTY = -4.0D;

    private static final float BLEEDING_PROC_CHANCE = 0.15F;
    private static final float CRUSHED_WILL_PROC_CHANCE = 0.20F;
    private static final float CRUSHED_WILL_LOW_HP_PROC_CHANCE = 0.05F;
    private static final float CRUSHED_WILL_HP_THRESHOLD = 30.0F;
    private static final float SPIRIT_SHACKLE_PROC_CHANCE = 0.10F;

    private static final int BLEEDING_DURATION_TICKS = 160;
    private static final int CRUSHED_WILL_DURATION_TICKS = 160;
    private static final int SPIRIT_SHACKLE_DURATION_TICKS = 340;

    public static boolean himproveme$frenzyDamageGuard;
    public static boolean himproveme$spiritShackleGuard;

    private WardenPerkHelper() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(WardenSepukuPayload.ID, WardenSepukuPayload.CODEC);
    }

    public static boolean isWardenPerk(Identifier perkId) {
        return WARDEN_PERKS.contains(perkId);
    }

    public static boolean hasUnlockToken(ServerPlayerEntity player) {
        return player.getMainHandStack().isOf(ModItem.WARDEN_TOKEN);
    }

    public static void consumeUnlockToken(ServerPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        if (stack.isOf(ModItem.WARDEN_TOKEN)) {
            stack.decrement(1);
        }
    }

    public static void enforceExclusiveToggle(PerkInstanceState instance, Identifier enabledPerkId) {
        for (Identifier perkId : WARDEN_PERKS) {
            if (!perkId.equals(enabledPerkId) && instance.getLevel(perkId) > 0) {
                instance.setEnabled(perkId, false);
            }
        }
    }

    public static boolean hasAnyActiveWardenPerk(PlayerEntity player) {
        return getActiveProfile(player).isPresent();
    }

    public static boolean hasAnyUnlockedWardenPerk(PlayerEntity player) {
        return PerkAccess.hasUnlocked(player, WARDEN_BLEEDING)
                || PerkAccess.hasUnlocked(player, WARDEN_STUNNED)
                || PerkAccess.hasUnlocked(player, WARDEN_FRENZY);
    }

    public static Optional<Identifier> getActiveProfile(PlayerEntity player) {
        if (PerkAccess.hasEffect(player, WARDEN_BLEEDING)) {
            return Optional.of(WARDEN_BLEEDING);
        }
        if (PerkAccess.hasEffect(player, WARDEN_STUNNED)) {
            return Optional.of(WARDEN_STUNNED);
        }
        if (PerkAccess.hasEffect(player, WARDEN_FRENZY)) {
            return Optional.of(WARDEN_FRENZY);
        }
        return Optional.empty();
    }

    public static void applyHealthPenalty(PlayerEntity player) {
        EntityAttributeInstance maxHealth = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealth == null) return;

        if (hasAnyActiveWardenPerk(player)) {
            maxHealth.updateModifier(new EntityAttributeModifier(HEALTH_PENALTY_MODIFIER_ID, HEALTH_PENALTY, EntityAttributeModifier.Operation.ADD_VALUE));
            if (!player.getWorld().isClient() && player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        } else {
            maxHealth.removeModifier(HEALTH_PENALTY_MODIFIER_ID);
        }
    }

    public static void onSuccessfulAttack(ServerPlayerEntity attacker, LivingEntity target, float dealtDamage) {
        HimProveMe.LOGGER.info("onSuccessfulAttack called: attacker={}, target={}, dealt={}", attacker.getName().getString(), target.getName().getString(), dealtDamage);

        if (dealtDamage <= 0.0F || !target.isAlive()) {
            HimProveMe.LOGGER.info("onSuccessfulAttack early exit: dealt={}, alive={}", dealtDamage, target.isAlive());
            return;
        }

        Identifier activePerk = getActiveProfile(attacker).orElse(null);
        HimProveMe.LOGGER.info("onSuccessfulAttack activePerk={}", activePerk);
        if (activePerk == null) return;

        ServerWorld world = attacker.getServerWorld();
        float roll = world.getRandom().nextFloat();
        HimProveMe.LOGGER.info("onSuccessfulAttack roll={}", roll);

        if (activePerk == WARDEN_BLEEDING) {
            HimProveMe.LOGGER.info("BLEEDING proc check: roll={} vs chance={}", roll, BLEEDING_PROC_CHANCE);
            if (roll < BLEEDING_PROC_CHANCE) {
                HimProveMe.LOGGER.info("APPLYING BLEEDING to target {}", target.getName().getString());
                target.addStatusEffect(new StatusEffectInstance(ModStatusEffects.BLEEDING, BLEEDING_DURATION_TICKS, 0, true, false, true));
            }
        } else if (activePerk == WARDEN_STUNNED) {
            float crushedWillChance = target.getMaxHealth() > CRUSHED_WILL_HP_THRESHOLD ? CRUSHED_WILL_LOW_HP_PROC_CHANCE : CRUSHED_WILL_PROC_CHANCE;
            HimProveMe.LOGGER.info("CRUSHED WILL proc check: roll={} vs chance={} (targetMaxHP={})", roll, crushedWillChance, target.getMaxHealth());
            if (roll < crushedWillChance) {
                HimProveMe.LOGGER.info("APPLYING CRUSHED WILL to target {}", target.getName().getString());
                target.addStatusEffect(new StatusEffectInstance(ModStatusEffects.STUNNED, CRUSHED_WILL_DURATION_TICKS, 0, true, false, true));
                if (target instanceof MobEntity mob) {
                    fleeFromAttacker(mob, attacker);
                }
            }
        } else if (activePerk == WARDEN_FRENZY) {
            HimProveMe.LOGGER.info("SPIRIT SHACKLE proc check: roll={} vs chance={}", roll, SPIRIT_SHACKLE_PROC_CHANCE);
            if (roll < SPIRIT_SHACKLE_PROC_CHANCE) {
                HimProveMe.LOGGER.info("APPLYING SPIRIT SHACKLE to target {}", target.getName().getString());
                target.addStatusEffect(new StatusEffectInstance(ModStatusEffects.FRENZY, SPIRIT_SHACKLE_DURATION_TICKS, 0, true, false, true));
            }
        }
    }

    public static void playSepukuAnimation(LivingEntity target, int durationTicks) {
        Collection<ServerPlayerEntity> recipients = new HashSet<>(PlayerLookup.tracking(target));
        if (target instanceof ServerPlayerEntity playerTarget) {
            recipients.add(playerTarget);
        }
        WardenSepukuPayload payload = new WardenSepukuPayload(target.getUuid(), durationTicks);
        for (ServerPlayerEntity recipient : recipients) {
            ServerPlayNetworking.send(recipient, payload);
        }
    }

    private static void fleeFromAttacker(MobEntity mob, LivingEntity attacker) {
        mob.setTarget(null);
        double awayX = mob.getX() - attacker.getX();
        double awayZ = mob.getZ() - attacker.getZ();
        double length = Math.sqrt(awayX * awayX + awayZ * awayZ);
        if (length < 1.0E-4D) {
            awayX = mob.getRandom().nextDouble() - 0.5D;
            awayZ = mob.getRandom().nextDouble() - 0.5D;
            length = Math.sqrt(awayX * awayX + awayZ * awayZ);
        }
        double scale = 16.0D / length;
        mob.getNavigation().startMovingTo(mob.getX() + awayX * scale, mob.getY(), mob.getZ() + awayZ * scale, 1.5D);
    }
}
