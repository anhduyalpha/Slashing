package com.main.slashing.fx;

import com.main.slashing.particle.SlashParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;

public final class SkyCrossFxTask implements FxTask {

    private final ServerPlayer caster;
    private final ServerLevel level;

    private final int totalTicks;
    private int ticksLeft;

    private final float damage;
    private final float knockback;
    private final double hitRadius;
    private final HashSet<Integer> hit = new HashSet<>();

    public SkyCrossFxTask(ServerPlayer caster, int durationTicks,
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
    public int estimatedParticlesThisTick() { return 220; }

    @Override
    public boolean tick() {
        int t = totalTicks - ticksLeft;
        if (ticksLeft-- <= 0) return false;

        Vec3 fwd = caster.getLookAngle().normalize();
        FxMath.Basis b = FxMath.basisFromForward(fwd);
        Vec3 right = b.right();
        Vec3 up = b.up();

        if (t == 0) {
            caster.setDeltaMovement(caster.getDeltaMovement().add(0, 0.62, 0));
            caster.resetFallDistance();
        }

        // Center trước mặt + hơi cao
        Vec3 center = caster.getEyePosition().add(fwd.scale(1.75)).add(0, 0.25, 0);

        // 2 đường chéo tạo chữ X
        Vec3 d1 = right.add(up).normalize();   // /
        Vec3 d2 = up.subtract(right).normalize(); // \

        boolean first = (t >= 3 && t <= 6);
        boolean second = (t >= 7 && t <= 10);

        if (!first && !second) {
            // aura nhẹ
            level.sendParticles(ParticleTypes.GLOW, caster.getX(), caster.getY() + 1.0, caster.getZ(), 2, 0.15, 0.12, 0.15, 0);
            return true;
        }

        Vec3 axis = first ? d1 : d2;

        SlashParticleOptions slash = new SlashParticleOptions(
                1.00f, 0.78f, 0.22f,
                0.92f,
                0.28f,
                12
        );

        ArrayList<Vec3> points = new ArrayList<>(64);

        int steps = 28;
        double len = 2.35;

        for (int i = 0; i <= steps; i++) {
            if ((i & 1) == 1) continue;
            double k = (i / (double)steps) * 2.0 - 1.0; // -1..1
            Vec3 p = center.add(axis.scale(k * len)).add(fwd.scale(0.15 * Math.abs(k)));

            points.add(p);

            level.sendParticles(slash, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            if ((i & 3) == 0) level.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0);
        }

        FxHit.hitByPoints(caster, points, hitRadius, damage, knockback, true, hit);
        return true;
    }
}
