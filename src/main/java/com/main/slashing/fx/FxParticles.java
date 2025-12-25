package com.main.slashing.fx;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3f;

public final class FxParticles {
    private FxParticles() {}

    public static ParticleOptions sunSlash() {
        // warm orange dust
        return new DustParticleOptions(new Vector3f(1.0f, 0.55f, 0.15f), 1.2f);
    }

    public static ParticleOptions sunHot() {
        return new DustParticleOptions(new Vector3f(1.0f, 0.75f, 0.25f), 1.5f);
    }

    public static ParticleOptions ember() {
        return ParticleTypes.FLAME;
    }

    public static ParticleOptions glow() {
        return ParticleTypes.GLOW;
    }

    public static void one(ServerLevel level, ParticleOptions p, double x, double y, double z) {
        level.sendParticles(p, x, y, z, 1, 0, 0, 0, 0);
    }

    public static void burst(ServerLevel level, ParticleOptions p, double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
        level.sendParticles(p, x, y, z, count, dx, dy, dz, speed);
    }
}
