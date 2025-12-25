package com.main.slashing.fx;

public final class DelayedFxTask implements FxTask {
    private int delayTicks;
    private final FxTask inner;

    public DelayedFxTask(int delayTicks, FxTask inner) {
        this.delayTicks = Math.max(0, delayTicks);
        this.inner = inner;
    }

    @Override
    public int estimatedParticlesThisTick() {
        if (delayTicks > 0) return 0;
        return inner == null ? 0 : inner.estimatedParticlesThisTick();
    }

    @Override
    public boolean tick() {
        if (inner == null) return false;
        if (delayTicks-- > 0) return true;
        return inner.tick();
    }
}
