package com.main.slashing.mmo.fx;

import com.main.slashing.fx.FxTask;
import com.main.slashing.mmo.DamageSpec;
import com.main.slashing.mmo.HitFxSpec;
import com.main.slashing.mmo.MmoHitFx;
import com.main.slashing.mmo.SkillElement;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Projectile bay cong nhẹ (arcHeight) - chạy server-side, không tạo entity.
 * Tối ưu:
 * - mỗi tick chỉ spawn trailParticlesPerTick hạt
 * - kiểm tra entity trong AABB đoạn di chuyển
 */
public final class CurveProjectileFxTask implements FxTask {
    private final ServerPlayer caster;
    private final ServerLevel level;
    private final double speed;
    private final double maxRange;
    private final double arcHeight;
    private final double hitRadius;
    private final int pierce;
    private final boolean stopOnBlock;
    private final int trailParticlesPerTick;
    private final DamageSpec damage;
    private final HitFxSpec hitFx;
    private final SkillElement element;

    private final Vec3 start;
    private final Vec3 dir;

    private double traveled = 0.0;
    private int hitsLeft;
    private final Set<Integer> hitEntityIds = new HashSet<>();

    public CurveProjectileFxTask(
            ServerPlayer caster,
            double speed,
            double maxRange,
            double arcHeight,
            double hitRadius,
            int pierce,
            boolean stopOnBlock,
            int trailParticlesPerTick,
            DamageSpec damage,
            HitFxSpec hitFx,
            SkillElement element
    ) {
        this.caster = caster;
        this.level = caster.serverLevel();
        this.speed = speed;
        this.maxRange = maxRange;
        this.arcHeight = arcHeight;
        this.hitRadius = hitRadius;
        this.pierce = pierce;
        this.stopOnBlock = stopOnBlock;
        this.trailParticlesPerTick = trailParticlesPerTick;
        this.damage = damage;
        this.hitFx = hitFx;
        this.element = element;

        this.start = caster.getEyePosition().add(caster.getLookAngle().normalize().scale(0.6));
        this.dir = caster.getLookAngle().normalize();
        this.hitsLeft = pierce <= 0 ? 1 : (pierce + 1);
    }

    @Override
    public int estimatedParticlesThisTick() {
        return Math.max(1, trailParticlesPerTick);
    }

    @Override
    public boolean tick() {
        if (caster == null || caster.isRemoved()) return false;
        if (traveled >= maxRange) return false;

        Vec3 p0 = positionAt(traveled);
        double next = Math.min(maxRange, traveled + speed);
        Vec3 p1 = positionAt(next);

        // Block clip (nếu stopOnBlock)
        if (stopOnBlock) {
            BlockHitResult bhr = level.clip(new ClipContext(p0, p1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
            if (bhr.getType() == HitResult.Type.BLOCK) {
                Vec3 hit = bhr.getLocation();
                spawnTrail(p0, hit);
                level.sendParticles(element.accentParticle(), hit.x, hit.y, hit.z, 8, 0.20, 0.20, 0.20, 0.02);
                return false;
            }
        }

        // Entity hit check
        AABB box = new AABB(p0, p1).inflate(hitRadius);
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, box, e ->
                e != caster && e.isAlive() && !e.isSpectator()
        );

        LivingEntity hitEntity = null;
        double best = Double.MAX_VALUE;

        for (LivingEntity e : list) {
            if (hitEntityIds.contains(e.getId())) continue;
            Vec3 c = e.getBoundingBox().getCenter();
            double d2 = distanceToSegmentSqr(c, p0, p1);
            if (d2 <= hitRadius * hitRadius && d2 < best) {
                best = d2;
                hitEntity = e;
            }
        }

        if (hitEntity != null) {
            hitEntityIds.add(hitEntity.getId());
            damage.apply(caster, hitEntity);

            MmoHitFx.onHit(caster, hitEntity, element, hitFx);

            // impact
            Vec3 impact = hitEntity.getBoundingBox().getCenter();
            level.sendParticles(element.accentParticle(), impact.x, impact.y, impact.z, 12, 0.25, 0.20, 0.25, 0.03);

            hitsLeft--;
            if (hitsLeft <= 0) return false;
        }

        // Trail
        spawnTrail(p0, p1);

        traveled = next;
        return true;
    }

    private Vec3 positionAt(double dist) {
        double t = dist / maxRange;
        double yOff = Math.sin(t * Math.PI) * arcHeight;
        return start.add(dir.scale(dist)).add(0, yOff, 0);
    }

    private void spawnTrail(Vec3 a, Vec3 b) {
        int n = Math.max(1, trailParticlesPerTick);
        for (int i = 0; i < n; i++) {
            double t = (i + 0.5) / n;
            Vec3 p = a.lerp(b, t);
            level.sendParticles(element.dust(), p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    private static double distanceToSegmentSqr(Vec3 p, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double abLen2 = ab.lengthSqr();
        if (abLen2 < 1e-8) return p.distanceToSqr(a);
        double t = (p.subtract(a)).dot(ab) / abLen2;
        t = Mth.clamp(t, 0.0, 1.0);
        Vec3 proj = a.add(ab.scale(t));
        return proj.distanceToSqr(p);
    }
}
