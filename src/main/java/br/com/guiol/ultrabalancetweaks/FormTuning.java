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
        applyInstinctAuraVisuals();

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
        // DragonMineZ displays RES as the mean of DEF and STM. Keeping both aligned
        // makes the configured resistance value exact in the UI and in calculations.
        data.setStmMultiplier(tuning.defense().get());
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
            blue.setAuraType("kakarot");
            blue.setAuraLayer(1);
            blue.setAuraColor("#35E4FF");
            blue.setExtraAuraType("kakarot");
            blue.setExtraAuraLayer(-1);
            blue.setExtraAuraColor("#FFFFFF");
            blue.setHasLightnings(true);
            blue.setLightningColor("#D6FAFF");
            blue.setTintIntensity(0.0);
        }

        FormConfig.FormData blueEvolved = ConfigManager.getForm(
                "saiyan", "godforms", "super_saiyan_blue_evolved");
        if (blueEvolved != null) {
            blueEvolved.setAuraType("kakarot");
            blueEvolved.setAuraLayer(1);
            blueEvolved.setAuraColor("#49C8FF");
            blueEvolved.setExtraAuraType("kakarot");
            blueEvolved.setExtraAuraLayer(-1);
            blueEvolved.setExtraAuraColor("#FFFFFF");
            blueEvolved.setHasLightnings(true);
            blueEvolved.setLightningColor("#C7F2FF");
            blueEvolved.setTintIntensity(0.0);
        }

        applyInstinctAuraVisuals();
    }

    public static void applyInstinctAuraVisuals() {
        applyTrueInstinctAuraShape(ConfigManager.getForm("saiyan", "ultrainstinct", "sign"));
        applyTrueInstinctAuraShape(ConfigManager.getForm("saiyan", "ultrainstinct", "mastered"));
        applyTrueInstinctAuraShape(ConfigManager.getForm("saiyan", "ultrainstinct", "true"));
        applyTrueInstinctAuraShape(ConfigManager.getStackForm(InstinctTechnique.GROUP, InstinctTechnique.FORM));
    }

    static void applyTrueInstinctAuraShape(FormConfig.FormData form) {
        if (form == null) {
            return;
        }
        // TRUE's distinctive silhouette comes from these two native geometry
        // layers. Keep each stage's own aura colours while sharing the shape.
        form.setAuraType("god");
        form.setAuraLayer(1);
        form.setExtraAuraType("kakarot");
        form.setExtraAuraLayer(2);
    }
}
