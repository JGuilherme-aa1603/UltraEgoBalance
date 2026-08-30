package br.com.guiol.ultrabalancetweaks.client;

import com.dragonminez.common.config.ConfigManager;
import com.dragonminez.common.stats.StatsData;

/**
 * Client-only projection of the server-authoritative Ego Power bonus. The
 * server still owns combat damage; these methods are used only by the V menu.
 */
public final class EgoMenuProjection {
    private static final double EPSILON = 1.0E-6;

    private EgoMenuProjection() {
    }

    public static double kiDamage(StatsData data, double nativeValue) {
        if (!isUltraEgoActive(data)) {
            return nativeValue;
        }
        double nativeTotal = data.getTotalMultiplier("PWR");
        if (Math.abs(nativeTotal) < EPSILON) {
            return nativeValue;
        }
        return nativeValue * effectivePowerMultiplier(data, nativeTotal) / nativeTotal;
    }

    public static double totalMultiplier(StatsData data, String statName, double nativeValue) {
        if (!"PWR".equalsIgnoreCase(statName) || !isUltraEgoActive(data)) {
            return nativeValue;
        }
        return effectivePowerMultiplier(data, nativeValue);
    }

    private static double effectivePowerMultiplier(StatsData data, double nativeTotal) {
        double nativeForm = Math.max(EPSILON, data.getFormMultiplier("PWR"));
        double serverBase = ClientEgoState.basePowerMultiplier();
        double correctedBaseTotal;
        if (ConfigManager.getServerConfig().getGameplay().getMultiplicationInsteadOfAdditionForMultipliers()) {
            correctedBaseTotal = nativeTotal / nativeForm * serverBase;
        } else {
            correctedBaseTotal = nativeTotal + serverBase - nativeForm;
        }

        // Combat applies currentPower/basePower after DragonMineZ has calculated
        // the normal transformed damage. Mirroring that exact order keeps the V
        // menu predictive without changing gameplay on the client.
        return correctedBaseTotal * ClientEgoState.currentPowerMultiplier() / serverBase;
    }

    private static boolean isUltraEgoActive(StatsData data) {
        return ClientEgoState.active()
                && data != null
                && data.getCharacter() != null
                && data.getCharacter().hasActiveForm()
                && "ultraego".equalsIgnoreCase(data.getCharacter().getActiveFormGroup());
    }
}
