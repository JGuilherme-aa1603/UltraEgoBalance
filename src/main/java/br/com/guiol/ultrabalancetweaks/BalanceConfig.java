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
    public static final ForgeConfigSpec.DoubleValue HAKAI_REQUIRED_EGO;
    public static final ForgeConfigSpec.DoubleValue HAKAI_KI_COST;
    public static final ForgeConfigSpec.IntValue HAKAI_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.DoubleValue HAKAI_RANGE;
    public static final ForgeConfigSpec.DoubleValue HAKAI_KI_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue HAKAI_MINIMUM_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue HAKAI_DAMAGE_FLOOR;
    public static final ForgeConfigSpec.DoubleValue HAKAI_PLAYER_DAMAGE_FLOOR;
    public static final ForgeConfigSpec.DoubleValue HAKAI_EXECUTION_THRESHOLD;
    public static final ForgeConfigSpec.BooleanValue HAKAI_EXECUTION_ENABLED;
    public static final ForgeConfigSpec.DoubleValue SPHERE_REQUIRED_EGO;
    public static final ForgeConfigSpec.DoubleValue SPHERE_KI_COST;
    public static final ForgeConfigSpec.IntValue SPHERE_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.DoubleValue SPHERE_RANGE;
    public static final ForgeConfigSpec.DoubleValue SPHERE_KI_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue SPHERE_MINIMUM_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue SPHERE_SIZE;
    public static final ForgeConfigSpec.DoubleValue SPHERE_SPEED;
    public static final ForgeConfigSpec.IntValue SPHERE_CHARGE_TICKS;
    public static final ForgeConfigSpec.BooleanValue SPHERE_AFFECTS_PLAYERS;
    public static final ForgeConfigSpec.DoubleValue AURA_REQUIRED_EGO;
    public static final ForgeConfigSpec.DoubleValue AURA_MAX_ERASURE_CHANCE;
    public static final ForgeConfigSpec.DoubleValue AURA_EGO_COST;

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
    public static final ForgeConfigSpec.BooleanValue HUD_SHOW_ABILITIES;

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

        COMMON_BUILDER.push("destruction");
        COMMON_BUILDER.comment("Hakai is a precise finisher. It cannot execute players, tamed pets or entities tagged forge:bosses.");
        HAKAI_REQUIRED_EGO = commonDecimal("hakai_required_ego", 70.0, 0.0, 100.0,
                "Minimum Ego gauge required to use Hakai.");
        HAKAI_KI_COST = commonDecimal("hakai_ki_cost", 0.35, 0.0, 1.0,
                "Fraction of maximum Ki consumed by Hakai.");
        HAKAI_COOLDOWN_TICKS = COMMON_BUILDER.comment("Hakai cooldown in ticks.")
                .defineInRange("hakai_cooldown_ticks", 600, 0, 72000);
        HAKAI_RANGE = commonDecimal("hakai_range", 24.0, 2.0, 128.0,
                "Maximum target distance in blocks.");
        HAKAI_KI_DAMAGE_MULTIPLIER = commonDecimal("hakai_ki_damage_multiplier", 5.0, 0.1, 100.0,
                "Multiplier applied to DragonMineZ's transformed Ki damage. The native projectile may hit directly and again in its compact blast.");
        HAKAI_MINIMUM_DAMAGE = commonDecimal("hakai_minimum_damage", 40.0, 1.0, 1000000.0,
                "Absolute minimum damage, ensuring the technique remains lethal to ordinary vanilla mobs even with unusual stat configs.");
        HAKAI_DAMAGE_FLOOR = commonDecimal("hakai_damage_floor", 0.35, 0.0, 1.0,
                "Minimum fraction of a non-player target's maximum health dealt after DragonMineZ scaling.");
        HAKAI_PLAYER_DAMAGE_FLOOR = commonDecimal("hakai_player_damage_floor", 0.18, 0.0, 1.0,
                "Minimum fraction of a player's maximum health dealt. Players are never executed.");
        HAKAI_EXECUTION_THRESHOLD = commonDecimal("hakai_execution_threshold", 0.15, 0.0, 1.0,
                "Non-boss execution threshold as a fraction of current health.");
        HAKAI_EXECUTION_ENABLED = COMMON_BUILDER.comment("Allow Hakai to finish eligible non-player targets below the threshold.")
                .define("hakai_execution_enabled", true);

        COMMON_BUILDER.comment("Sphere of Destruction uses DragonMineZ's native Death Ball entity, renderer, collision and explosion. Block destruction remains disabled.");
        SPHERE_REQUIRED_EGO = commonDecimal("sphere_required_ego", 50.0, 0.0, 100.0,
                "Minimum Ego gauge required to launch a Sphere of Destruction.");
        SPHERE_KI_COST = commonDecimal("sphere_ki_cost", 0.25, 0.0, 1.0,
                "Fraction of maximum Ki consumed by the sphere.");
        SPHERE_COOLDOWN_TICKS = COMMON_BUILDER.comment("Sphere cooldown in ticks.")
                .defineInRange("sphere_cooldown_ticks", 240, 0, 72000);
        SPHERE_RANGE = commonDecimal("sphere_range", 32.0, 2.0, 128.0,
                "Maximum projectile travel distance in blocks before it dissipates.");
        SPHERE_KI_DAMAGE_MULTIPLIER = commonDecimal("sphere_ki_damage_multiplier", 3.5, 0.1, 100.0,
                "Multiplier applied to DragonMineZ's transformed Ki damage for every target in the native explosion.");
        SPHERE_MINIMUM_DAMAGE = commonDecimal("sphere_minimum_damage", 24.0, 1.0, 1000000.0,
                "Absolute minimum damage. Normal damage at developed levels is much higher because it scales with Ki damage.");
        SPHERE_SIZE = commonDecimal("sphere_size", 3.35, 0.5, 12.0,
                "Native Death Ball visual size. Explosion radius is approximately 1.5 times this value.");
        SPHERE_SPEED = commonDecimal("sphere_speed", 1.15, 0.1, 5.0,
                "Native projectile speed in blocks per tick.");
        SPHERE_CHARGE_TICKS = COMMON_BUILDER.comment("Ticks spent charging the sphere above the player before it fires.")
                .defineInRange("sphere_charge_ticks", 14, 0, 200);
        SPHERE_AFFECTS_PLAYERS = COMMON_BUILDER.comment("Allow the sphere to damage other players.")
                .define("sphere_affects_players", false);

        COMMON_BUILDER.comment("At high Ego, the destruction aura can erase incoming projectiles.");
        AURA_REQUIRED_EGO = commonDecimal("aura_required_ego", 80.0, 0.0, 100.0,
                "Gauge at which projectile erasure begins. Chance scales from zero here to its maximum at 100.");
        AURA_MAX_ERASURE_CHANCE = commonDecimal("aura_max_erasure_chance", 0.40, 0.0, 1.0,
                "Projectile erasure chance at 100 Ego.");
        AURA_EGO_COST = commonDecimal("aura_ego_cost", 5.0, 0.0, 100.0,
                "Ego gauge consumed when the aura erases a projectile.");
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("ultra_instinct");
        COMMON_BUILDER.comment("Exact server-authoritative dodge values. Chance no longer falls with current Ki or mastery; a dodge simply fails when there is not enough Ki to pay its cost.");
        SIGN_DODGE = fixedDodge("sign_exact", 0.70, 0.020);
        MASTERED_DODGE = fixedDodge("mastered_exact", 0.90, 0.018);
        TRUE_DODGE = fixedDodge("true_exact", 0.80, 0.012);

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
        HUD_SHOW_ABILITIES = CLIENT_BUILDER.comment("Show Hakai and Sphere of Destruction readiness below the gauge.")
                .define("show_abilities", true);
        CLIENT_BUILDER.pop();
        CLIENT_SPEC = CLIENT_BUILDER.build();
    }

    private BalanceConfig() {
    }

    private static DodgeTuning fixedDodge(String key, double chanceValue, double costValue) {
        COMMON_BUILDER.push(key + "_dodge");
        ForgeConfigSpec.DoubleValue chance = commonDecimal("chance", chanceValue, 0.0, 1.0,
                "Exact chance to evade an eligible attack.");
        ForgeConfigSpec.DoubleValue cost = commonDecimal("ki_cost", costValue, 0.0, 1.0,
                "Fraction of maximum Ki spent per successful dodge.");
        COMMON_BUILDER.pop();
        return new DodgeTuning(chance, cost);
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

    public record DodgeTuning(ForgeConfigSpec.DoubleValue chance,
                              ForgeConfigSpec.DoubleValue kiCost) {
    }

    public record PrecisionTuning(ForgeConfigSpec.DoubleValue chanceAtFull,
                                  ForgeConfigSpec.DoubleValue damageAtFull) {
    }
}
