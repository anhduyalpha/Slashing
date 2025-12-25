package com.main.slashing.mmo.fx;

import com.main.slashing.fx.FxTask;
import com.main.slashing.mmo.DamageSpec;
import com.main.slashing.mmo.HitFxSpec;
import com.main.slashing.mmo.MmoHitFx;
import com.main.slashing.mmo.SkillElement;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Ground AoE: telegraph vòng tròn -> nổ.
 * Tối ưu: mỗi tick chỉ vẽ ít điểm của vòng.
 */
public final class GroundAoeFxTask implements FxTask {
    private final ServerPlayer caster;
    private final ServerLevel level;
    private final int telegraphTicks;
    private final double centerForward;
    private final double centerY;
    private final double radius;
    private final DamageSpec damage;
    private final HitFxSpec hitFx;
    private final SkillElement element;

    private int tick = 0;

    public GroundAoeFxTask(
            ServerPlayer caster,
            int telegraphTicks,
            double centerForward,
            double centerY,
            double radius,
            DamageSpec damage,
            HitFxSpec hitFx,
            SkillElement element
    ) {
        this.caster = caster;
        this.level = caster.serverLevel();
        this.telegraphTicks = Math.max(1, telegraphTicks);
        this.centerForward = centerForward;
        this.centerY = centerY;
        this.radius = radius;
        this.damage = damage;
        this.hitFx = hitFx;
        this.element = element;
    }

    @Override
    public int estimatedParticlesThisTick() {
        // vòng tròn ~ 12 điểm/tick, nổ ~ 80
        return tick < telegraphTicks ? 12 : 80;
    }

    @Override
    public boolean tick() {
        if (caster == null || caster.isRemoved()) return false;

        Vec3 center = caster.position().add(caster.getLookAngle().normalize().scale(centerForward)).add(0, centerY, 0);

        if (tick < telegraphTicks) {
            // Telegraph: vẽ vòng (12 điểm) + 1 particle ở tâm
            int pts = 12;
            double angOff = (tick * 0.35) % (Math.PI * 2);
            for (int i = 0; i < pts; i++) {
                double a = angOff + (Math.PI * 2) * (i / (double) pts);
                double x = center.x + Math.cos(a) * radius;
                double z = center.z + Math.sin(a) * radius;
                level.sendParticles(element.dust(), x, center.y + 0.05, z, 1, 0, 0, 0, 0);
            }
            level.sendParticles(element.accentParticle(), center.x, center.y + 0.10, center.z, 1, 0, 0, 0, 0);
            tick++;
            return true;
        }

        // Detonate
        level.sendParticles(element.accentParticle(), center.x, center.y + 0.10, center.z, 35, 0.55, 0.10, 0.55, 0.04);
        level.sendParticles(element.dust(), center.x, center.y + 0.10, center.z, 45, 0.65, 0.12, 0.65, 0.02);

        AABB box = new AABB(center, center).inflate(radius, 1.25, radius);
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, box, e ->
                e != caster && e.isAlive() && !e.isSpectator()
        );

        for (LivingEntity e : list) {
            double d2 = e.position().distanceToSqr(center);
            if (d2 <= radius * radius) {
                // knockback hướng ra ngoài
                Vec3 fwd = e.position().subtract(center);
                if (fwd.lengthSqr() < 1e-6) fwd = caster.getLookAngle();
                Vec3 dir = fwd.normalize();
                damage.apply(caster, e);

                MmoHitFx.onHit(caster, e, element, hitFx);
            }
        }

        return false;
    }
}
