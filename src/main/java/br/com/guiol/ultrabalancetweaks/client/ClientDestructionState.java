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

    private ClientDestructionState() {
    }

    public static void update(int hakaiTicks, int sphereTicks, float hakaiEgo, float sphereEgo, float auraEgo,
                              boolean learnedDestruction, boolean learnedInstinct, boolean activeInstinct) {
        hakaiCooldown = Math.max(0, hakaiTicks);
        sphereCooldown = Math.max(0, sphereTicks);
        hakaiRequirement = hakaiEgo;
        sphereRequirement = sphereEgo;
        auraRequirement = auraEgo;
        destructionUnlocked = learnedDestruction;
        instinctTechniqueUnlocked = learnedInstinct;
        instinctTechniqueActive = activeInstinct;
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

    public static void clear() {
        hakaiCooldown = 0;
        sphereCooldown = 0;
        destructionUnlocked = false;
        instinctTechniqueUnlocked = false;
        instinctTechniqueActive = false;
    }
}
