package br.com.guiol.ultrabalancetweaks;

import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsData;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

public final class DmzForms {
    private DmzForms() {
    }

    public static ActiveForm active(Player player) {
        StatsData data = StatsProvider.get(StatsCapability.INSTANCE, player).orElse(null);
        if (data == null || !data.getStatus().isHasCreatedCharacter()) {
            return null;
        }
        String group;
        String form;
        double mastery;
        if (data.getCharacter().hasActiveForm()) {
            group = normalize(data.getCharacter().getActiveFormGroup());
            form = normalize(data.getCharacter().getActiveForm());
            mastery = data.getCharacter().getFormMasteries().getMastery(group, form);
        } else if (data.getCharacter().hasActiveStackForm()) {
            group = normalize(data.getCharacter().getActiveStackFormGroup());
            form = normalize(data.getCharacter().getActiveStackForm());
            mastery = data.getCharacter().getStackFormMasteries().getMastery(group, form);
        } else {
            return null;
        }
        return new ActiveForm(data, group, form, clamp01(mastery / 100.0));
    }

    public static boolean isUltraEgo(Player player) {
        ActiveForm state = active(player);
        return state != null && state.isUltraEgo();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    public static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    public record ActiveForm(StatsData data, String group, String form, double masteryRatio) {
        public boolean isUltraEgo() {
            return "ultraego".equals(group);
        }

        public boolean isUltraInstinct() {
            return "ultrainstinct".equals(group)
                    && ("sign".equals(form) || "mastered".equals(form) || "true".equals(form)
                    || "ultrainstinctomen".equals(form));
        }
    }
}
