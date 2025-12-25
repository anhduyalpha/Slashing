package com.main.slashing.rpg;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class RpgCastManager {
    private static final List<CastPipeline> PIPELINES = new ArrayList<>();

    private RpgCastManager() {}

    public static void cast(ServerPlayer caster, String skillId) {
        RpgSkillDef skill = RpgDataManager.getSkills().get(skillId);
        if (skill == null) {
            caster.sendSystemMessage(Component.literal("Skill not found: " + skillId));
            return;
        }
        PlayerProfile profile = RpgProfileManager.get(caster);
        long nowMs = getNowMs();
        Long cdEnd = profile.cooldowns().get(skillId);
        if (cdEnd != null && cdEnd > nowMs) {
            long remain = cdEnd - nowMs;
            caster.sendSystemMessage(Component.literal("Skill on cooldown: " + remain + "ms"));
            return;
        }

        if (!hasResources(profile, skill.cast().cost())) {
            caster.sendSystemMessage(Component.literal("Not enough resources."));
            return;
        }

        spendResources(profile, skill.cast().cost());
        gainOnCast(profile);
        profile.cooldowns().put(skillId, nowMs + skill.cast().cooldownMs());

        LivingEntity target = RpgTargeting.resolveTarget(caster, skill.targeting());
        long startTick = getNowTick() + msToTicks(skill.cast().timeMs());
        PIPELINES.add(new CastPipeline(caster, target, skill, startTick));
    }

    public static void tick(MinecraftServer server) {
        if (PIPELINES.isEmpty()) return;
        long nowTick = getNowTick();
        Iterator<CastPipeline> it = PIPELINES.iterator();
        while (it.hasNext()) {
            CastPipeline pipeline = it.next();
            if (pipeline.isExpired()) {
                it.remove();
                continue;
            }
            if (nowTick < pipeline.nextTick) continue;
            boolean keep = pipeline.tick();
            if (!keep) it.remove();
        }
    }

    private static boolean hasResources(PlayerProfile profile, Map<String, Double> cost) {
        for (var entry : cost.entrySet()) {
            ResourceState state = profile.resources().get(entry.getKey());
            if (state == null) return false;
            if (state.current() < entry.getValue()) return false;
        }
        return true;
    }

    private static void spendResources(PlayerProfile profile, Map<String, Double> cost) {
        for (var entry : cost.entrySet()) {
            ResourceState state = profile.resources().get(entry.getKey());
            if (state == null) continue;
            state.setCurrent(state.current() - entry.getValue());
        }
    }

    private static void gainOnCast(PlayerProfile profile) {
        for (ResourceState state : profile.resources().values()) {
            state.setCurrent(state.current() + state.gainOnCast());
        }
    }

    public static void tickResourcesAndStatuses(ServerPlayer player) {
        PlayerProfile profile = RpgProfileManager.get(player);
        profile.resources().values().forEach(res -> res.setCurrent(res.current() + res.regenPerSec()));
        applyStatusEffects(player, profile);
        cleanupStatuses(profile);
    }

    private static void cleanupStatuses(PlayerProfile profile) {
        long nowMs = getNowMs();
        profile.statuses().entrySet().removeIf(entry -> entry.getValue().endMs() <= nowMs);
    }

    private static void applyStatusEffects(ServerPlayer player, PlayerProfile profile) {
        long nowMs = getNowMs();
        for (ActiveStatus status : profile.statuses().values()) {
            if (status.endMs() <= nowMs) continue;
            if (Boolean.TRUE.equals(status.flags().get("disable_move"))) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 255, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 128, false, false));
            }
        }
    }

    private static void applyStatusToEntity(LivingEntity target, RpgStatusDef status, long durationMs) {
        if (target instanceof ServerPlayer player) {
            PlayerProfile profile = RpgProfileManager.get(player);
            long endMs = getNowMs() + durationMs;
            profile.statuses().put(status.id(), new ActiveStatus(status.id(), endMs, status.flags()));
            return;
        }
        if (Boolean.TRUE.equals(status.flags().get("disable_move"))) {
            int ticks = msToTicks(durationMs);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 255, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.JUMP, ticks, 128, false, false));
        }
    }

    private static long getNowMs() {
        return getNowTick() * 50L;
    }

    private static long getNowTick() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return 0;
        return server.getTickCount();
    }

    private static int msToTicks(long ms) {
        return (int) Math.max(1, Math.ceil(ms / 50.0));
    }

    private static final class CastPipeline {
        private final ServerPlayer caster;
        private final LivingEntity target;
        private final RpgSkillDef skill;
        private int index = 0;
        private long nextTick;

        private CastPipeline(ServerPlayer caster, LivingEntity target, RpgSkillDef skill, long startTick) {
            this.caster = caster;
            this.target = target;
            this.skill = skill;
            this.nextTick = startTick;
        }

        private boolean isExpired() {
            return caster.isRemoved() || caster.isDeadOrDying();
        }

        private boolean tick() {
            if (index >= skill.pipeline().size()) return false;
            RpgActionDef action = skill.pipeline().get(index);
            int delayTicks = executeAction(action);
            index++;
            nextTick = getNowTick() + Math.max(1, delayTicks);
            return index < skill.pipeline().size();
        }

        private int executeAction(RpgActionDef action) {
            if (action instanceof RpgActionDef.Wait wait) {
                return msToTicks(wait.ms());
            }
            if (action instanceof RpgActionDef.PlaySound playSound) {
                playSound(playSound);
                return 1;
            }
            if (action instanceof RpgActionDef.SpawnParticles spawn) {
                spawnParticles(spawn);
                return 1;
            }
            if (action instanceof RpgActionDef.ApplyForce force) {
                applyForce(force);
                return 1;
            }
            if (action instanceof RpgActionDef.Damage damage) {
                applyDamage(damage);
                return 1;
            }
            if (action instanceof RpgActionDef.ApplyStatus status) {
                applyStatus(status);
                return 1;
            }
            return 1;
        }

        private void playSound(RpgActionDef.PlaySound action) {
            ServerLevel level = caster.serverLevel();
            SoundEvent event = BuiltInRegistries.SOUND_EVENT.getOptional(new ResourceLocation(action.sound())).orElse(null);
            if (event == null) return;
            level.playSound(null, caster.blockPosition(), event, SoundSource.PLAYERS, action.volume(), action.pitch());
        }

        private void spawnParticles(RpgActionDef.SpawnParticles action) {
            ServerLevel level = caster.serverLevel();
            ParticleOptions particle = null;
            var type = BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(action.particle()));
            if (type instanceof SimpleParticleType simple) {
                particle = simple;
            }
            if (particle == null) return;
            Vec3 pos = caster.position().add(0, caster.getBbHeight() * 0.6, 0);
            level.sendParticles(particle, pos.x, pos.y, pos.z, action.count(), action.spread(), action.spread(), action.spread(), action.speed());
        }

        private void applyForce(RpgActionDef.ApplyForce action) {
            if (target == null) return;
            Vec3 dir;
            if ("pull_to_caster".equalsIgnoreCase(action.mode())) {
                dir = caster.position().subtract(target.position());
            } else {
                dir = target.position().subtract(caster.position());
            }
            if (dir.lengthSqr() == 0) return;
            Vec3 norm = dir.normalize().scale(action.strength());
            target.push(norm.x, norm.y * 0.1, norm.z);
            target.hurtMarked = true;
        }

        private void applyDamage(RpgActionDef.Damage action) {
            double amount = RpgFormula.evaluate(action.amount(), RpgProfileManager.get(caster));
            if (action.radius() > 0) {
                AABB box = new AABB(caster.blockPosition()).inflate(action.radius());
                List<LivingEntity> targets = caster.level().getEntitiesOfClass(LivingEntity.class, box,
                    entity -> entity != caster && entity.isAlive());
                for (LivingEntity entity : targets) {
                    entity.hurt(caster.damageSources().playerAttack(caster), (float) amount);
                }
            } else if (target != null) {
                target.hurt(caster.damageSources().playerAttack(caster), (float) amount);
            }
        }

        private void applyStatus(RpgActionDef.ApplyStatus action) {
            RpgStatusDef status = RpgDataManager.getStatuses().get(action.status());
            if (status == null || target == null) return;
            long duration = action.durationMs() > 0 ? action.durationMs() : status.durationMs();
            applyStatusToEntity(target, status, duration);
        }
    }
}
