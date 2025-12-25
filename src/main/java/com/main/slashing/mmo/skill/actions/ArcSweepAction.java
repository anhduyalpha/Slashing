package com.main.slashing.mmo.skill.actions;

import com.main.slashing.fx.FxTask;
import com.main.slashing.mmo.DamageSpec;
import com.main.slashing.mmo.HitFxSpec;
import com.main.slashing.mmo.SkillElement;
import com.main.slashing.mmo.fx.ArcSweepHitFxTask;
import com.main.slashing.mmo.skill.MmoAction;
import net.minecraft.server.level.ServerPlayer;

/** Action: đòn chém arc có damage + hit FX (data-driven). */
public record ArcSweepAction(
        int delayTicks,
        int durationTicks,
        double centerForwardOffset,
        double centerYOff,
        double arcDeg,
        double sweepDeg,
        double[] radii,
        int steps,
        int sampleStride,
        double hitRadius,
        DamageSpec damage,
        HitFxSpec hitFx,
        SkillElement element,
        boolean followCaster,
        boolean addSparks
) implements MmoAction {

    @Override
    public FxTask build(ServerPlayer caster) {
        return new ArcSweepHitFxTask(
                caster,
                durationTicks,
                centerForwardOffset,
                centerYOff,
                Math.toRadians(arcDeg),
                Math.toRadians(sweepDeg),
                radii,
                steps,
                sampleStride,
                hitRadius,
                damage,
                hitFx,
                element,
                followCaster,
                addSparks
        );
    }
}
