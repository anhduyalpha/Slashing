package com.main.slashing.fx;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;

public final class FxHit {
    private FxHit() {}

    public static void hitByPoints(ServerPlayer caster,
                                   List<Vec3> points,
                                   double radius,
                                   float damage,
                                   float knockback,
                                   boolean setOnFireSeconds2,
                                   HashSet<Integer> alreadyHit) {

        if (points == null || points.isEmpty()) return;

        ServerLevel level = caster.serverLevel();

        double minX = points.get(0).x, minY = points.get(0).y, minZ = points.get(0).z;
        double maxX = minX, maxY = minY, maxZ = minZ;

        for (Vec3 p : points) {
            if (p.x < minX) minX = p.x; if (p.y < minY) minY = p.y; if (p.z < minZ) minZ = p.z;
            if (p.x > maxX) maxX = p.x; if (p.y > maxY) maxY = p.y; if (p.z > maxZ) maxZ = p.z;
        }

        AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ).inflate(radius);

        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, box, e ->
                e.isAlive()
                        && e != caster
                        && !e.isInvulnerable()
                        && !caster.isAlliedTo(e)
        );

        if (candidates.isEmpty()) return;

        DamageSource src = level.damageSources().playerAttack(caster);
        double r2 = radius * radius;

        for (LivingEntity e : candidates) {
            int id = e.getId();
            if (alreadyHit != null && alreadyHit.contains(id)) continue;

            Vec3 ep = e.position().add(0, e.getBbHeight() * 0.55, 0);

            if (nearAnyPoint(ep, points, r2)) {
                if (alreadyHit != null) alreadyHit.add(id);

                e.hurt(src, damage);

                if (knockback > 0f) {
                    Vec3 fwd = caster.getLookAngle().normalize();
                    e.knockback(knockback, -fwd.x, -fwd.z);
                }

                if (setOnFireSeconds2) e.setSecondsOnFire(2);
            }
        }
    }

    private static boolean nearAnyPoint(Vec3 ep, List<Vec3> points, double r2) {
        for (Vec3 p : points) {
            if (ep.distanceToSqr(p) <= r2) return true;
        }
        return false;
    }
}
