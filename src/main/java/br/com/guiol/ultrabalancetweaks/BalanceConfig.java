package br.com.guiol.ultrabalancetweaks;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BalanceConfig {
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue EGO_MAX_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue EGO_GAUGE_CONVERSION;
    public static final ForgeConfigSpec.DoubleValue EGO_MAX_DEFENSE_PENETRATION;
    public static final ForgeConfigSpec.DoubleValue EGO_DAMAGE_TAKEN_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue EGO_HEAL_GAUGE_LOSS;
    public static final ForgeConfigSpec.IntValue EGO_DECAY_DELAY_TICKS;
    public static final ForgeConfigSpec.DoubleValue EGO_DECAY_PER_SECOND;
    public static final ForgeConfigSpec.DoubleValue EGO_VIT_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue EGO_STAMINA_DRAIN;

    public static final DodgeTuning SIGN_DODGE;
    public static final DodgeTuning MASTERED_DODGE;
    public static final DodgeTuning TRUE_DODGE;
    public static final PrecisionTuning SIGN_PRECISION;
    public static final PrecisionTuning MASTERED_PRECISION;
    public static final PrecisionTuning TRUE_PRECISION;

    public static final ForgeConfigSpec.BooleanValue HUD_ENABLED;
    public static final ForgeConfigSpec.IntValue HUD_X_OFFSET;
    public static final ForgeConfigSpec.IntValue HUD_Y_OFFSET;
    public static final ForgeConfigSpec.IntValue HUD_WIDTH;
    public static final ForgeConfigSpec.BooleanValue HUD_SHOW_NUMERIC_VALUE;

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        COMMON_BUILDER.push("ultra_ego");
        EGO_MAX_DAMAGE_MULTIPLIER = commonDecimal("max_damage_multiplier", 1.60, 1.05, 3.0,
                "Final special damage multiplier at 100 Ego. At zero Ego the multiplier is x1.05.");
        EGO_GAUGE_CONVERSION = commonDecimal("gauge_conversion", 1.67, 0.0, 10.0,
                "Gauge gained per percent of max-health damage. 1.67 fills the gauge after about 60% cumulative damage.");
        EGO_MAX_DEFENSE_PENETRATION = commonDecimal("max_defense_penetration", 0.20, 0.0, 1.0,
                "Defense penetration granted at 100 Ego.");
        EGO_DAMAGE_TAKEN_MULTIPLIER = commonDecimal("damage_taken_multiplier", 0.85, 0.05, 1.0,
                "Final incoming damage multiplier while Ultra Ego is active (0.85 means 15% reduction)." );
        EGO_HEAL_GAUGE_LOSS = commonDecimal("heal_gauge_loss_ratio", 0.50, 0.0, 5.0,
                "Gauge points removed per percent of max health restored by normal healing. DMZ passive regeneration does not erase the gauge instantly.");
        EGO_DECAY_DELAY_TICKS = COMMON_BUILDER.comment("Ticks out of combat before Ego begins to decay.")
                .defineInRange("decay_delay_ticks", 200, 0, 72000);
        EGO_DECAY_PER_SECOND = commonDecimal("decay_per_second", 5.0, 0.0, 100.0,
                "Ego gauge lost per second after the decay delay.");
        EGO_VIT_MULTIPLIER = commonDecimal("vit_multiplier", 1.60, 1.0, 20.0,
                "Runtime VIT multiplier. The DragonMineZ JSON is never overwritten.");
        EGO_STAMINA_DRAIN = commonDecimal("stamina_drain", 0.045, 0.0, 2.0,
                "Runtime stamina drain. The DragonMineZ JSON is never overwritten.");
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("ultra_instinct");
        COMMON_BUILDER.comment("Dodge is interpolated by mastery, then by the fraction of Ki currently available.");
        SIGN_DODGE = dodge("sign", 0.20, 0.25, 0.45, 0.60, 0.022, 0.018);
        MASTERED_DODGE = dodge("mastered", 0.25, 0.35, 0.70, 0.90, 0.023, 0.018);
        TRUE_DODGE = dodge("true", 0.25, 0.35, 0.65, 0.80, 0.021, 0.016);

        COMMON_BUILDER.comment("Precision procs start at half the configured chance at zero mastery and reach the full values at 100 mastery.");
        SIGN_PRECISION = precision("sign", 0.10, 1.15);
        MASTERED_PRECISION = precision("mastered", 0.15, 1.20);
        TRUE_PRECISION = precision("true", 0.20, 1.30);
        COMMON_BUILDER.pop();
        COMMON_SPEC = COMMON_BUILDER.build();

        CLIENT_BUILDER.push("ego_hud");
        HUD_ENABLED = CLIENT_BUILDER.comment("Render the Ultra Ego gauge above the hotbar.").define("enabled", true);
        HUD_X_OFFSET = CLIENT_BUILDER.comment("Horizontal offset from screen center.").defineInRange("x_offset", 0, -4096, 4096);
        HUD_Y_OFFSET = CLIENT_BUILDER.comment("Vertical offset from the default position above the hotbar.").defineInRange("y_offset", 0, -4096, 4096);
        HUD_WIDTH = CLIENT_BUILDER.comment("Gauge width in GUI pixels.").defineInRange("width", 142, 90, 320);
        HUD_SHOW_NUMERIC_VALUE = CLIENT_BUILDER.comment("Show the exact percentage inside the gauge.").define("show_numeric_value", true);
        CLIENT_BUILDER.pop();
        CLIENT_SPEC = CLIENT_BUILDER.build();
    }

    private BalanceConfig() {
    }

    private static DodgeTuning dodge(String key, double min0, double min100, double max0, double max100,
                                      double cost0, double cost100) {
        COMMON_BUILDER.push(key + "_dodge");
        ForgeConfigSpec.DoubleValue minAtZero = commonDecimal("min_chance_at_0_mastery", min0, 0.0, 1.0, "Chance with empty Ki at zero mastery.");
        ForgeConfigSpec.DoubleValue minAtFull = commonDecimal("min_chance_at_100_mastery", min100, 0.0, 1.0, "Chance with empty Ki at full mastery.");
        ForgeConfigSpec.DoubleValue maxAtZero = commonDecimal("max_chance_at_0_mastery", max0, 0.0, 1.0, "Chance with full Ki at zero mastery.");
        ForgeConfigSpec.DoubleValue maxAtFull = commonDecimal("max_chance_at_100_mastery", max100, 0.0, 1.0, "Chance with full Ki at full mastery.");
        ForgeConfigSpec.DoubleValue costAtZero = commonDecimal("ki_cost_at_0_mastery", cost0, 0.0, 1.0, "Fraction of maximum Ki spent per successful dodge at zero mastery.");
        ForgeConfigSpec.DoubleValue costAtFull = commonDecimal("ki_cost_at_100_mastery", cost100, 0.0, 1.0, "Fraction of maximum Ki spent per successful dodge at full mastery.");
        COMMON_BUILDER.pop();
        return new DodgeTuning(minAtZero, minAtFull, maxAtZero, maxAtFull, costAtZero, costAtFull);
    }

    private static PrecisionTuning precision(String key, double chance, double damage) {
        COMMON_BUILDER.push(key + "_precision");
        ForgeConfigSpec.DoubleValue chanceValue = commonDecimal("chance_at_100_mastery", chance, 0.0, 1.0, "Proc chance at full mastery.");
        ForgeConfigSpec.DoubleValue damageValue = commonDecimal("damage_multiplier_at_100_mastery", damage, 1.0, 5.0, "Damage multiplier when precision activates at full mastery.");
        COMMON_BUILDER.pop();
        return new PrecisionTuning(chanceValue, damageValue);
    }

    private static ForgeConfigSpec.DoubleValue commonDecimal(String key, double value, double min, double max, String comment) {
        COMMON_BUILDER.comment(comment);
        return COMMON_BUILDER.defineInRange(key, value, min, max);
    }

    public record DodgeTuning(ForgeConfigSpec.DoubleValue minAtZero,
                              ForgeConfigSpec.DoubleValue minAtFull,
                              ForgeConfigSpec.DoubleValue maxAtZero,
                              ForgeConfigSpec.DoubleValue maxAtFull,
                              ForgeConfigSpec.DoubleValue costAtZero,
                              ForgeConfigSpec.DoubleValue costAtFull) {
    }

    public record PrecisionTuning(ForgeConfigSpec.DoubleValue chanceAtFull,
                                  ForgeConfigSpec.DoubleValue damageAtFull) {
    }
}
