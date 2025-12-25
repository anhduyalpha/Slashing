package com.main.slashing.fx;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class DragonHelixFxTask implements FxTask {
    private static final RandomSource RAND = RandomSource.create();

    private final ServerLevel level;
    private final Vec3 origin;
    private final Vec3 forward;
    private final int totalTicks;
    private int ticksLeft;

    public DragonHelixFxTask(ServerLevel level, Vec3 origin, Vec3 forward, int totalTicks) {
        this.level = level;
        this.origin = origin;
        this.forward = forward.normalize();
        this.totalTicks = totalTicks;
        this.ticksLeft = totalTicks;
    }

    @Override
    public int estimatedParticlesThisTick() { return 210; }

    @Override
    public boolean tick() {
        if (ticksLeft-- <= 0) return false;

        float prog = 1f - (ticksLeft / (float) totalTicks);

        FxMath.Basis b0 = FxMath.basisFromForward(forward);
        Vec3 right = b0.right();
        Vec3 up = b0.up();
        Vec3 f = b0.forward();

        double len = 6.7;
        double along = prog * len;

        int pts = 72;
        for (int i = 0; i < pts; i++) {
            double t = (along + i * 0.10) / len;
            double ang = t * Math.PI * 2.0 * 3.1;
            double rad = 0.55 + 0.28 * Math.sin(t * Math.PI);

            Vec3 p = origin.add(f.scale(along + i * 0.10))
                    .add(right.scale(Math.cos(ang) * rad))
                    .add(up.scale(Math.sin(ang) * rad));

            FxParticles.one(level, FxParticles.sunHot(), p.x, p.y, p.z);
            if ((i & 2) == 0) level.sendParticles(FxParticles.ember(), p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.0);
            if ((i & 5) == 0) level.sendParticles(FxParticles.glow(), p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.0);
        }

        return true;
    }
}
