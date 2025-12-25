package com.main.slashing.mmo.skill.actions;

import com.main.slashing.fx.ArcFanFxTask;
import com.main.slashing.fx.FxTask;
import com.main.slashing.mmo.skill.MmoAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Action: arc fan FX-only (không hit). */
public record ArcFanAction(
        int delayTicks,
        int durationTicks,
        double centerForwardOffset,
        double centerYOff,
        double arcDeg,
        double[] radii,
        int steps,
        boolean addSparks,
        boolean hot
) implements MmoAction {

    @Override
    public FxTask build(ServerPlayer caster) {
        Vec3 origin = caster.getEyePosition();
        Vec3 forward = caster.getLookAngle().normalize();
        Vec3 center = origin.add(forward.scale(centerForwardOffset)).add(0, centerYOff, 0);

        return new ArcFanFxTask(
                caster.serverLevel(),
                center,
                forward,
                radii,
                durationTicks,
                Math.toRadians(arcDeg),
                steps,
                addSparks,
                hot
        );
    }
}
