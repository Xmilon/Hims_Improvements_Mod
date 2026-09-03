package net.xmilon.himproveme.compat.jade;

import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.effect.ModStatusEffects;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

@WailaPlugin(HimProveMe.MOD_ID)
public final class HimProveMeJadePlugin implements IWailaPlugin {
    private static final Identifier EFFECTS_PROVIDER_ID = Identifier.of(HimProveMe.MOD_ID, "perk_effects");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(StatusEffectsProvider.INSTANCE, LivingEntity.class);
    }

    private enum StatusEffectsProvider implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (!(accessor.getEntity() instanceof LivingEntity living)) return;

            List<Text> activeEffects = new ArrayList<>();
            if (living.hasStatusEffect(ModStatusEffects.BLEEDING)) {
                activeEffects.add(Text.translatable("jade.himproveme.effect.bleeding"));
            }
            if (living.hasStatusEffect(ModStatusEffects.STUNNED)) {
                activeEffects.add(Text.translatable("jade.himproveme.effect.stunned"));
            }
            if (living.hasStatusEffect(ModStatusEffects.FRENZY)) {
                activeEffects.add(Text.translatable("jade.himproveme.effect.frenzy"));
            }

            if (!activeEffects.isEmpty()) {
                tooltip.add(Text.translatable("jade.himproveme.effects", String.join(", ", activeEffects.stream().map(Text::getString).toList())));
            }
        }

        @Override
        public Identifier getUid() {
            return EFFECTS_PROVIDER_ID;
        }
    }
}
