package br.com.guiol.ultrabalancetweaks;

import br.com.guiol.ultrabalancetweaks.network.BalanceNetwork;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DestructionAbilities {
    public static final ResourceKey<DamageType> DESTRUCTION_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(UltraBalanceTweaks.MOD_ID, "destruction"));

    private static final DustParticleOptions PURPLE_DUST =
            new DustParticleOptions(new Vector3f(0.58f, 0.08f, 0.92f), 1.35f);
    private static final DustParticleOptions BRIGHT_DUST =
            new DustParticleOptions(new Vector3f(1.0f, 0.22f, 1.0f), 1.05f);
    private static final List<PendingSphere> PENDING_SPHERES = new ArrayList<>();

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

    public static boolean isDestructionDamage(DamageSource source) {
        return source.is(DESTRUCTION_DAMAGE);
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

    public static void tickSpheres(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING_SPHERES.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        Iterator<PendingSphere> iterator = PENDING_SPHERES.iterator();
        while (iterator.hasNext()) {
            PendingSphere sphere = iterator.next();
            ServerPlayer owner = server.getPlayerList().getPlayer(sphere.owner);
            ServerLevel level = server.getLevel(sphere.dimension);
            if (owner == null || level == null || !owner.isAlive()) {
                iterator.remove();
                continue;
            }

            sphere.age++;
            double progress = Math.min(1.0, sphere.age / (double) sphere.travelTicks);
            double eased = 1.0 - Math.pow(1.0 - progress, 2.0);
            Vec3 position = sphere.start.lerp(sphere.target, eased);
            renderTravel(level, position, sphere.age);
            if (sphere.age >= sphere.travelTicks) {
                detonate(level, owner, sphere.target);
                iterator.remove();
            }
        }
    }

    private static void activateHakai(ServerPlayer player) {
        AbilityContext context = validate(player, DestructionAbility.HAKAI,
                BalanceConfig.HAKAI_REQUIRED_EGO.get(), BalanceConfig.HAKAI_KI_COST.get());
        if (context == null) {
            return;
        }

        AimResult aim = raycast(player, BalanceConfig.HAKAI_RANGE.get());
        LivingEntity target = aim.entity;
        if (target == null) {
            message(player, "message.ultrabalancetweaks.no_target");
            return;
        }
        if (isProtectedTarget(player, target)) {
            message(player, "message.ultrabalancetweaks.protected_target");
            return;
        }

        DamageSource source = destructionSource(player.serverLevel(), player);
        if (target.isInvulnerableTo(source)) {
            message(player, "message.ultrabalancetweaks.protected_target");
            return;
        }

        payAndStart(player, context, DestructionAbility.HAKAI, BalanceConfig.HAKAI_COOLDOWN_TICKS.get());
        boolean execute = canExecute(target)
                && target.getHealth() / Math.max(1.0f, target.getMaxHealth())
                <= BalanceConfig.HAKAI_EXECUTION_THRESHOLD.get();
        float ratio = target instanceof Player
                ? BalanceConfig.HAKAI_PLAYER_DAMAGE_RATIO.get().floatValue()
                : BalanceConfig.HAKAI_DAMAGE_RATIO.get().floatValue();
        float damage = Math.max(1.0f, target.getMaxHealth() * ratio);
        if (execute) {
            damage = Math.max(damage, target.getHealth() + target.getAbsorptionAmount() + 1.0f);
        }

        renderHakai(player.serverLevel(), player.getEyePosition(), target.getBoundingBox().getCenter());
        target.invulnerableTime = 0;
        target.hurt(source, damage);
        player.displayClientMessage(Component.translatable(
                execute ? "message.ultrabalancetweaks.hakai_erased" : "message.ultrabalancetweaks.hakai_hit",
                target.getDisplayName()), true);
    }

    private static void activateSphere(ServerPlayer player) {
        AbilityContext context = validate(player, DestructionAbility.SPHERE,
                BalanceConfig.SPHERE_REQUIRED_EGO.get(), BalanceConfig.SPHERE_KI_COST.get());
        if (context == null) {
            return;
        }
        AimResult aim = raycast(player, BalanceConfig.SPHERE_RANGE.get());
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.getEyePosition().add(look.scale(1.25));
        Vec3 target = aim.position;
        payAndStart(player, context, DestructionAbility.SPHERE, BalanceConfig.SPHERE_COOLDOWN_TICKS.get());
        PENDING_SPHERES.add(new PendingSphere(player.getUUID(), player.level().dimension(),
                start, target, BalanceConfig.SPHERE_TRAVEL_TICKS.get()));

        ServerLevel level = player.serverLevel();
        level.sendParticles(PURPLE_DUST, start.x, start.y, start.z, 28, 0.28, 0.28, 0.28, 0.035);
        level.sendParticles(ParticleTypes.DRAGON_BREATH, start.x, start.y, start.z,
                18, 0.22, 0.22, 0.22, 0.025);
        level.playSound(null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE,
                SoundSource.PLAYERS, 1.0f, 0.58f);
        player.displayClientMessage(Component.translatable("message.ultrabalancetweaks.sphere_launched"), true);
    }

    private static AbilityContext validate(ServerPlayer player, DestructionAbility ability,
                                           double requiredGauge, double kiCostRatio) {
        DmzForms.ActiveForm state = DmzForms.active(player);
        if (state == null || !state.isUltraEgo()) {
            message(player, "message.ultrabalancetweaks.requires_ultra_ego");
            return null;
        }
        float gauge = EgoData.gauge(player);
        if (gauge + 1.0E-3 < requiredGauge) {
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
        float maximum = Math.max(1.0f, state.data().getMaxEnergy());
        int cost = Math.max(1, (int) Math.ceil(maximum * kiCostRatio));
        if (state.data().getResources().getCurrentEnergy() < cost) {
            message(player, "message.ultrabalancetweaks.not_enough_ki");
            return null;
        }
        return new AbilityContext(state, cost);
    }

    private static void payAndStart(ServerPlayer player, AbilityContext context,
                                    DestructionAbility ability, int cooldown) {
        context.state.data().getResources().removeEnergy(context.kiCost);
        DestructionData.startCooldown(player, ability, cooldown);
        EgoData.touchCombat(player);
        BalanceNetwork.syncDestruction(player);
    }

    private static AimResult raycast(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().normalize().scale(range));
        ServerLevel level = player.serverLevel();
        BlockHitResult blockHit = level.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 limit = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
        double bestDistance = start.distanceToSqr(limit);
        LivingEntity bestEntity = null;
        Vec3 bestPosition = limit;

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
                bestPosition = intersection.get();
            }
        }
        return new AimResult(bestEntity, bestPosition);
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

    private static DamageSource destructionSource(ServerLevel level, ServerPlayer owner) {
        Holder<DamageType> holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DESTRUCTION_DAMAGE);
        return new DamageSource(holder, owner);
    }

    private static void renderHakai(ServerLevel level, Vec3 start, Vec3 end) {
        int steps = Math.max(8, (int) Math.ceil(start.distanceTo(end) * 2.0));
        for (int i = 0; i <= steps; i++) {
            Vec3 point = start.lerp(end, i / (double) steps);
            level.sendParticles(i % 3 == 0 ? BRIGHT_DUST : PURPLE_DUST,
                    point.x, point.y, point.z, 1, 0.01, 0.01, 0.01, 0.0);
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, end.x, end.y, end.z,
                36, 0.35, 0.45, 0.35, 0.08);
        level.sendParticles(ParticleTypes.DRAGON_BREATH, end.x, end.y, end.z,
                26, 0.28, 0.38, 0.28, 0.04);
        level.playSound(null, end.x, end.y, end.z, SoundEvents.RESPAWN_ANCHOR_DEPLETE.get(),
                SoundSource.PLAYERS, 1.15f, 0.62f);
    }

    private static void renderTravel(ServerLevel level, Vec3 position, int age) {
        double angle = age * 0.72;
        for (int i = 0; i < 8; i++) {
            double theta = angle + Math.PI * 2.0 * i / 8.0;
            double radius = 0.32 + 0.05 * Math.sin(age * 0.45);
            level.sendParticles(i % 2 == 0 ? BRIGHT_DUST : PURPLE_DUST,
                    position.x + Math.cos(theta) * radius,
                    position.y + Math.sin(theta * 2.0) * 0.18,
                    position.z + Math.sin(theta) * radius,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
        level.sendParticles(ParticleTypes.DRAGON_BREATH, position.x, position.y, position.z,
                5, 0.12, 0.12, 0.12, 0.01);
    }

    private static void detonate(ServerLevel level, ServerPlayer owner, Vec3 center) {
        double radius = BalanceConfig.SPHERE_RADIUS.get();
        DamageSource source = destructionSource(level, owner);
        AABB area = new AABB(center, center).inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity.isAlive() && entity != owner)) {
            if (target instanceof Player && !BalanceConfig.SPHERE_AFFECTS_PLAYERS.get()) {
                continue;
            }
            if (target instanceof TamableAnimal tamable && tamable.isOwnedBy(owner)) {
                continue;
            }
            double distance = target.position().distanceTo(center);
            if (distance > radius) {
                continue;
            }
            float ratio = target instanceof Player
                    ? BalanceConfig.SPHERE_PLAYER_DAMAGE_RATIO.get().floatValue()
                    : BalanceConfig.SPHERE_DAMAGE_RATIO.get().floatValue();
            float falloff = (float) Math.max(0.35, 1.0 - distance / radius * 0.65);
            float damage = Math.max(1.0f, target.getMaxHealth() * ratio * falloff);
            target.invulnerableTime = 0;
            target.hurt(source, damage);
            Vec3 push = target.position().subtract(center);
            if (push.lengthSqr() > 1.0E-4) {
                push = push.normalize().scale(0.45 + 0.75 * (1.0 - distance / radius));
                target.push(push.x, Math.max(0.18, push.y + 0.18), push.z);
            }
        }

        level.sendParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y, center.z,
                110, radius * 0.38, radius * 0.38, radius * 0.38, 0.08);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z,
                90, radius * 0.32, radius * 0.32, radius * 0.32, 0.12);
        for (int i = 0; i < 32; i++) {
            double angle = Math.PI * 2.0 * i / 32.0;
            level.sendParticles(i % 2 == 0 ? BRIGHT_DUST : PURPLE_DUST,
                    center.x + Math.cos(angle) * radius,
                    center.y + 0.15 * Math.sin(angle * 3.0),
                    center.z + Math.sin(angle) * radius,
                    2, 0.04, 0.04, 0.04, 0.0);
        }
        level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS, 1.4f, 0.52f);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.RESPAWN_ANCHOR_DEPLETE.get(),
                SoundSource.PLAYERS, 1.1f, 0.72f);
    }

    private static void message(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }

    private record AbilityContext(DmzForms.ActiveForm state, int kiCost) {
    }

    private record AimResult(LivingEntity entity, Vec3 position) {
    }

    private static final class PendingSphere {
        private final UUID owner;
        private final ResourceKey<Level> dimension;
        private final Vec3 start;
        private final Vec3 target;
        private final int travelTicks;
        private int age;

        private PendingSphere(UUID owner, ResourceKey<Level> dimension,
                              Vec3 start, Vec3 target, int travelTicks) {
            this.owner = owner;
            this.dimension = dimension;
            this.start = start;
            this.target = target;
            this.travelTicks = travelTicks;
        }
    }
}
