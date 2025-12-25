package com.main.slashing.mmo.skill.actions;

import com.main.slashing.fx.FxTask;
import com.main.slashing.mmo.DamageSpec;
import com.main.slashing.mmo.HitFxSpec;
import com.main.slashing.mmo.SkillElement;
import com.main.slashing.mmo.fx.RaycastStrikeFxTask;
import com.main.slashing.mmo.skill.MmoAction;
import net.minecraft.server.level.ServerPlayer;

/** Action: raycast strike (đâm/chém tia) có damage + hit FX. */
public record RaycastStrikeAction(
        int delayTicks,
        double range,
        double hitRadius,
        int particleSteps,
        boolean addSparks,
        DamageSpec damage,
        HitFxSpec hitFx,
        SkillElement element
) implements MmoAction {

    @Override
    public FxTask build(ServerPlayer caster) {
        return new RaycastStrikeFxTask(
                caster,
                range,
                hitRadius,
                Math.max(6, particleSteps),
                addSparks,
                damage,
                hitFx,
                element
        );
    }
}
