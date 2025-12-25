package com.main.slashing.rpg;

public final class ResourceState {
    private final double max;
    private final double regenPerSec;
    private final double gainOnCast;
    private double current;

    public ResourceState(double max, double regenPerSec, double gainOnCast, double current) {
        this.max = max;
        this.regenPerSec = regenPerSec;
        this.gainOnCast = gainOnCast;
        this.current = current;
    }

    public double max() {
        return max;
    }

    public double regenPerSec() {
        return regenPerSec;
    }

    public double gainOnCast() {
        return gainOnCast;
    }

    public double current() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = Math.min(max, Math.max(0, current));
    }
}
