package br.com.guiol.ultrabalancetweaks;

import br.com.guiol.ultrabalancetweaks.network.BalanceNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class InstinctCounterData {
    private static final String TARGET_KEY = UltraBalanceTweaks.MOD_ID + ":counter_target";
    private static final String FORM_KEY = UltraBalanceTweaks.MOD_ID + ":counter_form";
    private static final String EXPIRES_KEY = UltraBalanceTweaks.MOD_ID + ":counter_expires";
    private static final String COOLDOWN_KEY = UltraBalanceTweaks.MOD_ID + ":counter_cooldown";

    private InstinctCounterData() {
    }

    public static void arm(ServerPlayer player, LivingEntity attacker, String form) {
        long now = player.level().getGameTime();
        CompoundTag tag = player.getPersistentData();
        if (now < tag.getLong(COOLDOWN_KEY)) {
            return;
        }
        BalanceConfig.CounterTuning tuning = tuning(form);
        int window = tuning.windowTicks().get();
        tag.putUUID(TARGET_KEY, attacker.getUUID());
        tag.putString(FORM_KEY, normalizeForm(form));
        tag.putLong(EXPIRES_KEY, now + window);
        BalanceNetwork.syncCounter(player, window, tuning.damageMultiplier().get().floatValue());
    }

    public static double consume(ServerPlayer player, LivingEntity target) {
        CompoundTag tag = player.getPersistentData();
        long now = player.level().getGameTime();
        if (!tag.hasUUID(TARGET_KEY) || now > tag.getLong(EXPIRES_KEY)) {
            clearWindow(player);
            return 1.0;
        }
        UUID targetId = tag.getUUID(TARGET_KEY);
        if (!targetId.equals(target.getUUID())) {
            return 1.0;
        }

        String armedForm = tag.getString(FORM_KEY);
        String currentForm = currentForm(player);
        if (!armedForm.equals(currentForm)) {
            clearWindow(player);
            return 1.0;
        }

        BalanceConfig.CounterTuning tuning = tuning(armedForm);
        double multiplier = tuning.damageMultiplier().get();
        tag.putLong(COOLDOWN_KEY, now + tuning.cooldownTicks().get());
        clearWindow(player);
        return multiplier;
    }

    public static void execute(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        long now = player.level().getGameTime();
        if (!tag.hasUUID(TARGET_KEY) || now > tag.getLong(EXPIRES_KEY)
                || !tag.getString(FORM_KEY).equals(currentForm(player))) {
            clearWindow(player);
            return;
        }

        if (!(player.serverLevel().getEntity(tag.getUUID(TARGET_KEY)) instanceof LivingEntity target)
                || !target.isAlive() || target == player || player.distanceToSqr(target) > 24.0 * 24.0) {
            clearWindow(player);
            return;
        }

        player.lookAt(EntityAnchorArgument.Anchor.EYES,
                target.getEyePosition().subtract(0.0, target.getBbHeight() * 0.12, 0.0));
        Vec3 approach = target.position().subtract(player.position());
        double horizontal = Math.sqrt(approach.x * approach.x + approach.z * approach.z);
        if (horizontal > 2.35) {
            double impulse = Math.min(1.35, Math.max(0.45, (horizontal - 1.8) * 0.22));
            player.setDeltaMovement(approach.x / horizontal * impulse,
                    Math.max(player.getDeltaMovement().y, 0.12), approach.z / horizontal * impulse);
            player.hurtMarked = true;
        }

        player.resetAttackStrengthTicker();
        player.swing(InteractionHand.MAIN_HAND, true);
        player.attack(target);
        if (tag.hasUUID(TARGET_KEY)) {
            clearWindow(player);
        }
    }

    public static void tick(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.hasUUID(TARGET_KEY)) {
            return;
        }
        if (player.level().getGameTime() > tag.getLong(EXPIRES_KEY) || currentForm(player).isEmpty()) {
            clearWindow(player);
        }
    }

    public static void clear(ServerPlayer player) {
        player.getPersistentData().remove(COOLDOWN_KEY);
        clearWindow(player);
    }

    private static void clearWindow(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.hasUUID(TARGET_KEY)) {
            return;
        }
        tag.remove(TARGET_KEY);
        tag.remove(FORM_KEY);
        tag.remove(EXPIRES_KEY);
        BalanceNetwork.syncCounter(player, 0, 1.0f);
    }

    private static String currentForm(ServerPlayer player) {
        if (InstinctTechnique.isActive(player)) {
            return "sign";
        }
        DmzForms.ActiveForm active = DmzForms.activeUltraInstinct(player);
        return active == null ? "" : normalizeForm(active.form());
    }

    private static String normalizeForm(String form) {
        return "ultrainstinctomen".equals(form) ? "sign" : form;
    }

    private static BalanceConfig.CounterTuning tuning(String form) {
        return switch (normalizeForm(form)) {
            case "mastered" -> BalanceConfig.MASTERED_COUNTER;
            case "true" -> BalanceConfig.TRUE_COUNTER;
            default -> BalanceConfig.SIGN_COUNTER;
        };
    }
}
