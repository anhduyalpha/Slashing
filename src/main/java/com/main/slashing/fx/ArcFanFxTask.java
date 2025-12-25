package com.main.slashing.fx;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class ArcFanFxTask implements FxTask {
    private static final RandomSource RAND = RandomSource.create();

    private final ServerLevel level;
    private final Vec3 center;
    private final Vec3 forward;
    private final double[] radii;
    private final int totalTicks;
    private final double arcAngleRad;
    private final int steps;
    private int ticksLeft;

    private final boolean addSparks;
    private final boolean hot;

    public ArcFanFxTask(ServerLevel level, Vec3 center, Vec3 forward,
                        double[] radii, int totalTicks, double arcAngleRad, int steps,
                        boolean addSparks, boolean hot) {
        this.level = level;
        this.center = center;
        this.forward = forward;
        this.radii = radii;
        this.totalTicks = totalTicks;
        this.arcAngleRad = arcAngleRad;
        this.steps = steps;
        this.addSparks = addSparks;
        this.hot = hot;
        this.ticksLeft = totalTicks;
    }

    @Override
    public int estimatedParticlesThisTick() {
        int base = radii.length * (steps + 1);
        return addSparks ? (base + base / 3) : base;
    }

    @Override
    public boolean tick() {
        if (ticksLeft-- <= 0) return false;

        float prog = 1f - (ticksLeft / (float) totalTicks);
        double sweepShift = Mth.lerp(prog, -0.70, 0.70);

        FxMath.Basis b0 = FxMath.basisFromForward(forward);
        Vec3 right = b0.right();
        Vec3 up = b0.up();

        double start = -arcAngleRad * 0.5 + sweepShift;
        double end   =  arcAngleRad * 0.5 + sweepShift;

        for (double radius : radii) {
            for (int i = 0; i <= steps; i++) {
                double tt = (double) i / (double) steps;
                double ang = start + (end - start) * tt;

                Vec3 p = center
                        .add(right.scale(radius * Math.cos(ang)))
                        .add(up.scale(radius * Math.sin(ang)));

                // slash dust
                FxParticles.one(level, hot ? FxParticles.sunHot() : FxParticles.sunSlash(), p.x, p.y, p.z);

                if (addSparks && ((i & 3) == 0)) {
                    level.sendParticles(FxParticles.ember(), p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
                }

                if ((i & 7) == 0) {
                    level.sendParticles(FxParticles.glow(), p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
                }
            }
        }
        return true;
    }
}
