package br.com.guiol.ultrabalancetweaks;

import com.dragonminez.common.config.ConfigManager;
import com.dragonminez.common.config.FormConfig;

import java.util.Map;

public final class FormTuning {
    private FormTuning() {
    }

    public static void apply() {
        int changed = 0;
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
            changed++;
        }
        UltraBalanceTweaks.LOGGER.info("Applied non-persistent Ultra Ego tuning to {} race form registries", changed);
    }
}
