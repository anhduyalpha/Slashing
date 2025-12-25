package com.main.slashing.mmo.skill.actions;

import com.main.slashing.fx.FxTask;
import com.main.slashing.mmo.DamageSpec;
import com.main.slashing.mmo.HitFxSpec;
import com.main.slashing.mmo.SkillElement;
import com.main.slashing.mmo.fx.CurveProjectileFxTask;
import com.main.slashing.mmo.skill.MmoAction;
import net.minecraft.server.level.ServerPlayer;

/** Action: projectile bay cong nhẹ (arc) - không cần entity projectile. */
public record CurveProjectileAction(
        int delayTicks,
        double speedPerTick,
        double range,
        double arcHeight,
        double hitRadius,
        int pierce,
        boolean stopOnBlock,
        int trailParticlesPerTick,
        DamageSpec damage,
        HitFxSpec hitFx,
        SkillElement element
) implements MmoAction {

    @Override
    public FxTask build(ServerPlayer caster) {
        return new CurveProjectileFxTask(
                caster,
                Math.max(0.05, speedPerTick),
                Math.max(0.5, range),
                arcHeight,
                Math.max(0.15, hitRadius),
                Math.max(0, pierce),
                stopOnBlock,
                Math.max(0, trailParticlesPerTick),
                damage,
                hitFx,
                element
        );
    }
}
