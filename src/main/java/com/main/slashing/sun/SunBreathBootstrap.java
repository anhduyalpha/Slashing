package com.main.slashing.sun;

import net.minecraftforge.eventbus.api.IEventBus;

public final class SunBreathBootstrap {
    private SunBreathBootstrap() {}

    public static void register(IEventBus modBus) {
        SunBreathContent.attach(modBus);
    }
}
