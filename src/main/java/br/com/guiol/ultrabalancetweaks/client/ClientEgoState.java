package br.com.guiol.ultrabalancetweaks.client;

public final class ClientEgoState {
    private static boolean active;
    private static float targetGauge;
    private static float displayedGauge;
    private static float basePowerMultiplier = 1.0f;
    private static float maxPowerMultiplier = 1.0f;

    private ClientEgoState() {
    }

    public static void update(boolean newActive, float newGauge,
                              float newBasePowerMultiplier, float newMaxPowerMultiplier) {
        active = newActive;
        targetGauge = Math.max(0.0f, Math.min(100.0f, newGauge));
        basePowerMultiplier = Math.max(0.01f, newBasePowerMultiplier);
        maxPowerMultiplier = Math.max(basePowerMultiplier, newMaxPowerMultiplier);
        if (!active) {
            displayedGauge = 0.0f;
        }
    }

    public static void clear() {
        active = false;
        targetGauge = 0.0f;
        displayedGauge = 0.0f;
        basePowerMultiplier = 1.0f;
        maxPowerMultiplier = 1.0f;
    }

    public static boolean active() {
        return active;
    }

    public static double basePowerMultiplier() {
        return basePowerMultiplier;
    }

    public static double currentPowerMultiplier() {
        return powerMultiplier(targetGauge);
    }

    public static double powerMultiplier(double gauge) {
        double ratio = Math.max(0.0, Math.min(1.0, gauge / 100.0));
        return basePowerMultiplier + (maxPowerMultiplier - basePowerMultiplier) * ratio;
    }

    public static float animatedGauge(float partialTick) {
        float speed = Math.min(1.0f, 0.18f + partialTick * 0.08f);
        displayedGauge += (targetGauge - displayedGauge) * speed;
        if (Math.abs(targetGauge - displayedGauge) < 0.02f) {
            displayedGauge = targetGauge;
        }
        return displayedGauge;
    }
}
