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
        StatsData data = stats(player);
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

    public static ActiveForm activeUltraInstinct(Player player) {
        StatsData data = stats(player);
        if (data == null || !data.getStatus().isHasCreatedCharacter()) {
            return null;
        }
        if (data.getCharacter().hasActiveForm()) {
            ActiveForm form = form(data, false);
            if (form != null && form.isUltraInstinct()) {
                return form;
            }
        }
        if (data.getCharacter().hasActiveStackForm()) {
            ActiveForm form = form(data, true);
            if (form != null && form.isUltraInstinct()) {
                return form;
            }
        }
        return null;
    }

    public static StatsData stats(Player player) {
        return StatsProvider.get(StatsCapability.INSTANCE, player).orElse(null);
    }

    public static double mastery(Player player, String group, String form) {
        StatsData data = stats(player);
        if (data == null || !data.getStatus().isHasCreatedCharacter()) {
            return 0.0;
        }
        double normal = data.getCharacter().getFormMasteries().getMastery(group, form);
        double stacked = data.getCharacter().getStackFormMasteries().getMastery(group, form);
        return Math.max(normal, stacked);
    }

    public static boolean hasMastered(Player player, String group, String form) {
        return mastery(player, group, form) + 1.0E-6 >= 100.0;
    }

    public static boolean isBase(Player player) {
        StatsData data = stats(player);
        return data != null && data.getStatus().isHasCreatedCharacter()
                && !data.getCharacter().hasActiveForm()
                && !data.getCharacter().hasActiveStackForm();
    }

    public static boolean isSaiyan(Player player) {
        StatsData data = stats(player);
        return data != null && "saiyan".equals(normalize(data.getCharacter().getRace()));
    }

    public static boolean isUltraEgo(Player player) {
        ActiveForm state = active(player);
        return state != null && state.isUltraEgo();
    }

    static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    public static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static ActiveForm form(StatsData data, boolean stacked) {
        String group = normalize(stacked ? data.getCharacter().getActiveStackFormGroup()
                : data.getCharacter().getActiveFormGroup());
        String form = normalize(stacked ? data.getCharacter().getActiveStackForm()
                : data.getCharacter().getActiveForm());
        double mastery = (stacked ? data.getCharacter().getStackFormMasteries()
                : data.getCharacter().getFormMasteries()).getMastery(group, form);
        return new ActiveForm(data, group, form, clamp01(mastery / 100.0));
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
