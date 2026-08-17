package br.com.guiol.ultrabalancetweaks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public final class EgoData {
    private static final String GAUGE_KEY = "ultrabalancetweaks:ego_gauge";
    private static final String LAST_COMBAT_KEY = "ultrabalancetweaks:ego_last_combat";
    private static final String LAST_SYNC_KEY = "ultrabalancetweaks:ego_last_sync";
    private static final String LAST_ACTIVE_KEY = "ultrabalancetweaks:ego_last_active";

    private EgoData() {
    }

    public static float gauge(ServerPlayer player) {
        return clamp(player.getPersistentData().getFloat(GAUGE_KEY));
    }

    public static float setGauge(ServerPlayer player, double value) {
        float clamped = clamp((float) value);
        player.getPersistentData().putFloat(GAUGE_KEY, clamped);
        return clamped;
    }

    public static float addGauge(ServerPlayer player, double amount) {
        return setGauge(player, gauge(player) + amount);
    }

    public static void touchCombat(ServerPlayer player) {
        player.getPersistentData().putLong(LAST_COMBAT_KEY, player.level().getGameTime());
    }

    public static long ticksSinceCombat(ServerPlayer player) {
        long last = player.getPersistentData().getLong(LAST_COMBAT_KEY);
        return Math.max(0L, player.level().getGameTime() - last);
    }

    public static boolean shouldSync(ServerPlayer player, boolean active, float gauge) {
        CompoundTag tag = player.getPersistentData();
        float lastGauge = tag.getFloat(LAST_SYNC_KEY);
        boolean lastActive = tag.getBoolean(LAST_ACTIVE_KEY);
        if (lastActive != active || Math.abs(lastGauge - gauge) >= 0.05f || player.tickCount % 40 == 0) {
            tag.putFloat(LAST_SYNC_KEY, gauge);
            tag.putBoolean(LAST_ACTIVE_KEY, active);
            return true;
        }
        return false;
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(100.0f, value));
    }
}
