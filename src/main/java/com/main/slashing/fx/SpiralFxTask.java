package com.main.slashing.fx;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class SpiralFxTask implements FxTask {
    private static final RandomSource RAND = RandomSource.create();

    private final ServerLevel level;
    private final Vec3 origin;
    private final Vec3 forward;
    private final int totalTicks;
    private final int pointsPerTick;
    private int ticksLeft;

    public SpiralFxTask(ServerLevel level, Vec3 origin, Vec3 forward, int totalTicks, int pointsPerTick) {
        this.level = level;
        this.origin = origin;
        this.forward = forward;
        this.totalTicks = totalTicks;
        this.pointsPerTick = pointsPerTick;
        this.ticksLeft = totalTicks;
    }

    @Override
    public int estimatedParticlesThisTick() { return pointsPerTick + pointsPerTick / 2; }

    @Override
    public boolean tick() {
        if (ticksLeft-- <= 0) return false;

        float prog = 1f - (ticksLeft / (float) totalTicks);
        FxMath.Basis b0 = FxMath.basisFromForward(forward);
        Vec3 right = b0.right();
        Vec3 up = b0.up();
        Vec3 f = b0.forward();

        double len = 2.2 + prog * 2.1;
        double radius = 0.25 + prog * 0.75;
        double turns = 3.2;

        for (int i = 0; i < pointsPerTick; i++) {
            double t = (prog + i / (double)pointsPerTick) / 1.15;
            double ang = t * turns * Math.PI * 2.0;
            double along = t * len;

            Vec3 p = origin.add(f.scale(along))
                    .add(right.scale(Math.cos(ang) * radius))
                    .add(up.scale(Math.sin(ang) * radius));

            FxParticles.one(level, FxParticles.sunHot(), p.x, p.y, p.z);
            if ((i & 1) == 0) level.sendParticles(FxParticles.ember(), p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
        return true;
    }
}
