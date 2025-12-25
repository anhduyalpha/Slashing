package com.main.slashing.fx;

import com.main.slashing.particle.SlashParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class SlashSweepFxTask implements FxTask {

    private final ServerPlayer caster;
    private final ServerLevel level;

    private final Vec3 center;
    private final Vec3 forward;

    private final int totalTicks;
    private int ticksLeft;

    private final float damage;
    private final float knockback;
    private final double hitRadius;
    private final boolean setOnFireSeconds2;

    // Per-cast hit prevent (mỗi entity chỉ trúng 1 lần trong cả skill)
    private final HashSet<Integer> hit = new HashSet<>();

    public SlashSweepFxTask(
            ServerPlayer caster,
            Vec3 center,
            Vec3 forward,
            int durationTicks,
            float damage,
            float knockback,
            double hitRadius,
            boolean setOnFireSeconds2
    ) {
        this.caster = caster;
        this.level = caster.serverLevel();
        this.center = center;
        this.forward = forward.normalize();

        this.totalTicks = Math.max(1, durationTicks);
        this.ticksLeft = this.totalTicks;

        this.damage = damage;
        this.knockback = knockback;
        this.hitRadius = Math.max(0.1, hitRadius);
        this.setOnFireSeconds2 = setOnFireSeconds2;
    }

    @Override
    public int estimatedParticlesThisTick() {
        // 2-3 vòng arc * số điểm mỗi tick (ước lượng)
        return 3 * 28;
    }

    @Override
    public boolean tick() {
        if (ticksLeft-- <= 0) return false;

        float prog = 1f - (ticksLeft / (float) totalTicks);

        // Basis theo hướng nhìn
        FxMath.Basis b = FxMath.basisFromForward(forward);
        Vec3 right = b.right();
        Vec3 up = b.up();

        // Arc config
        double arc = Math.toRadians(155);
        double sweep = (prog - 0.5) * Math.toRadians(42);
        double start = -arc * 0.5 + sweep;
        double end   =  arc * 0.5 + sweep;

        double[] radii = {1.05, 1.28, 1.52};
        int steps = 26;

        // ==== 1) Build sample points (để hit theo vệt) ====
        ArrayList<Vec3> points = new ArrayList<>(64);

        for (double r : radii) {
            for (int i = 0; i <= steps; i++) {
                // giảm tải: chỉ lấy mỗi 2 điểm
                if ((i & 1) == 1) continue;

                double t = i / (double) steps;
                double ang = start + (end - start) * t;

                Vec3 p = center
                        .add(right.scale(Math.cos(ang) * r))
                        .add(up.scale(Math.sin(ang) * r));

                points.add(p);

                // ==== 2) FX (giữ style slash của bạn) ====
                SlashParticleOptions opt = new SlashParticleOptions(
                        0.55f, 0.95f, 1.00f,  // RGB
                        0.85f,                // alpha
                        0.22f,                // scale
                        12                    // lifetime
                );
                level.sendParticles(opt, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }
        }

        // ==== 3) DAMAGE theo sample points (CÁCH B) ====
        if (!points.isEmpty()) {
            double maxR = radii[radii.length - 1] + hitRadius + 0.75;
            AABB queryBox = new AABB(center, center).inflate(maxR, maxR, maxR);

            List<LivingEntity> candidates = level.getEntitiesOfClass(
                    LivingEntity.class,
                    queryBox,
                    e -> e.isAlive()
                            && e != caster
                            && !e.isInvulnerable()
                            && !caster.isAlliedTo(e)    // tránh đánh đồng minh/team
            );

            if (!candidates.isEmpty()) {
                DamageSource src = level.damageSources().playerAttack(caster);
                double r2 = hitRadius * hitRadius;

                // check từng candidate có “chạm” vào bất kì sample point nào không
                for (LivingEntity e : candidates) {
                    int id = e.getId();
                    if (hit.contains(id)) continue;

                    Vec3 ep = e.position().add(0, e.getBbHeight() * 0.55, 0);

                    if (nearAnyPoint(ep, points, r2)) {
                        hit.add(id);

                        e.hurt(src, damage);

                        if (knockback > 0f) {
                            e.knockback(knockback, -forward.x, -forward.z);
                        }

                        if (setOnFireSeconds2) {
                            e.setSecondsOnFire(2);
                        }
                    }
                }
            }
        }

        return true;
    }

    private static boolean nearAnyPoint(Vec3 ep, ArrayList<Vec3> points, double r2) {
        for (int i = 0; i < points.size(); i++) {
            Vec3 p = points.get(i);
            if (ep.distanceToSqr(p) <= r2) return true;
        }
        return false;
    }
}
