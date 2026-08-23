package br.com.guiol.ultrabalancetweaks;

import br.com.guiol.ultrabalancetweaks.network.BalanceNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class InstinctCounterData {
    private static final String TARGET_KEY = UltraBalanceTweaks.MOD_ID + ":counter_target";
    private static final String FORM_KEY = UltraBalanceTweaks.MOD_ID + ":counter_form";
    private static final String EXPIRES_KEY = UltraBalanceTweaks.MOD_ID + ":counter_expires";
    private static final String COOLDOWN_KEY = UltraBalanceTweaks.MOD_ID + ":counter_cooldown";
    private static final String EXECUTING_KEY = UltraBalanceTweaks.MOD_ID + ":counter_executing";
    private static final double MAX_EXECUTION_RANGE = 48.0;
    private static final double ATTACK_GAP = 2.65;
    private static final int EXECUTION_GRACE_TICKS = 30;

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
        tag.remove(EXECUTING_KEY);
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
        if (tag.getBoolean(EXECUTING_KEY)) {
            return;
        }
        if (!tag.hasUUID(TARGET_KEY) || now > tag.getLong(EXPIRES_KEY)
                || !tag.getString(FORM_KEY).equals(currentForm(player))) {
            clearWindow(player);
            return;
        }

        LivingEntity target = counterTarget(player, tag);
        if (target == null || player.distanceToSqr(target) > MAX_EXECUTION_RANGE * MAX_EXECUTION_RANGE) {
            clearWindow(player);
            return;
        }

        long executionExpiry = Math.max(tag.getLong(EXPIRES_KEY), now + EXECUTION_GRACE_TICKS);
        tag.putLong(EXPIRES_KEY, executionExpiry);
        tag.putBoolean(EXECUTING_KEY, true);
        BalanceNetwork.syncCounter(player, (int) Math.max(1L, executionExpiry - now),
                tuning(tag.getString(FORM_KEY)).damageMultiplier().get().floatValue());
        advanceAndStrike(player, target);
    }

    public static void tick(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.hasUUID(TARGET_KEY)) {
            return;
        }
        if (player.level().getGameTime() > tag.getLong(EXPIRES_KEY)
                || !tag.getString(FORM_KEY).equals(currentForm(player))) {
            clearWindow(player);
            return;
        }
        if (tag.getBoolean(EXECUTING_KEY)) {
            LivingEntity target = counterTarget(player, tag);
            if (target == null || player.distanceToSqr(target) > MAX_EXECUTION_RANGE * MAX_EXECUTION_RANGE) {
                clearWindow(player);
                return;
            }
            advanceAndStrike(player, target);
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
        tag.remove(EXECUTING_KEY);
        BalanceNetwork.syncCounter(player, 0, 1.0f);
    }

    private static LivingEntity counterTarget(ServerPlayer player, CompoundTag tag) {
        if (!tag.hasUUID(TARGET_KEY)) {
            return null;
        }
        if (!(player.serverLevel().getEntity(tag.getUUID(TARGET_KEY)) instanceof LivingEntity target)
                || !target.isAlive() || target == player || target.isSpectator()) {
            return null;
        }
        return target;
    }

    private static void advanceAndStrike(ServerPlayer player, LivingEntity target) {
        player.lookAt(EntityAnchorArgument.Anchor.EYES,
                target.getEyePosition().subtract(0.0, target.getBbHeight() * 0.12, 0.0));

        if (boxGapSqr(player.getBoundingBox(), target.getBoundingBox()) <= ATTACK_GAP * ATTACK_GAP) {
            Vec3 movement = player.getDeltaMovement();
            player.setDeltaMovement(movement.x * 0.2, movement.y, movement.z * 0.2);
            player.hurtMarked = true;
            if (player.getAttackStrengthScale(0.5f) < 0.90f) {
                return;
            }

            player.swing(InteractionHand.MAIN_HAND, true);
            player.attack(target);
            if (player.getPersistentData().hasUUID(TARGET_KEY)) {
                clearWindow(player);
            }
            return;
        }

        double dx = target.getX() - player.getX();
        double dz = target.getZ() - player.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double centerDeltaY = target.getY() + target.getBbHeight() * 0.5
                - (player.getY() + player.getBbHeight() * 0.5);
        double distance = Math.sqrt(horizontal * horizontal + centerDeltaY * centerDeltaY);
        double speed = Math.min(3.25, Math.max(0.85, distance * 0.32));
        double horizontalSpeed = horizontal < 1.0E-4 ? 0.0
                : speed * Math.min(1.0, horizontal / Math.max(1.0E-4, distance));
        double velocityY = Math.max(-0.9, Math.min(1.15, centerDeltaY * 0.28));
        if (player.onGround() && centerDeltaY < 1.1) {
            velocityY = Math.max(0.16, velocityY);
        }

        player.setDeltaMovement(horizontal < 1.0E-4 ? 0.0 : dx / horizontal * horizontalSpeed,
                velocityY,
                horizontal < 1.0E-4 ? 0.0 : dz / horizontal * horizontalSpeed);
        player.fallDistance = 0.0f;
        player.hurtMarked = true;

        if (player.tickCount % 2 == 0) {
            ServerLevel level = player.serverLevel();
            level.sendParticles(ParticleTypes.END_ROD, player.getX(),
                    player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                    5, 0.16, 0.24, 0.16, 0.015);
            level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.1, player.getZ(),
                    3, 0.12, 0.02, 0.12, 0.02);
        }
        if (player.getPersistentData().getLong(EXPIRES_KEY) - player.level().getGameTime()
                == EXECUTION_GRACE_TICKS) {
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                    SoundSource.PLAYERS, 0.65f, 1.55f);
        }
    }

    private static double boxGapSqr(AABB first, AABB second) {
        double dx = Math.max(0.0, Math.max(first.minX - second.maxX, second.minX - first.maxX));
        double dy = Math.max(0.0, Math.max(first.minY - second.maxY, second.minY - first.maxY));
        double dz = Math.max(0.0, Math.max(first.minZ - second.maxZ, second.minZ - first.maxZ));
        return dx * dx + dy * dy + dz * dz;
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
