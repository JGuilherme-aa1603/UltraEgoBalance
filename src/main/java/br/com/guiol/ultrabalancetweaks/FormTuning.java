package br.com.guiol.ultrabalancetweaks;

import com.dragonminez.common.config.ConfigManager;
import com.dragonminez.common.config.FormConfig;

import java.util.Map;

public final class FormTuning {
    private FormTuning() {
    }

    public static void apply() {
        int ultraEgoRegistries = applyRuntimeTuning();
        int saiyanForms = applyAttributeMultipliers();

        UltraBalanceTweaks.LOGGER.info(
                "Applied verified tuning to {} Ultra Ego race registries and {} Saiyan form entries",
                ultraEgoRegistries, saiyanForms);
    }

    public static int applyRuntimeTuning() {
        int ultraEgoRegistries = 0;
        for (String race : ConfigManager.getAllRaceStats().keySet()) {
            Map<String, FormConfig> groups = ConfigManager.getAllFormsForRace(race);
            if (groups == null) {
                continue;
            }
            FormConfig ultraEgo = groups.get("ultraego");
            if (ultraEgo == null) {
                continue;
            }
            FormConfig.FormData mastered = ultraEgo.getFormByKey("mastered");
            if (mastered == null) {
                continue;
            }
            mastered.setVitMultiplier(BalanceConfig.EGO_VIT_MULTIPLIER.get());
            mastered.setStaminaDrain(BalanceConfig.EGO_STAMINA_DRAIN.get());
            ultraEgoRegistries++;
        }
        return ultraEgoRegistries;
    }

    public static int applyAttributeMultipliers() {
        int saiyanForms = 0;
        saiyanForms += tuneSaiyan("supersaiyan", "supersaiyanmastered", BalanceConfig.SSJ1_MULTIPLIERS);
        saiyanForms += tuneSaiyan("supersaiyan", "supersaiyan2", BalanceConfig.SSJ2_MULTIPLIERS);
        saiyanForms += tuneSaiyan("supersaiyan", "supersaiyan3", BalanceConfig.SSJ3_MULTIPLIERS);
        saiyanForms += tuneSaiyan("supersaiyan", "supersaiyan4", BalanceConfig.SSJ4_MULTIPLIERS);
        saiyanForms += tuneSaiyan("oozaru", "supersaiyan4", BalanceConfig.SSJ4_MULTIPLIERS);
        saiyanForms += tuneSaiyan("godforms", "super_saiyan_god", BalanceConfig.SSJ_GOD_MULTIPLIERS);
        saiyanForms += tuneSaiyan("godforms", "super_saiyan_blue", BalanceConfig.SSJ_BLUE_MULTIPLIERS);
        saiyanForms += tuneSaiyan("godforms", "super_saiyan_blue_evolved", BalanceConfig.SSJ_BLUE_EVOLVED_MULTIPLIERS);
        saiyanForms += tuneSaiyan("legendaryforms", "ssjfullpower", BalanceConfig.LEGENDARY_SSJ_MULTIPLIERS);
        saiyanForms += tuneSaiyan("ultraego", "mastered", BalanceConfig.ULTRA_EGO_MULTIPLIERS);
        saiyanForms += tuneSaiyan("beastforms", "beast", BalanceConfig.BEAST_MULTIPLIERS);
        saiyanForms += tuneSaiyan("ultrainstinct", "sign", BalanceConfig.UI_SIGN_MULTIPLIERS);
        saiyanForms += tuneSaiyan("ultrainstinct", "mastered", BalanceConfig.UI_MASTERED_MULTIPLIERS);
        saiyanForms += tuneSaiyan("ultrainstinct", "true", BalanceConfig.UI_TRUE_MULTIPLIERS);

        return saiyanForms;
    }

    private static int tuneSaiyan(String group, String form, BalanceConfig.FormMultipliers tuning) {
        FormConfig.FormData data = ConfigManager.getForm("saiyan", group, form);
        if (data == null) {
            UltraBalanceTweaks.LOGGER.warn("Could not tune missing Saiyan form {}.{}", group, form);
            return 0;
        }
        data.setStrMultiplier(tuning.strength().get());
        data.setSkpMultiplier(tuning.skill().get());
        data.setPwrMultiplier(tuning.kiPower().get());
        data.setDefMultiplier(tuning.defense().get());
        data.setMaxStatsMultiplier(1.0);
        return 1;
    }

    public static void applyAuraVisuals() {
        FormConfig.FormData god = ConfigManager.getForm("saiyan", "godforms", "super_saiyan_god");
        if (god != null) {
            god.setAuraType("god");
            god.setAuraLayer(1);
            god.setAuraColor("#FF493D");
            god.setExtraAuraType("kakarot");
            god.setExtraAuraLayer(0);
            god.setExtraAuraColor("#FFB13B");
            god.setHasLightnings(false);
        }

        FormConfig.FormData blue = ConfigManager.getForm("saiyan", "godforms", "super_saiyan_blue");
        if (blue != null) {
            blue.setAuraType("god");
            blue.setAuraLayer(1);
            blue.setAuraColor("#35E4FF");
            blue.setExtraAuraType("kakarot");
            blue.setExtraAuraLayer(2);
            blue.setExtraAuraColor("#176BFF");
            blue.setHasLightnings(true);
            blue.setLightningColor("#D6FAFF");
        }
    }
}
