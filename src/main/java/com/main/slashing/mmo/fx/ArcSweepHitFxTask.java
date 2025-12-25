package com.main.slashing.mmo.fx;

import com.main.slashing.fx.FxMath;
import com.main.slashing.fx.FxTask;
import com.main.slashing.particle.SlashParticleOptions;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 1 template "đòn chém MMO" dạng arc sweep:
 * - spawn slash particles theo path
 * - hit detection theo sample points
 * - on-hit: damage + extra particles + hit confirm (shake/flash)
 */
public final class ArcSweepHitFxTask implements FxTask {

    private final ServerPlayer caster;
    private final ServerLevel level;

    private final int totalTicks;
    private int ticksLeft;

    private final double centerForwardOffset;
    private final double centerYOff;

    private final double arcAngleRad;
    private final double sweepAngleRad;
    private final double[] radii;
    private final int steps;
    private final int sampleStride;

    private final double hitRadius;
    private final DamageSpec damage;
    private final HitFxSpec hitFx;
    private final SkillElement element;

    private final boolean followCaster;
    private final boolean addSparks;

    private final Vec3 lockedForward;
    private final Vec3 lockedCenter;

    private final HashSet<Integer> alreadyHit = new HashSet<>();

    public ArcSweepHitFxTask(
            ServerPlayer caster,
            int durationTicks,
            double centerForwardOffset,
            double centerYOff,
            double arcAngleRad,
            double sweepAngleRad,
            double[] radii,
            int steps,
            int sampleStride,
            double hitRadius,
            DamageSpec damage,
            HitFxSpec hitFx,
            SkillElement element,
            boolean followCaster,
            boolean addSparks
    ) {
        this.caster = caster;
        this.level = caster.serverLevel();
        this.totalTicks = Math.max(1, durationTicks);
        this.ticksLeft = this.totalTicks;
        this.centerForwardOffset = centerForwardOffset;
        this.centerYOff = centerYOff;
        this.arcAngleRad = arcAngleRad;
        this.sweepAngleRad = sweepAngleRad;
        this.radii = (radii == null || radii.length == 0) ? new double[]{1.05, 1.28, 1.52} : radii;
        this.steps = Math.max(6, steps);
        this.sampleStride = Math.max(1, sampleStride);
        this.hitRadius = Math.max(0.05, hitRadius);
        this.damage = (damage == null) ? DamageSpec.none() : damage;
        this.hitFx = (hitFx == null) ? HitFxSpec.none() : hitFx;
        this.element = (element == null) ? SkillElement.PHYSICAL : element;
        this.followCaster = followCaster;
        this.addSparks = addSparks;

        // lock ngay khi cast để dùng cho chế độ không-follow (tối ưu, ít jitter)
        Vec3 fwd0 = caster.getLookAngle().normalize();
        Vec3 org0 = caster.getEyePosition();
        this.lockedForward = fwd0;
        this.lockedCenter = org0.add(fwd0.scale(centerForwardOffset)).add(0, centerYOff, 0);
    }

    @Override
    public int estimatedParticlesThisTick() {
        // radii * (steps/stride) (ước lượng)
        int pts = (steps / sampleStride) + 2;
        int base = radii.length * pts;
        return addSparks ? (base + base / 3) : base;
    }

    @Override
    public boolean tick() {
        if (caster == null || !caster.isAlive()) return false;
        if (ticksLeft-- <= 0) return false;

        Vec3 forward;
        Vec3 center;
        if (followCaster) {
            Vec3 origin = caster.getEyePosition();
            forward = caster.getLookAngle().normalize();
            center = origin.add(forward.scale(centerForwardOffset)).add(0, centerYOff, 0);
        } else {
            forward = lockedForward;
            center = lockedCenter;
        }

        float prog = 1f - (ticksLeft / (float) totalTicks);
        double sweep = (prog - 0.5) * sweepAngleRad;
        double start = -arcAngleRad * 0.5 + sweep;
        double end = arcAngleRad * 0.5 + sweep;

        FxMath.Basis basis = FxMath.basisFromForward(forward);
        Vec3 right = basis.right();
        Vec3 up = basis.up();

        // ===== 1) Build sample points + particles =====
        ArrayList<Vec3> points = new ArrayList<>(64);

        int rgb = element.rgb();
        float pr = ((rgb >> 16) & 0xFF) / 255f;
        float pg = ((rgb >> 8) & 0xFF) / 255f;
        float pb = (rgb & 0xFF) / 255f;

        for (double r : radii) {
            for (int i = 0; i <= steps; i += sampleStride) {
                double t = (double) i / (double) steps;
                double ang = Mth.lerp(t, start, end);

                Vec3 p = center
                        .add(right.scale(Math.cos(ang) * r))
                        .add(up.scale(Math.sin(ang) * r));

                points.add(p);

                // slash particle chính
                SlashParticleOptions slash = new SlashParticleOptions(
                        pr, pg, pb,
                        0.85f,
                        0.22f,
                        11
                );
                level.sendParticles(slash, p.x, p.y, p.z, 1, 0, 0, 0, 0);

                // accent sparks (đẹp với shader)
                if (addSparks && ((i / sampleStride) & 2) == 0) {
                    level.sendParticles(element.accentParticle(), p.x, p.y, p.z, 1,
                            0.02, 0.02, 0.02, 0.01);
                }
            }
        }

        // ===== 2) Damage + onHitFX =====
        if (!points.isEmpty() && damage.amount() > 0f) {
            double maxR = radii[radii.length - 1] + hitRadius + 0.75;
            AABB box = new AABB(center, center).inflate(maxR, maxR, maxR);

            List<LivingEntity> candidates = level.getEntitiesOfClass(
                    LivingEntity.class,
                    box,
                    e -> e.isAlive()
                            && e != caster
                            && !e.isInvulnerable()
                            && !caster.isAlliedTo(e)
            );

            if (!candidates.isEmpty()) {
                double r2 = hitRadius * hitRadius;
                for (LivingEntity e : candidates) {
                    int id = e.getId();
                    if (alreadyHit.contains(id)) continue;

                    Vec3 ep = e.position().add(0, e.getBbHeight() * 0.55, 0);
                    if (nearAnyPoint(ep, points, r2)) {
                        alreadyHit.add(id);
                        damage.apply(caster, e);
                        MmoHitFx.onHit(caster, e, element, hitFx);
                    }
                }
            }
        }

        return true;
    }

    private static boolean nearAnyPoint(Vec3 ep, ArrayList<Vec3> points, double r2) {
        for (int i = 0; i < points.size(); i++) {
            if (ep.distanceToSqr(points.get(i)) <= r2) return true;
        }
        return false;
    }
}
