package com.main.slashing.fx;

import com.main.slashing.particle.SlashParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;

public final class FlashDashFxTask implements FxTask {

    private final ServerPlayer caster;
    private final ServerLevel level;

    private final int totalTicks;
    private int ticksLeft;

    private final float damage;
    private final float knockback;
    private final double hitRadius;
    private final HashSet<Integer> hit = new HashSet<>();

    public FlashDashFxTask(ServerPlayer caster, int durationTicks,
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
    public int estimatedParticlesThisTick() { return 260; }

    @Override
    public boolean tick() {
        int t = totalTicks - ticksLeft;
        if (ticksLeft-- <= 0) return false;

        Vec3 baseFwd = caster.getLookAngle().normalize();

        // 3 nhịp dash: thẳng -> lệch trái -> lệch phải
        double angleDeg = switch (t / 4) {
            case 0 -> 0;
            case 1 -> -22;
            default -> +22;
        };

        Vec3 dir = rotateY(baseFwd, Math.toRadians(angleDeg)).normalize();

        Vec3 from = caster.position();
        Vec3 toRaw = from.add(dir.scale(0.90));
        Vec3 to = safeStep(from.add(0, caster.getBbHeight() * 0.5, 0),
                toRaw.add(0, caster.getBbHeight() * 0.5, 0),
                dir);

        // teleport “flash”
        caster.teleportTo(to.x, to.y, to.z);
        caster.resetFallDistance();

        // trail points
        int seg = 8;
        ArrayList<Vec3> points = new ArrayList<>(seg + 1);

        SlashParticleOptions flash = new SlashParticleOptions(
                1.00f, 0.86f, 0.28f,
                0.95f,
                0.22f,
                10
        );

        for (int i = 0; i <= seg; i++) {
            double k = i / (double) seg;
            Vec3 p = lerp(from.add(0, 0.9, 0), to.add(0, 0.9, 0), k);
            points.add(p);

            level.sendParticles(flash, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            if ((i & 1) == 0) level.sendParticles(ParticleTypes.GLOW, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0);
        }

        FxHit.hitByPoints(caster, points, hitRadius, damage, knockback, true, hit);
        return true;
    }

    private Vec3 safeStep(Vec3 from, Vec3 to, Vec3 dir) {
        HitResult hr = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        if (hr.getType() == HitResult.Type.BLOCK) {
            Vec3 loc = hr.getLocation();
            return loc.subtract(dir.scale(0.35));
        }
        return to;
    }

    private static Vec3 lerp(Vec3 a, Vec3 b, double t) {
        return new Vec3(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t);
    }

    private static Vec3 rotateY(Vec3 v, double rad) {
        double c = Math.cos(rad), s = Math.sin(rad);
        double x = v.x * c - v.z * s;
        double z = v.x * s + v.z * c;
        return new Vec3(x, v.y, z);
    }
}
