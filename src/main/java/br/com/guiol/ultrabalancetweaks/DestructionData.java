package br.com.guiol.ultrabalancetweaks;

import net.minecraft.server.level.ServerPlayer;

public final class DestructionData {
    private static final String HAKAI_READY_KEY = "ultrabalancetweaks:hakai_ready_at";
    private static final String SPHERE_READY_KEY = "ultrabalancetweaks:sphere_ready_at";

    private DestructionData() {
    }

    public static int cooldown(ServerPlayer player, DestructionAbility ability) {
        long readyAt = player.getPersistentData().getLong(key(ability));
        long remaining = Math.max(0L, readyAt - player.level().getGameTime());
        return (int) Math.min(Integer.MAX_VALUE, remaining);
    }

    public static boolean ready(ServerPlayer player, DestructionAbility ability) {
        return cooldown(player, ability) == 0;
    }

    public static void startCooldown(ServerPlayer player, DestructionAbility ability, int ticks) {
        player.getPersistentData().putLong(key(ability), player.level().getGameTime() + Math.max(0, ticks));
    }

    public static void clear(ServerPlayer player) {
        player.getPersistentData().remove(HAKAI_READY_KEY);
        player.getPersistentData().remove(SPHERE_READY_KEY);
    }

    private static String key(DestructionAbility ability) {
        return ability == DestructionAbility.HAKAI ? HAKAI_READY_KEY : SPHERE_READY_KEY;
    }
}
