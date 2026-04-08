package net.xmilon.himproveme.compat.jade;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.perk.warden.AfflictionProfile;
import net.xmilon.himproveme.perk.warden.WardenPerkHelper;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ProgressStyle;

/**
 * Optional Jade integration that exposes the ephemeral affliction bar on hovered entities without making Jade required.
 */
@WailaPlugin(HimProveMe.MOD_ID)
public final class HimProveMeJadePlugin implements IWailaPlugin {
    private static final Identifier AFFLICTION_PROVIDER_ID = Identifier.of(HimProveMe.MOD_ID, "affliction_bar");
    private static final String BAR_KEY = "AfflictionBar";
    private static final String PROFILE_KEY = "AfflictionProfile";

    /**
     * Registers the server-side data provider used to serialize the otherwise-ephemeral affliction bar into Jade requests.
     */
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(AfflictionBarProvider.INSTANCE, LivingEntity.class);
    }

    /**
     * Registers the client-side tooltip renderer that turns the synced server data into a Jade progress bar.
     */
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(AfflictionBarProvider.INSTANCE, LivingEntity.class);
    }

    /**
     * Shared client/server provider that serializes the affliction state and renders a Jade progress element when present.
     */
    private enum AfflictionBarProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
        INSTANCE;

        /**
         * Emits the current bar state for the hovered living entity so Jade can render it client-side without extra packets.
         */
        @Override
        public void appendServerData(NbtCompound data, EntityAccessor accessor) {
            if (!(accessor.getEntity() instanceof LivingEntity livingEntity)) {
                return;
            }

            WardenPerkHelper.getAfflictionView(livingEntity).ifPresent(view -> {
                data.putFloat(BAR_KEY, view.barPercent());
                data.putString(PROFILE_KEY, view.profile().name());
            });
        }

        /**
         * Renders the affliction bar and profile label in Jade only when the server supplied a live affliction snapshot.
         */
        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            NbtCompound data = accessor.getServerData();
            if (!data.contains(BAR_KEY)) {
                return;
            }

            AfflictionProfile profile;
            try {
                profile = AfflictionProfile.valueOf(data.getString(PROFILE_KEY));
            } catch (IllegalArgumentException exception) {
                return;
            }

            float barPercent = Math.max(0.0F, Math.min(100.0F, data.getFloat(BAR_KEY)));
            IElementHelper helper = IElementHelper.get();
            ProgressStyle style = helper.progressStyle()
                    .color(getPrimaryColor(profile), getSecondaryColor(profile))
                    .textColor(0xFFF3E6D1);

            tooltip.add(Text.translatable("jade.himproveme.affliction", getProfileText(profile)));
            tooltip.add(helper.progress(
                    barPercent / 100.0F,
                    Text.translatable("jade.himproveme.affliction_progress", Math.round(barPercent)),
                    style,
                    BoxStyle.getNestedBox(),
                    true
            ));
        }

        /**
         * Uses a stable provider ID so Jade can manage ordering and toggles consistently across sessions.
         */
        @Override
        public Identifier getUid() {
            return AFFLICTION_PROVIDER_ID;
        }

        /**
         * Requests server data for hovered entities because the affliction runtime lives only on the logical server.
         */
        @Override
        public boolean shouldRequestData(EntityAccessor accessor) {
            return accessor.getEntity() instanceof LivingEntity;
        }

        /**
         * Maps each affliction profile to a translatable label instead of leaking enum constants into the HUD.
         */
        private Text getProfileText(AfflictionProfile profile) {
            return switch (profile) {
                case BLEEDING -> Text.translatable("jade.himproveme.affliction.bleeding");
                case STUNNED -> Text.translatable("jade.himproveme.affliction.stunned");
                case FRENZY -> Text.translatable("jade.himproveme.affliction.frenzy");
            };
        }

        /**
         * Supplies the darker base color used by the Jade progress bar for the current affliction profile.
         */
        private int getPrimaryColor(AfflictionProfile profile) {
            return switch (profile) {
                case BLEEDING -> 0xFF742024;
                case STUNNED -> 0xFF8D8A4D;
                case FRENZY -> 0xFF8C2922;
            };
        }

        /**
         * Supplies the brighter fill color used by the Jade progress bar for the current affliction profile.
         */
        private int getSecondaryColor(AfflictionProfile profile) {
            return switch (profile) {
                case BLEEDING -> 0xFFD15356;
                case STUNNED -> 0xFFD4D07A;
                case FRENZY -> 0xFFE0644A;
            };
        }
    }
}
