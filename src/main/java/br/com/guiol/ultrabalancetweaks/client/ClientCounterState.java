package br.com.guiol.ultrabalancetweaks.client;

public final class ClientCounterState {
    private static int remainingTicks;
    private static int maximumTicks;
    private static float multiplier = 1.0f;

    private ClientCounterState() {
    }

    public static void update(int ticks, float damageMultiplier) {
        remainingTicks = Math.max(0, ticks);
        maximumTicks = remainingTicks;
        multiplier = Math.max(1.0f, damageMultiplier);
    }

    public static void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public static boolean active() {
        return remainingTicks > 0;
    }

    public static float multiplier() {
        return multiplier;
    }

    public static float progress() {
        return maximumTicks <= 0 ? 0.0f : Math.min(1.0f, remainingTicks / (float) maximumTicks);
    }

    public static void clear() {
        remainingTicks = 0;
        maximumTicks = 0;
        multiplier = 1.0f;
    }
}
