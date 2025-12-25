package com.main.slashing.mmo;

public record ScreenFlashSpec(int rgb, float alpha, int durationTicks) {
    public static ScreenFlashSpec none() {
        return new ScreenFlashSpec(0xFFFFFF, 0f, 1);
    }
}
