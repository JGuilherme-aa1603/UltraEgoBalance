package br.com.guiol.ultrabalancetweaks;

import br.com.guiol.ultrabalancetweaks.network.BalanceNetwork;
import com.dragonminez.common.config.ConfigManager;
import com.dragonminez.common.config.FormConfig;
import com.dragonminez.common.events.DMZEvent;
import com.dragonminez.common.network.NetworkHandler;
import com.dragonminez.common.network.S2C.StatsSyncS2C;
import com.dragonminez.common.stats.StatsData;
import com.google.gson.Gson;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.common.MinecraftForge;

import java.util.LinkedHashMap;

/**
 * Mastery reward that turns Ultra Instinct -Sign- into a stackable technique.
 * The native DMZ stack-form slot is used for rendering and persistence, while
 * all attribute multipliers stay at x1 so the Saiyan transformation remains
 * the source of the player's power.
 */
public final class InstinctTechnique {
    public static final String GROUP = "ultrainstincttechnique";
    public static final String FORM = "sign";
    private static final String UE_NOTICE = UltraBalanceTweaks.MOD_ID + ":ue_mastery_reward_announced";
    private static final String UI_NOTICE = UltraBalanceTweaks.MOD_ID + ":ui_mastery_reward_announced";

    private InstinctTechnique() {
    }

    public static void installNativeStackForm() {
        if (ConfigManager.getAllStackForms().containsKey(GROUP)) {
            return;
        }
        FormConfig.FormData source = ConfigManager.getForm("saiyan", "ultrainstinct", "sign");
        if (source == null) {
            UltraBalanceTweaks.LOGGER.error("Could not install the mastered Instinct technique: UI Sign form was not found");
            return;
        }

        FormConfig.FormData technique = new Gson().fromJson(new Gson().toJson(source), FormConfig.FormData.class);
        technique.setName(FORM);
        technique.setKeepBaseFormHeadBones(true);
        technique.setHairType("");
        technique.setForcedHairCode("");
        technique.setHairColor("");
        technique.setStrMultiplier(1.0);
        technique.setSkpMultiplier(1.0);
        technique.setStmMultiplier(1.0);
        technique.setDefMultiplier(1.0);
        technique.setVitMultiplier(1.0);
        technique.setPwrMultiplier(1.0);
        technique.setEneMultiplier(1.0);
        technique.setSpeedMultiplier(1.0);
        technique.setStaminaDrainMultiplier(1.0);
        technique.setEnergyDrain(0.0);
        technique.setStaminaDrain(0.0);
        technique.setHealthDrain(0.0);
        technique.setAttackSpeed(1.0);
        technique.setMaxStatsMultiplier(1.0);
        technique.setMaxCostMultiplier(1.0);
        technique.setMasteryPerHitDealt(0.0);
        technique.setMasteryPerHitReceived(0.0);
        technique.setPassiveMasteryEveryFiveSeconds(0.0);
        technique.setFormStackable(true);
        technique.setStackDrainMultiplier(1.0);

        FormConfig group = new FormConfig();
        group.setConfigVersion(FormConfig.CURRENT_VERSION);
        group.setGroupName(GROUP);
        group.setFormType(GROUP);
        LinkedHashMap<String, FormConfig.FormData> forms = new LinkedHashMap<>();
        forms.put(FORM, technique);
        group.setForms(forms);
        ConfigManager.getAllStackForms().put(GROUP, group);
        UltraBalanceTweaks.LOGGER.info("Installed native stackable Instinct technique form");
    }

    public static boolean destructionUnlocked(ServerPlayer player) {
        return DmzForms.hasMastered(player, "ultraego", "mastered");
    }

    public static boolean unlocked(ServerPlayer player) {
        return DmzForms.hasMastered(player, "ultrainstinct", "true");
    }

    public static boolean isActive(ServerPlayer player) {
        StatsData data = DmzForms.stats(player);
        return data != null && data.getCharacter().hasActiveStackForm()
                && GROUP.equals(DmzForms.normalize(data.getCharacter().getActiveStackFormGroup()))
                && FORM.equals(DmzForms.normalize(data.getCharacter().getActiveStackForm()));
    }

    public static void toggle(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) {
            return;
        }
        if (isActive(player)) {
            deactivate(player, "message.ultrabalancetweaks.instinct_technique_off");
            return;
        }
        if (!unlocked(player)) {
            player.displayClientMessage(Component.translatable(
                    "message.ultrabalancetweaks.instinct_technique_locked"), true);
            return;
        }
        if (!DmzForms.isSaiyan(player)) {
            player.displayClientMessage(Component.translatable(
                    "message.ultrabalancetweaks.instinct_technique_saiyan_only"), true);
            return;
        }

        StatsData data = DmzForms.stats(player);
        if (data == null || !data.getCharacter().hasActiveForm()) {
            player.displayClientMessage(Component.translatable(
                    "message.ultrabalancetweaks.instinct_technique_requires_form"), true);
            return;
        }
        String primaryGroup = DmzForms.normalize(data.getCharacter().getActiveFormGroup());
        if ("ultrainstinct".equals(primaryGroup) || "ultraego".equals(primaryGroup)) {
            player.displayClientMessage(Component.translatable(
                    "message.ultrabalancetweaks.instinct_technique_requires_saiyan_form"), true);
            return;
        }
        if (data.getCharacter().hasActiveStackForm()) {
            player.displayClientMessage(Component.translatable(
                    "message.ultrabalancetweaks.instinct_technique_stack_conflict"), true);
            return;
        }
        if (ConfigManager.getStackForm(GROUP, FORM) == null) {
            installNativeStackForm();
            if (ConfigManager.getStackForm(GROUP, FORM) == null) {
                return;
            }
        }

        float[] resources = data.snapshotMultiplierResources();
        data.getCharacter().recordPreviousStackForm();
        data.getCharacter().getStackFormsUsedBefore().putForm(GROUP, FORM);
        data.getCharacter().setActiveStackForm(GROUP, FORM);
        data.restoreMultiplierGains(player, resources);
        player.refreshDimensions();
        MinecraftForge.EVENT_BUS.post(new DMZEvent.StackFormChangeEvent(player, "", "", GROUP, FORM));
        syncNativeState(player);
        activationEffect(player, true);
        player.displayClientMessage(Component.translatable(
                "message.ultrabalancetweaks.instinct_technique_on"), true);
    }

    public static void validateActiveState(ServerPlayer player) {
        if (!isActive(player)) {
            return;
        }
        StatsData data = DmzForms.stats(player);
        if (!unlocked(player) || !DmzForms.isSaiyan(player) || data == null
                || !data.getCharacter().hasActiveForm()) {
            deactivate(player, "message.ultrabalancetweaks.instinct_technique_lost_form");
            return;
        }
        String primaryGroup = DmzForms.normalize(data.getCharacter().getActiveFormGroup());
        if ("ultrainstinct".equals(primaryGroup) || "ultraego".equals(primaryGroup)) {
            deactivate(player, "message.ultrabalancetweaks.instinct_technique_lost_form");
        }
    }

    public static void checkMasteryRewards(ServerPlayer player) {
        if (destructionUnlocked(player) && !player.getPersistentData().getBoolean(UE_NOTICE)) {
            player.getPersistentData().putBoolean(UE_NOTICE, true);
            rewardEffect(player, "message.ultrabalancetweaks.destruction_mastered");
        }
        if (unlocked(player) && !player.getPersistentData().getBoolean(UI_NOTICE)) {
            player.getPersistentData().putBoolean(UI_NOTICE, true);
            rewardEffect(player, "message.ultrabalancetweaks.instinct_technique_unlocked");
        }
    }

    private static void deactivate(ServerPlayer player, String messageKey) {
        if (!isActive(player)) {
            return;
        }
        StatsData data = DmzForms.stats(player);
        if (data == null) {
            return;
        }
        data.getCharacter().clearActiveStackForm(player);
        syncNativeState(player);
        activationEffect(player, false);
        player.displayClientMessage(Component.translatable(messageKey), true);
    }

    private static void syncNativeState(ServerPlayer player) {
        NetworkHandler.sendToTrackingEntityAndSelf(new StatsSyncS2C(player), player);
        BalanceNetwork.syncDestruction(player);
    }

    private static void activationEffect(ServerPlayer player, boolean enabled) {
        ServerLevel level = player.serverLevel();
        level.sendParticles(enabled ? ParticleTypes.END_ROD : ParticleTypes.CLOUD,
                player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                enabled ? 38 : 18, 0.45, 0.75, 0.45, enabled ? 0.06 : 0.03);
        level.playSound(null, player.blockPosition(),
                enabled ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE,
                SoundSource.PLAYERS, 0.85f, enabled ? 1.45f : 0.85f);
    }

    private static void rewardEffect(ServerPlayer player, String messageKey) {
        player.sendSystemMessage(Component.translatable(messageKey));
        player.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1.0, player.getZ(), 36, 0.45, 0.7, 0.45, 0.08);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 1.0f, 1.1f);
    }
}
