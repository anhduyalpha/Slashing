package com.main.slashing.mmo;

public record ScreenShakeSpec(float intensity, int durationTicks, float frequency) {
    public static ScreenShakeSpec none() {
        return new ScreenShakeSpec(0f, 1, 1f);
    }
}
