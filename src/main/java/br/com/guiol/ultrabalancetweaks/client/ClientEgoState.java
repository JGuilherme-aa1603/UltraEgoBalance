package br.com.guiol.ultrabalancetweaks.client;

public final class ClientEgoState {
    private static boolean active;
    private static float targetGauge;
    private static float displayedGauge;

    private ClientEgoState() {
    }

    public static void update(boolean newActive, float newGauge) {
        active = newActive;
        targetGauge = Math.max(0.0f, Math.min(100.0f, newGauge));
        if (!active) {
            displayedGauge = 0.0f;
        }
    }

    public static boolean active() {
        return active;
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
