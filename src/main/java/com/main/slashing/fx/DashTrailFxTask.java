package com.main.slashing.fx;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class DashTrailFxTask implements FxTask {
    private static final RandomSource RAND = RandomSource.create();

    private final ServerLevel level;
    private final Vec3 start;
    private final Vec3 dir;
    private final int totalTicks;
    private int ticksLeft;

    public DashTrailFxTask(ServerLevel level, Vec3 start, Vec3 dir, int totalTicks) {
        this.level = level;
        this.start = start;
        this.dir = dir.normalize();
        this.totalTicks = totalTicks;
        this.ticksLeft = totalTicks;
    }

    @Override
    public int estimatedParticlesThisTick() { return 90; }

    @Override
    public boolean tick() {
        if (ticksLeft-- <= 0) return false;

        float prog = 1f - (ticksLeft / (float) totalTicks);
        double dist = prog * 5.2;
        Vec3 head = start.add(dir.scale(dist));

        for (int i = 0; i < 28; i++) {
            Vec3 p = head.add(dir.scale(-i * 0.10));

            FxParticles.one(level, FxParticles.sunSlash(), p.x, p.y, p.z);
            if ((i & 2) == 0) level.sendParticles(FxParticles.ember(), p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
        return true;
    }
}
