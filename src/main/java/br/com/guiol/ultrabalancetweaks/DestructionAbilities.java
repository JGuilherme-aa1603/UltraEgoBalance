package br.com.guiol.ultrabalancetweaks;

import br.com.guiol.ultrabalancetweaks.network.BalanceNetwork;
import com.dragonminez.common.init.entities.ki.KiBlastEntity;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.joml.Vector3f;

import java.util.Optional;

public final class DestructionAbilities {
    private static final String HAKAI_MARKER = UltraBalanceTweaks.MOD_ID + ":hakai_projectile";
    private static final String SPHERE_MARKER = UltraBalanceTweaks.MOD_ID + ":destruction_sphere";
    private static final int HAKAI_INTERIOR = 0xE9B7FF;
    private static final int HAKAI_EXTERIOR = 0x9700FF;
    private static final int HAKAI_OUTLINE = 0x25003A;
    private static final int SPHERE_INTERIOR = 0xF5D9FF;
    private static final int SPHERE_EXTERIOR = 0x9C20FF;
    private static final int SPHERE_OUTLINE = 0x210032;
    private static final DustParticleOptions PURPLE_DUST =
            new DustParticleOptions(new Vector3f(0.58f, 0.08f, 0.92f), 1.35f);

    private DestructionAbilities() {
    }

    public static void activate(ServerPlayer player, DestructionAbility ability) {
        if (player.isSpectator() || !player.isAlive()) {
            return;
        }
        if (ability == DestructionAbility.HAKAI) {
            activateHakai(player);
        } else {
            activateSphere(player);
        }
    }

    public static boolean isSphereProjectile(Entity entity) {
        return entity instanceof KiBlastEntity && entity.getPersistentData().getBoolean(SPHERE_MARKER);
    }

    public static boolean isDestructionProjectile(Entity entity) {
        return entity instanceof KiBlastEntity && (entity.getPersistentData().getBoolean(HAKAI_MARKER)
                || entity.getPersistentData().getBoolean(SPHERE_MARKER));
    }

    public static void adjustNativeDestructionDamage(LivingHurtEvent event) {
        Entity direct = event.getSource().getDirectEntity();
        if (!(direct instanceof KiBlastEntity projectile)) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (projectile.getPersistentData().getBoolean(HAKAI_MARKER)) {
            float floorRatio = target instanceof Player
                    ? BalanceConfig.HAKAI_PLAYER_DAMAGE_FLOOR.get().floatValue()
                    : BalanceConfig.HAKAI_DAMAGE_FLOOR.get().floatValue();
            float damage = Math.max(event.getAmount(), target.getMaxHealth() * floorRatio);
            damage = Math.max(damage, BalanceConfig.HAKAI_MINIMUM_DAMAGE.get().floatValue());
            boolean execute = canExecute(target)
                    && target.getHealth() / Math.max(1.0f, target.getMaxHealth())
                    <= BalanceConfig.HAKAI_EXECUTION_THRESHOLD.get();
            if (execute) {
                damage = Math.max(damage, target.getHealth() + target.getAbsorptionAmount() + 1.0f);
            }
            event.setAmount(damage);
        } else if (projectile.getPersistentData().getBoolean(SPHERE_MARKER)) {
            event.setAmount(Math.max(event.getAmount(), BalanceConfig.SPHERE_MINIMUM_DAMAGE.get().floatValue()));
        }
    }

    public static boolean tryEraseProjectile(ServerPlayer player, Entity directEntity) {
        if (!(directEntity instanceof Projectile projectile) || projectile.getOwner() == player) {
            return false;
        }
        float gauge = EgoData.gauge(player);
        double threshold = BalanceConfig.AURA_REQUIRED_EGO.get();
        if (gauge < threshold) {
            return false;
        }
        double development = threshold >= 100.0 ? 1.0
                : DmzForms.clamp01((gauge - threshold) / (100.0 - threshold));
        double chance = BalanceConfig.AURA_MAX_ERASURE_CHANCE.get() * development;
        if (player.getRandom().nextDouble() >= chance) {
            return false;
        }

        Vec3 position = projectile.position();
        projectile.discard();
        float newGauge = EgoData.addGauge(player, -BalanceConfig.AURA_EGO_COST.get());
        EgoData.touchCombat(player);
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, position.x, position.y, position.z,
                22, 0.25, 0.25, 0.25, 0.06);
        level.sendParticles(PURPLE_DUST, position.x, position.y, position.z,
                18, 0.22, 0.22, 0.22, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 0.8f, 0.55f);
        BalanceNetwork.syncEgo(player, true, newGauge);
        return true;
    }

    private static void activateHakai(ServerPlayer player) {
        AbilityContext context = validate(player, DestructionAbility.HAKAI,
                BalanceConfig.HAKAI_REQUIRED_EGO.get(), BalanceConfig.HAKAI_KI_COST.get());
        if (context == null) {
            return;
        }

        LivingEntity target = raycastTarget(player, BalanceConfig.HAKAI_RANGE.get());
        if (target == null) {
            message(player, "message.ultrabalancetweaks.no_target");
            return;
        }
        if (isProtectedTarget(player, target)) {
            message(player, "message.ultrabalancetweaks.protected_target");
            return;
        }

        float damage = scaledKiDamage(context, BalanceConfig.HAKAI_KI_DAMAGE_MULTIPLIER.get());
        payAndStart(player, context, DestructionAbility.HAKAI, BalanceConfig.HAKAI_COOLDOWN_TICKS.get());

        KiBlastEntity hakai = new KiBlastEntity(player.level(), player);
        hakai.setup(player, damage, 0.82f, 2.8f, HAKAI_INTERIOR, HAKAI_EXTERIOR, HAKAI_OUTLINE);
        hakai.setKiRenderType(0);
        hakai.setArmorPenetration(100);
        hakai.setBlockDestructionEnabled(false);
        hakai.setHomingTarget(target.getId());
        hakai.setFiring(true);
        hakai.setMaxLife(45);
        hakai.getPersistentData().putBoolean(HAKAI_MARKER, true);

        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.getEyePosition().add(look.scale(0.9));
        hakai.setPos(start.x, start.y - 0.18, start.z);
        hakai.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f,
                hakai.getKiSpeed(), 0.0f);
        player.level().addFreshEntity(hakai);
        player.displayClientMessage(Component.translatable("message.ultrabalancetweaks.hakai_launched",
                target.getDisplayName()), true);
    }

    private static void activateSphere(ServerPlayer player) {
        AbilityContext context = validate(player, DestructionAbility.SPHERE,
                BalanceConfig.SPHERE_REQUIRED_EGO.get(), BalanceConfig.SPHERE_KI_COST.get());
        if (context == null) {
            return;
        }

        float damage = scaledKiDamage(context, BalanceConfig.SPHERE_KI_DAMAGE_MULTIPLIER.get());
        int chargeTicks = BalanceConfig.SPHERE_CHARGE_TICKS.get();
        float speed = BalanceConfig.SPHERE_SPEED.get().floatValue();
        payAndStart(player, context, DestructionAbility.SPHERE, BalanceConfig.SPHERE_COOLDOWN_TICKS.get());

        KiBlastEntity sphere = new KiBlastEntity(player.level(), player);
        sphere.setupKiDeathBall(player, damage, speed,
                SPHERE_INTERIOR, SPHERE_EXTERIOR, SPHERE_OUTLINE, chargeTicks);
        sphere.setSize(BalanceConfig.SPHERE_SIZE.get().floatValue());
        sphere.setArmorPenetration(100);
        sphere.setBlockDestructionEnabled(false);
        sphere.getPersistentData().putBoolean(SPHERE_MARKER, true);
        int travelLife = Math.max(20,
                (int) Math.ceil(BalanceConfig.SPHERE_RANGE.get() / Math.max(0.1, speed)));
        sphere.setMaxLife(chargeTicks + travelLife);

        player.displayClientMessage(Component.translatable("message.ultrabalancetweaks.sphere_charging"), true);
    }

    private static float scaledKiDamage(AbilityContext context, double multiplier) {
        double kiDamage = Math.max(1.0, context.data.getKiDamage());
        return (float) Math.min(Float.MAX_VALUE, kiDamage * multiplier);
    }

    private static AbilityContext validate(ServerPlayer player, DestructionAbility ability,
                                           double requiredGauge, double kiCostRatio) {
        StatsData data = DmzForms.stats(player);
        if (data == null || !data.getStatus().isHasCreatedCharacter()) {
            return null;
        }
        DmzForms.ActiveForm state = DmzForms.active(player);
        boolean ultraEgo = state != null && state.isUltraEgo();
        boolean masteredInBase = DmzForms.isBase(player) && InstinctTechnique.destructionUnlocked(player);
        if (!ultraEgo && !masteredInBase) {
            message(player, InstinctTechnique.destructionUnlocked(player)
                    ? "message.ultrabalancetweaks.destruction_requires_base_or_ego"
                    : "message.ultrabalancetweaks.requires_ultra_ego");
            return null;
        }
        float gauge = ultraEgo ? EgoData.gauge(player) : 100.0f;
        if (ultraEgo && gauge + 1.0E-3 < requiredGauge) {
            player.displayClientMessage(Component.translatable("message.ultrabalancetweaks.requires_ego",
                    Math.round(requiredGauge)), true);
            return null;
        }
        int cooldown = DestructionData.cooldown(player, ability);
        if (cooldown > 0) {
            player.displayClientMessage(Component.translatable("message.ultrabalancetweaks.cooldown",
                    (int) Math.ceil(cooldown / 20.0)), true);
            return null;
        }
        float maximum = Math.max(1.0f, data.getMaxEnergy());
        int cost = Math.max(1, (int) Math.ceil(maximum * kiCostRatio));
        if (data.getResources().getCurrentEnergy() < cost) {
            message(player, "message.ultrabalancetweaks.not_enough_ki");
            return null;
        }
        return new AbilityContext(data, cost);
    }

    private static void payAndStart(ServerPlayer player, AbilityContext context,
                                    DestructionAbility ability, int cooldown) {
        context.data.getResources().removeEnergy(context.kiCost);
        DestructionData.startCooldown(player, ability, cooldown);
        EgoData.touchCombat(player);
        BalanceNetwork.syncDestruction(player);
    }

    private static LivingEntity raycastTarget(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().normalize().scale(range));
        ServerLevel level = player.serverLevel();
        BlockHitResult blockHit = level.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 limit = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
        double bestDistance = start.distanceToSqr(limit);
        LivingEntity bestEntity = null;

        AABB search = new AABB(start, end).inflate(1.25);
        for (Entity entity : level.getEntities(player, search,
                candidate -> candidate instanceof LivingEntity living && living.isAlive()
                        && !living.isSpectator() && living.isPickable())) {
            LivingEntity living = (LivingEntity) entity;
            Optional<Vec3> intersection = living.getBoundingBox().inflate(0.35).clip(start, end);
            if (intersection.isEmpty()) {
                continue;
            }
            double distance = start.distanceToSqr(intersection.get());
            if (distance < bestDistance) {
                bestDistance = distance;
                bestEntity = living;
            }
        }
        return bestEntity;
    }

    private static boolean isProtectedTarget(ServerPlayer caster, LivingEntity target) {
        if (target instanceof ServerPlayer player && (player.isCreative() || player.isSpectator())) {
            return true;
        }
        return target instanceof TamableAnimal tamable && tamable.isOwnedBy(caster);
    }

    private static boolean canExecute(LivingEntity target) {
        return BalanceConfig.HAKAI_EXECUTION_ENABLED.get()
                && !(target instanceof Player)
                && !(target instanceof TamableAnimal)
                && !(target instanceof ArmorStand)
                && !target.getType().is(Tags.EntityTypes.BOSSES);
    }

    private static void message(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }

    private record AbilityContext(StatsData data, int kiCost) {
    }
}
