package com.main.slashing.mmo;

/**
 * FX khi trúng entity ("hit confirm").
 * Để tối ưu, template chỉ gửi packet flash/shake cho chính caster.
 */
public record HitFxSpec(
        int extraParticles,
        float hitScreenShakeIntensity,
        int hitScreenShakeTicks,
        float hitScreenShakeFreq,
        int hitFlashRgb,
        float hitFlashAlpha,
        int hitFlashTicks,
        SoundSpec hitSound
) {
    public static HitFxSpec none() {
        return new HitFxSpec(0, 0f, 1, 1f, 0xFFFFFF, 0f, 1, SoundSpec.none());
    }
}
