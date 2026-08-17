package br.com.guiol.ultrabalancetweaks;

import br.com.guiol.ultrabalancetweaks.network.BalanceNetwork;
import com.dragonminez.common.events.DMZEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.unofficial.unofficialdmzaddon.UnofficialDMZConfig;
import org.unofficial.unofficialdmzaddon.network.AddonNetwork;

public final class BalanceEvents {
    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        FormTuning.apply();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void protectPlayersFromSphere(LivingAttackEvent event) {
        if (!BalanceConfig.SPHERE_AFFECTS_PLAYERS.get()
                && event.getEntity() instanceof Player
                && DestructionAbilities.isSphereProjectile(event.getSource().getDirectEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDestructionAura(LivingAttackEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer victim)
                || !DmzForms.isUltraEgo(victim)) {
            return;
        }
        Entity directEntity = event.getSource().getDirectEntity();
        if (directEntity != null && DestructionAbilities.tryEraseProjectile(victim, directEntity)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUltraInstinctAttack(LivingAttackEvent event) {
        if (event.isCanceled()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer victim)) {
            return;
        }
        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof LivingEntity attacker) || sourceEntity == victim) {
            return;
        }

        DmzForms.ActiveForm state = DmzForms.active(victim);
        if (state == null || !state.isUltraInstinct()) {
            return;
        }

        BalanceConfig.DodgeTuning tuning = dodgeTuning(state.form());
        double mastery = state.masteryRatio();
        double minChance = lerp(tuning.minAtZero().get(), tuning.minAtFull().get(), mastery);
        double maxChance = lerp(tuning.maxAtZero().get(), tuning.maxAtFull().get(), mastery);
        float maxEnergy = Math.max(1.0f, state.data().getMaxEnergy());
        float currentEnergy = Math.max(0.0f, state.data().getResources().getCurrentEnergy());
        double kiRatio = DmzForms.clamp01(currentEnergy / maxEnergy);
        double chance = lerp(minChance, maxChance, kiRatio);

        if (victim.getRandom().nextDouble() >= chance) {
            return;
        }

        double costRatio = lerp(tuning.costAtZero().get(), tuning.costAtFull().get(), mastery);
        int kiCost = Math.max(1, (int) Math.ceil(maxEnergy * costRatio));
        if (currentEnergy < kiCost) {
            return;
        }

        state.data().getResources().removeEnergy(kiCost);
        event.setCanceled(true);
        playDodge(victim, attacker, state.form(), mastery);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void captureCombatBeforeSpecialBuffs(LivingHurtEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0.0f) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer victim) {
            DmzForms.ActiveForm victimForm = DmzForms.active(victim);
            if (victimForm != null && victimForm.isUltraEgo()) {
                float before = EgoData.gauge(victim);
                double healthFraction = event.getAmount() / Math.max(1.0f, victim.getMaxHealth());
                float after = EgoData.addGauge(victim,
                        healthFraction * 100.0 * BalanceConfig.EGO_GAUGE_CONVERSION.get());
                EgoData.touchCombat(victim);
                if (before < 100.0f && after >= 100.0f) {
                    playFullEgoEffect(victim);
                }
            }
        }

        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity instanceof ServerPlayer attacker && DmzForms.isUltraEgo(attacker)) {
            EgoData.touchCombat(attacker);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void applyBalancedSpecialDamage(LivingHurtEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0.0f) {
            return;
        }
        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity instanceof ServerPlayer attacker && attacker != event.getEntity()) {
            DmzForms.ActiveForm attackerForm = DmzForms.active(attacker);
            if (attackerForm != null && attackerForm.isUltraEgo()) {
                replaceOriginalUltraEgoMultiplier(event, attacker);
            } else if (attackerForm != null && attackerForm.isUltraInstinct()) {
                applyUltraInstinctPrecision(event, attacker, attackerForm);
            }
        }

        if (event.getEntity() instanceof ServerPlayer victim) {
            DmzForms.ActiveForm victimForm = DmzForms.active(victim);
            if (victimForm != null && victimForm.isUltraEgo()) {
                boolean originalBuffEnabled = UnofficialDMZConfig.SPECIAL_FORM_BUFFS.get();
                double originalMultiplier = originalBuffEnabled ? 0.95 : 1.0;
                event.setAmount((float) (event.getAmount()
                        * BalanceConfig.EGO_DAMAGE_TAKEN_MULTIPLIER.get() / originalMultiplier));
            }
        }

        DestructionAbilities.adjustNativeDestructionDamage(event);
    }

    @SubscribeEvent
    public void applyDestructionPenetration(DMZEvent.DamageModifyEvent event) {
        Player attacker = event.getAttacker();
        if (!(attacker instanceof ServerPlayer serverPlayer) || !DmzForms.isUltraEgo(serverPlayer)) {
            return;
        }
        double ratio = EgoData.gauge(serverPlayer) / 100.0;
        event.setDefensePenetration(event.getDefensePenetration()
                + BalanceConfig.EGO_MAX_DEFENSE_PENETRATION.get() * ratio);
    }

    @SubscribeEvent
    public void onHealing(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0.0f
                || !DmzForms.isUltraEgo(player)) {
            return;
        }
        double healedPercent = event.getAmount() / Math.max(1.0f, player.getMaxHealth()) * 100.0;
        EgoData.addGauge(player, -healedPercent * BalanceConfig.EGO_HEAL_GAUGE_LOSS.get());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        boolean active = DmzForms.isUltraEgo(player);
        float gauge = EgoData.gauge(player);
        if (!active && gauge > 0.0f) {
            gauge = EgoData.setGauge(player, 0.0f);
        } else if (active && player.tickCount % 20 == 0
                && EgoData.ticksSinceCombat(player) >= BalanceConfig.EGO_DECAY_DELAY_TICKS.get()) {
            gauge = EgoData.addGauge(player, -BalanceConfig.EGO_DECAY_PER_SECOND.get());
        }

        if (EgoData.shouldSync(player, active, gauge)) {
            BalanceNetwork.syncEgo(player, active, gauge);
        }
        if (player.tickCount % 10 == 0) {
            BalanceNetwork.syncDestruction(player);
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            boolean active = DmzForms.isUltraEgo(player);
            BalanceNetwork.syncEgo(player, active, active ? EgoData.gauge(player) : 0.0f);
            BalanceNetwork.syncDestruction(player);
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EgoData.setGauge(player, 0.0f);
            BalanceNetwork.syncEgo(player, false, 0.0f);
        }
    }

    private static void replaceOriginalUltraEgoMultiplier(LivingHurtEvent event, ServerPlayer attacker) {
        boolean originalBuffEnabled = UnofficialDMZConfig.SPECIAL_FORM_BUFFS.get();
        double healthMissing = 1.0 - attacker.getHealth() / Math.max(1.0f, attacker.getMaxHealth());
        double originalMultiplier = originalBuffEnabled ? 1.05 + 0.30 * healthMissing : 1.0;
        double egoRatio = EgoData.gauge(attacker) / 100.0;
        double newMultiplier = lerp(1.05, BalanceConfig.EGO_MAX_DAMAGE_MULTIPLIER.get(), egoRatio);
        event.setAmount((float) (event.getAmount() / originalMultiplier * newMultiplier));
    }

    private static void applyUltraInstinctPrecision(LivingHurtEvent event, ServerPlayer attacker,
                                                     DmzForms.ActiveForm state) {
        BalanceConfig.PrecisionTuning tuning = precisionTuning(state.form());
        double development = 0.5 + 0.5 * state.masteryRatio();
        double chance = tuning.chanceAtFull().get() * development;
        if (attacker.getRandom().nextDouble() >= chance) {
            return;
        }
        double multiplier = 1.0 + (tuning.damageAtFull().get() - 1.0) * development;
        event.setAmount((float) (event.getAmount() * multiplier));
        if (attacker.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.CRIT, event.getEntity().getX(),
                    event.getEntity().getY() + event.getEntity().getBbHeight() * 0.55,
                    event.getEntity().getZ(), 14, 0.18, 0.25, 0.18, 0.04);
            level.playSound(null, event.getEntity().blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                    SoundSource.PLAYERS, 0.8f, 1.15f);
        }
    }

    private static void playDodge(ServerPlayer victim, LivingEntity attacker, String form, double mastery) {
        Vec3 direction = victim.position().subtract(attacker.position());
        if (direction.lengthSqr() < 1.0E-4) {
            direction = victim.getLookAngle().scale(-1.0);
        }
        direction = direction.normalize();
        double baseDistance = switch (form) {
            case "mastered" -> 1.12;
            case "true" -> 1.25;
            default -> 0.88;
        };
        double distance = baseDistance + 0.20 * mastery;
        Vec3 movement = victim.getDeltaMovement();
        victim.setDeltaMovement(direction.x * distance, Math.max(movement.y, 0.15), direction.z * distance);
        victim.hurtMarked = true;

        boolean leanRight = victim.getPersistentData().getBoolean("ultrabalancetweaks:dodge_side");
        victim.getPersistentData().putBoolean("ultrabalancetweaks:dodge_side", !leanRight);
        AddonNetwork.sendDodge(victim, leanRight);

        if (victim.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.END_ROD, victim.getX(), victim.getY() + victim.getBbHeight() * 0.5,
                    victim.getZ(), 18, 0.30, 0.42, 0.30, 0.01);
            level.sendParticles(ParticleTypes.CLOUD, victim.getX(), victim.getY() + 0.1,
                    victim.getZ(), 8, 0.14, 0.02, 0.14, 0.025);
            level.playSound(null, victim.blockPosition(), SoundEvents.PLAYER_ATTACK_NODAMAGE,
                    SoundSource.PLAYERS, 0.9f, 1.25f);
        }
    }

    private static void playFullEgoEffect(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        level.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + player.getBbHeight() * 0.5,
                player.getZ(), 42, 0.55, 0.75, 0.55, 0.04);
        level.sendParticles(ParticleTypes.WITCH, player.getX(), player.getY() + player.getBbHeight() * 0.55,
                player.getZ(), 28, 0.45, 0.60, 0.45, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE,
                SoundSource.PLAYERS, 1.0f, 0.72f);
        player.displayClientMessage(Component.translatable("gui.ultrabalancetweaks.ego_full"), true);
    }

    private static BalanceConfig.DodgeTuning dodgeTuning(String form) {
        return switch (form) {
            case "mastered" -> BalanceConfig.MASTERED_DODGE;
            case "true" -> BalanceConfig.TRUE_DODGE;
            default -> BalanceConfig.SIGN_DODGE;
        };
    }

    private static BalanceConfig.PrecisionTuning precisionTuning(String form) {
        return switch (form) {
            case "mastered" -> BalanceConfig.MASTERED_PRECISION;
            case "true" -> BalanceConfig.TRUE_PRECISION;
            default -> BalanceConfig.SIGN_PRECISION;
        };
    }

    private static double lerp(double start, double end, double ratio) {
        return start + (end - start) * DmzForms.clamp01(ratio);
    }
}
