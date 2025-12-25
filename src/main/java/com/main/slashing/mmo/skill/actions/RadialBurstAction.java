package com.main.slashing.mmo.skill.actions;

import com.main.slashing.fx.FxTask;
import com.main.slashing.fx.RadialBurstFxTask;
import com.main.slashing.mmo.skill.MmoAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Action: radial burst FX-only. */
public record RadialBurstAction(
        int delayTicks,
        int durationTicks,
        double centerYOff
) implements MmoAction {

    @Override
    public FxTask build(ServerPlayer caster) {
        Vec3 center = caster.position().add(0, centerYOff, 0);
        return new RadialBurstFxTask(caster.serverLevel(), center, durationTicks);
    }
}
