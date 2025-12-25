package com.main.slashing.mmo.skill;

import com.main.slashing.fx.FxTask;
import net.minecraft.server.level.ServerPlayer;

/**
 * 1 action của skill: khi cast, action sẽ tạo FxTask (có thể có delay).
 */
public interface MmoAction {
    int delayTicks();
    FxTask build(ServerPlayer caster);
}
