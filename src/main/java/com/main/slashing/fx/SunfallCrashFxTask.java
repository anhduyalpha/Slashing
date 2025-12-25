package com.main.slashing.fx;

import com.main.slashing.particle.SlashParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;

public final class SunfallCrashFxTask implements FxTask {

    private final ServerPlayer caster;
    private final ServerLevel level;

    private final int totalTicks;
    private int ticksLeft;

    private final float damage;
    private final float knockback;
    private final HashSet<Integer> hit = new HashSet<>();

    private boolean slammed = false;
    private Vec3 slamPos = null;

    public SunfallCrashFxTask(ServerPlayer caster, int durationTicks,
                              float damage, float knockback) {
        this.caster = caster;
        this.level = caster.serverLevel();
        this.totalTicks = Math.max(1, durationTicks);
        this.ticksLeft = this.totalTicks;

        this.damage = damage;
        this.knockback = knockback;
    }

    @Override
    public int estimatedParticlesThisTick() { return 420; }

    @Override
    public boolean tick() {
        int t = totalTicks - ticksLeft;
        if (ticksLeft-- <= 0) return false;

        Vec3 pos = caster.position();

        // hop cao
        if (t == 0) {
            caster.setDeltaMovement(caster.getDeltaMovement().add(0, 0.95, 0));
            caster.resetFallDistance();
        }

        // aura trước khi slam
        if (!slammed && t <= 6) {
            level.sendParticles(ParticleTypes.GLOW, pos.x, pos.y + 1.0, pos.z, 4, 0.25, 0.18, 0.25, 0);
            level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 1.0, pos.z, 2, 0.18, 0.12, 0.18, 0);
            return true;
        }

        // slam: teleport xuống mặt đất phía dưới
        if (!slammed) {
            slammed = true;
            slamPos = findGround(pos.add(0, 1.0, 0), 14.0);
            if (slamPos != null) {
                caster.teleportTo(slamPos.x, slamPos.y + 0.05, slamPos.z);
                caster.resetFallDistance();
            } else {
                slamPos = caster.position();
            }
        }

        // shockwave ring nở ra theo thời gian
        int ringT = t - 7; // 0..?
        if (ringT < 0) ringT = 0;

        double radius = 1.0 + ringT * 0.85;
        if (radius > 8.5) return false;

        SlashParticleOptions ring = new SlashParticleOptions(
                1.00f, 0.70f, 0.16f,
                0.92f,
                0.26f,
                12
        );

        int pts = 40;
        for (int i = 0; i < pts; i++) {
            double ang = (Math.PI * 2.0) * (i / (double)pts);
            double x = slamPos.x + Math.cos(ang) * radius;
            double z = slamPos.z + Math.sin(ang) * radius;
            double y = slamPos.y + 0.15;

            level.sendParticles(ring, x, y, z, 1, 0, 0, 0, 0);
            if ((i & 3) == 0) level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0.02, 0.02, 0.02, 0);
        }

        // damage theo vành: chỉ hit 1 lần/target trong cả skill
        damageRing(radius, 0.9);

        return true;
    }

    private void damageRing(double radius, double band) {
        AABB box = new AABB(slamPos, slamPos).inflate(radius + band, 2.0, radius + band);

        List<net.minecraft.world.entity.LivingEntity> list = level.getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class,
                box,
                e -> e.isAlive() && e != caster && !e.isInvulnerable() && !caster.isAlliedTo(e)
        );

        if (list.isEmpty()) return;

        double rMin = Math.max(0.0, radius - band);
        double rMax = radius + band;
        double rMin2 = rMin * rMin;
        double rMax2 = rMax * rMax;

        var src = level.damageSources().playerAttack(caster);

        for (var e : list) {
            int id = e.getId();
            if (hit.contains(id)) continue;

            double dx = e.getX() - slamPos.x;
            double dz = e.getZ() - slamPos.z;
            double d2 = dx * dx + dz * dz;

            if (d2 >= rMin2 && d2 <= rMax2) {
                hit.add(id);
                e.hurt(src, damage);
                if (knockback > 0f) e.knockback(knockback, -dx, -dz);
                e.setSecondsOnFire(3);
            }
        }
    }

    private Vec3 findGround(Vec3 start, double down) {
        Vec3 end = start.add(0, -down, 0);
        HitResult hr = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        if (hr.getType() == HitResult.Type.BLOCK) return hr.getLocation();
        return null;
    }
}
