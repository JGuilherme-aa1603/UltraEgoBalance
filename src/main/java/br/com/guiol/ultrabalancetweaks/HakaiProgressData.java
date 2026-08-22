package br.com.guiol.ultrabalancetweaks;

import br.com.guiol.ultrabalancetweaks.network.BalanceNetwork;
import com.dragonminez.common.init.entities.IBattlePower;
import com.dragonminez.common.init.entities.MobBattlePowerHelper;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class HakaiProgressData {
    private static final String MASTERY_KEY = UltraBalanceTweaks.MOD_ID + ":hakai_mastery";
    private static final String INITIALIZED_KEY = UltraBalanceTweaks.MOD_ID + ":hakai_mastery_initialized";
    private static final String TRAINING_GRANTED_KEY = UltraBalanceTweaks.MOD_ID + ":hakai_training_granted";

    private HakaiProgressData() {
    }

    public static void ensureInitialized(ServerPlayer player) {
        boolean unlocked = InstinctTechnique.destructionUnlocked(player);
        if (!player.getPersistentData().getBoolean(INITIALIZED_KEY)) {
            player.getPersistentData().putBoolean(INITIALIZED_KEY, true);
            if (unlocked) {
                player.getPersistentData().putFloat(MASTERY_KEY,
                        BalanceConfig.HAKAI_LEGACY_STARTING_MASTERY.get().floatValue());
                player.getPersistentData().putBoolean(TRAINING_GRANTED_KEY, true);
            } else {
                player.getPersistentData().putFloat(MASTERY_KEY, 0.0f);
            }
        }
        if (unlocked && !player.getPersistentData().getBoolean(TRAINING_GRANTED_KEY)) {
            player.getPersistentData().putBoolean(TRAINING_GRANTED_KEY, true);
            player.getPersistentData().putFloat(MASTERY_KEY,
                    Math.max(10.0f, masteryWithoutInitialization(player)));
        }
    }

    public static float mastery(ServerPlayer player) {
        ensureInitialized(player);
        return Math.max(0.0f, Math.min(100.0f, player.getPersistentData().getFloat(MASTERY_KEY)));
    }

    public static float addMastery(ServerPlayer player, double amount) {
        int beforeLevel = level(player);
        float result = Math.max(0.0f, Math.min(100.0f, mastery(player) + (float) amount));
        player.getPersistentData().putFloat(MASTERY_KEY, result);
        int afterLevel = level(player);
        if (afterLevel > beforeLevel) {
            player.sendSystemMessage(Component.translatable("message.ultrabalancetweaks.hakai_level_up",
                    roman(afterLevel)));
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS, 1.0f, 0.72f + afterLevel * 0.12f);
        }
        BalanceNetwork.syncDestruction(player);
        return result;
    }

    public static int level(ServerPlayer player) {
        ensureInitialized(player);
        double power = battlePower(player);
        float mastery = masteryWithoutInitialization(player);
        if (power >= BalanceConfig.HAKAI_LEVEL_4_BATTLE_POWER.get() && mastery >= 100.0f) {
            return 4;
        }
        if (power >= BalanceConfig.HAKAI_LEVEL_3_BATTLE_POWER.get() && mastery >= 50.0f) {
            return 3;
        }
        if (power >= BalanceConfig.HAKAI_LEVEL_2_BATTLE_POWER.get() && mastery >= 25.0f) {
            return 2;
        }
        if (power >= BalanceConfig.HAKAI_LEVEL_1_BATTLE_POWER.get() && mastery >= 10.0f) {
            return 1;
        }
        return 0;
    }

    public static double effectivePower(ServerPlayer player) {
        return battlePower(player) * (1.0 + 0.5 * mastery(player) / 100.0);
    }

    public static double battlePower(LivingEntity entity) {
        if (entity instanceof Player player) {
            StatsData data = DmzForms.stats(player);
            if (data != null && data.getStatus().isHasCreatedCharacter()) {
                return Math.max(1.0, data.getBattlePowerExact());
            }
        }
        if (entity instanceof IBattlePower powered) {
            return Math.max(1.0, powered.getBattlePower());
        }
        try {
            return Math.max(1.0, MobBattlePowerHelper.calculate(entity));
        } catch (RuntimeException ignored) {
            return Math.max(1.0, entity.getMaxHealth() * 20.0);
        }
    }

    public static String roman(int level) {
        return switch (level) {
            case 4 -> "IV";
            case 3 -> "III";
            case 2 -> "II";
            case 1 -> "I";
            default -> "—";
        };
    }

    private static float masteryWithoutInitialization(ServerPlayer player) {
        return Math.max(0.0f, Math.min(100.0f, player.getPersistentData().getFloat(MASTERY_KEY)));
    }
}
