package com.main.slashing.mmo.skill.actions;

import com.main.slashing.fx.FxTask;
import com.main.slashing.mmo.DamageSpec;
import com.main.slashing.mmo.HitFxSpec;
import com.main.slashing.mmo.SkillElement;
import com.main.slashing.mmo.fx.GroundAoeFxTask;
import com.main.slashing.mmo.skill.MmoAction;
import net.minecraft.server.level.ServerPlayer;

/** Action: đặt AOE dưới đất (telegraph -> nổ) giống MMO. */
public record GroundAoeAction(
        int delayTicks,
        int telegraphTicks,
        double centerForwardOffset,
        double centerYOff,
        double radius,
        DamageSpec damage,
        HitFxSpec hitFx,
        SkillElement element
) implements MmoAction {

    @Override
    public FxTask build(ServerPlayer caster) {
        return new GroundAoeFxTask(
                caster,
                Math.max(1, telegraphTicks),
                centerForwardOffset,
                centerYOff,
                Math.max(0.25, radius),
                damage,
                hitFx,
                element
        );
    }
}
