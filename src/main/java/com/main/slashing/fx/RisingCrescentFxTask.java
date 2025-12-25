package com.main.slashing.fx;

import com.main.slashing.particle.SlashParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;

public final class RisingCrescentFxTask implements FxTask {

    private final ServerPlayer caster;
    private final ServerLevel level;

    private final int totalTicks;
    private int ticksLeft;

    private final float damage;
    private final float knockback;
    private final double hitRadius;
    private final HashSet<Integer> hit = new HashSet<>();

    public RisingCrescentFxTask(ServerPlayer caster, int durationTicks,
                                float damage, float knockback, double hitRadius) {
        this.caster = caster;
        this.level = caster.serverLevel();
        this.totalTicks = Math.max(1, durationTicks);
        this.ticksLeft = this.totalTicks;

        this.damage = damage;
        this.knockback = knockback;
        this.hitRadius = Math.max(0.2, hitRadius);
    }

    @Override
    public int estimatedParticlesThisTick() { return 160; }

    @Override
    public boolean tick() {
        int t = totalTicks - ticksLeft; // 0..total-1
        if (ticksLeft-- <= 0) return false;

        Vec3 fwd = caster.getLookAngle().normalize();

        // tick 0: hop nhẹ + lao tới 1 chút
        if (t == 0) {
            caster.setDeltaMovement(caster.getDeltaMovement().add(fwd.scale(0.25)).add(0, 0.72, 0));
            caster.resetFallDistance();
        }

        float prog = t / (float)(totalTicks - 1 <= 0 ? 1 : (totalTicks - 1));
        FxMath.Basis b = FxMath.basisFromForward(fwd);
        Vec3 up = b.up();

        // Center nâng dần theo prog (cảm giác “chém đi lên”)
        Vec3 center = caster.getEyePosition()
                .add(fwd.scale(1.15))
                .add(0, 0.15 + prog * 0.85, 0);

        // Arc trong mặt phẳng (forward + up)
        double arc = Math.toRadians(165);
        double start = -arc * 0.5;
        double end   =  arc * 0.5;

        double[] radii = {1.00, 1.28, 1.55};
        int steps = 26;

        SlashParticleOptions slash = new SlashParticleOptions(
                1.00f, 0.72f, 0.18f,  // vàng cam
                0.92f,
                0.26f,
                12
        );

        ArrayList<Vec3> points = new ArrayList<>(64);

        for (double r : radii) {
            for (int i = 0; i <= steps; i++) {
                if ((i & 1) == 1) continue; // giảm tải
                double k = i / (double) steps;
                double ang = start + (end - start) * k;

                Vec3 p = center
                        .add(fwd.scale(Math.cos(ang) * r))
                        .add(up.scale(Math.sin(ang) * r));

                points.add(p);

                level.sendParticles(slash, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                if ((i & 3) == 0) level.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0);
            }
        }

        FxHit.hitByPoints(caster, points, hitRadius, damage, knockback, true, hit);
        return true;
    }
}
