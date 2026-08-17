package br.com.guiol.ultrabalancetweaks.client;

public final class ClientDestructionState {
    private static int hakaiCooldown;
    private static int sphereCooldown;
    private static float hakaiRequirement = 70.0f;
    private static float sphereRequirement = 50.0f;
    private static float auraRequirement = 80.0f;

    private ClientDestructionState() {
    }

    public static void update(int hakaiTicks, int sphereTicks, float hakaiEgo, float sphereEgo, float auraEgo) {
        hakaiCooldown = Math.max(0, hakaiTicks);
        sphereCooldown = Math.max(0, sphereTicks);
        hakaiRequirement = hakaiEgo;
        sphereRequirement = sphereEgo;
        auraRequirement = auraEgo;
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

    public static void clear() {
        hakaiCooldown = 0;
        sphereCooldown = 0;
    }
}
