package com.main.slashing.fx;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class RadialBurstFxTask implements FxTask {
    private final ServerLevel level;
    private final Vec3 center;
    private final int totalTicks;
    private int ticksLeft;

    public RadialBurstFxTask(ServerLevel level, Vec3 center, int totalTicks) {
        this.level = level;
        this.center = center;
        this.totalTicks = totalTicks;
        this.ticksLeft = totalTicks;
    }

    @Override
    public int estimatedParticlesThisTick() { return 260; }

    @Override
    public boolean tick() {
        if (ticksLeft-- <= 0) return false;

        float prog = 1f - (ticksLeft / (float) totalTicks);
        double radius = 0.6 + prog * 3.8;

        int spokes = 20;
        int steps = 7;

        for (int s = 0; s < spokes; s++) {
            double ang = (Math.PI * 2.0) * (s / (double)spokes);
            Vec3 dir = new Vec3(Math.cos(ang), 0.0, Math.sin(ang));

            for (int i = 0; i <= steps; i++) {
                double rr = radius * (i / (double)steps);
                Vec3 p = center.add(dir.scale(rr)).add(0, 0.25, 0);

                FxParticles.one(level, FxParticles.sunHot(), p.x, p.y, p.z);
                if ((i & 1) == 0) level.sendParticles(FxParticles.ember(), p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.0);
                if ((i & 3) == 0) level.sendParticles(FxParticles.glow(), p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.0);
            }
        }
        return true;
    }
}
