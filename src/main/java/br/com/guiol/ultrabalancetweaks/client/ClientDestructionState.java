package br.com.guiol.ultrabalancetweaks.client;

public final class ClientDestructionState {
    private static int hakaiCooldown;
    private static int sphereCooldown;
    private static float hakaiRequirement = 70.0f;
    private static float sphereRequirement = 50.0f;
    private static float auraRequirement = 80.0f;
    private static boolean destructionUnlocked;
    private static boolean instinctTechniqueUnlocked;
    private static boolean instinctTechniqueActive;
    private static int hakaiLevel;
    private static float hakaiMastery;
    private static double battlePower;

    private ClientDestructionState() {
    }

    public static void update(int hakaiTicks, int sphereTicks, float hakaiEgo, float sphereEgo, float auraEgo,
                              boolean learnedDestruction, boolean learnedInstinct, boolean activeInstinct,
                              int level, float mastery, double currentBattlePower) {
        hakaiCooldown = Math.max(0, hakaiTicks);
        sphereCooldown = Math.max(0, sphereTicks);
        hakaiRequirement = hakaiEgo;
        sphereRequirement = sphereEgo;
        auraRequirement = auraEgo;
        destructionUnlocked = learnedDestruction;
        instinctTechniqueUnlocked = learnedInstinct;
        instinctTechniqueActive = activeInstinct;
        hakaiLevel = Math.max(0, Math.min(4, level));
        hakaiMastery = Math.max(0.0f, Math.min(100.0f, mastery));
        battlePower = Math.max(0.0, currentBattlePower);
    }

    public static void tick() {
        if (hakaiCooldown > 0) {
            hakaiCooldown--;
        }
        if (sphereCooldown > 0) {
            sphereCooldown--;
        }
    }

    public static int hakaiCooldown() {
        return hakaiCooldown;
    }

    public static int sphereCooldown() {
        return sphereCooldown;
    }

    public static float hakaiRequirement() {
        return hakaiRequirement;
    }

    public static float sphereRequirement() {
        return sphereRequirement;
    }

    public static float auraRequirement() {
        return auraRequirement;
    }

    public static boolean destructionUnlocked() {
        return destructionUnlocked;
    }

    public static boolean instinctTechniqueUnlocked() {
        return instinctTechniqueUnlocked;
    }

    public static boolean instinctTechniqueActive() {
        return instinctTechniqueActive;
    }

    public static int hakaiLevel() {
        return hakaiLevel;
    }

    public static float hakaiMastery() {
        return hakaiMastery;
    }

    public static double battlePower() {
        return battlePower;
    }

    public static String hakaiLevelRoman() {
        return switch (hakaiLevel) {
            case 4 -> "IV";
            case 3 -> "III";
            case 2 -> "II";
            case 1 -> "I";
            default -> "—";
        };
    }

    public static void clear() {
        hakaiCooldown = 0;
        sphereCooldown = 0;
        destructionUnlocked = false;
        instinctTechniqueUnlocked = false;
        instinctTechniqueActive = false;
        hakaiLevel = 0;
        hakaiMastery = 0.0f;
        battlePower = 0.0;
    }
}
