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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;

import java.util.List;

/**
 * Raycast strike: server tính hit/damage, client chỉ render particle từ server.
 * - Tick 1: raycast + damage + hit FX
 * - Tick 2: particle "afterglow" nhẹ để cảm giác chém có đường.
 */
public final class RaycastStrikeFxTask implements FxTask {
    private final ServerPlayer caster;
    private final ServerLevel level;
    private final double range;
    private final double hitRadius;
    private final int steps;
    private final boolean addSparks;
    private final DamageSpec damage;
    private final HitFxSpec hitFx;
    private final SkillElement element;

    private int ticks = 2;
    private Vec3 start;
    private Vec3 end;

    public RaycastStrikeFxTask(
            ServerPlayer caster,
            double range,
            double hitRadius,
            int steps,
            boolean addSparks,
            DamageSpec damage,
            HitFxSpec hitFx,
            SkillElement element
    ) {
        this.caster = caster;
        this.level = caster.serverLevel();
        this.range = range;
        this.hitRadius = hitRadius;
        this.steps = steps;
        this.addSparks = addSparks;
        this.damage = damage;
        this.hitFx = hitFx;
        this.element = element;
    }

    @Override
    public int estimatedParticlesThisTick() {
        return steps + (addSparks ? 6 : 0);
    }

    @Override
    public boolean tick() {
        if (ticks-- <= 0 || caster == null || caster.isRemoved()) return false;

        Vec3 eye = caster.getEyePosition();
        Vec3 dir = caster.getLookAngle().normalize();
        Vec3 desiredEnd = eye.add(dir.scale(range));

        // block clip trước (để đường chém không xuyên tường)
        BlockHitResult bhr = level.clip(new ClipContext(eye, desiredEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        Vec3 clippedEnd = bhr.getType() == HitResult.Type.BLOCK ? bhr.getLocation() : desiredEnd;

        this.start = eye;
        this.end = clippedEnd;

        if (ticks == 1) {
            // Tick đầu: tìm entity gần nhất trên đoạn
            LivingEntity target = pickClosestLiving(level, caster, eye, clippedEnd, hitRadius);
            if (target != null) {
                damage.apply(caster, target);

                // hit FX chỉ gửi cho caster (shake/flash)
                MmoHitFx.onHit(caster, target, element, hitFx);
            }
        }

        // Particle line (rất nhẹ)
        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            Vec3 p = start.lerp(end, t);
            level.sendParticles(element.dust(), p.x, p.y, p.z, 1, 0, 0, 0, 0.0);
        }
        if (addSparks) {
            Vec3 mid = start.lerp(end, 0.6);
            level.sendParticles(element.accentParticle(), mid.x, mid.y, mid.z, 6, 0.15, 0.10, 0.15, 0.02);
        }

        return ticks > 0;
    }

    private static LivingEntity pickClosestLiving(ServerLevel level, ServerPlayer caster, Vec3 a, Vec3 b, double radius) {
        AABB box = new AABB(a, b).inflate(radius);
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, box, e ->
                e != caster && e.isAlive() && !e.isSpectator()
        );
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;

        Vec3 ab = b.subtract(a);
        double abLen2 = ab.lengthSqr();
        if (abLen2 < 1e-6) return null;

        for (LivingEntity e : list) {
            Vec3 p = e.getBoundingBox().getCenter();
            double t = (p.subtract(a)).dot(ab) / abLen2;
            t = Mth.clamp(t, 0.0, 1.0);
            Vec3 proj = a.add(ab.scale(t));
            double d2 = proj.distanceToSqr(p);
            if (d2 <= radius * radius && d2 < bestDist) {
                bestDist = d2;
                best = e;
            }
        }
        return best;
    }
}
