package com.main.slashing.rpg;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.projectile.ProjectileUtil;

public final class RpgTargeting {
    private RpgTargeting() {}

    public static LivingEntity resolveTarget(Player caster, RpgTargetingDef targeting) {
        if (targeting == null) return caster;
        if ("self".equalsIgnoreCase(targeting.type())) return caster;
        if ("look_entity".equalsIgnoreCase(targeting.type())) {
            LivingEntity found = rayTraceEntity(caster, targeting.range());
            if (found != null) return found;
            if ("self".equalsIgnoreCase(targeting.fallback())) return caster;
        }
        return caster;
    }

    private static LivingEntity rayTraceEntity(Player player, double range) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eyePos.add(look.x * range, look.y * range, look.z * range);
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
            (ServerLevel) player.level(),
            player,
            eyePos,
            end,
            box,
            entity -> entity instanceof LivingEntity && entity.isPickable() && entity != player
        );
        if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
            Entity entity = hit.getEntity();
            if (entity instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }
}
