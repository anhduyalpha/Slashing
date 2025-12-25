package com.main.slashing.fx;

public interface FxTask {
    boolean tick();
    default int estimatedParticlesThisTick() { return 0; }
}
