package com.main.slashing.mmo.skill.actions;

import com.main.slashing.fx.FxTask;
import com.main.slashing.fx.SpiralFxTask;
import com.main.slashing.mmo.skill.MmoAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Action: spiral FX-only. */
public record SpiralAction(
        int delayTicks,
        int durationTicks,
        int pointsPerTick,
        double originForwardOffset,
        double originYOff
) implements MmoAction {

    @Override
    public FxTask build(ServerPlayer caster) {
        Vec3 origin = caster.getEyePosition().add(0, originYOff, 0);
        Vec3 forward = caster.getLookAngle().normalize();
        origin = origin.add(forward.scale(originForwardOffset));
        return new SpiralFxTask(caster.serverLevel(), origin, forward, durationTicks, pointsPerTick);
    }
}
