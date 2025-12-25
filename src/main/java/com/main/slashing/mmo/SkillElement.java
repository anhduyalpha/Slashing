package com.main.slashing.mmo;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.StringRepresentable;
import com.main.slashing.fx.FxParticles;

import java.util.Locale;

/**
 * "Hệ" của skill (giống MMO: Fire/Ice/Lightning...).
 * Trong template này, Element chủ yếu quyết định màu FX + gợi ý hiệu ứng on-hit.
 */
public enum SkillElement implements StringRepresentable {
    PHYSICAL(0xE0E0E0, ParticleTypes.CRIT),
    FIRE(0xFF7A1A, ParticleTypes.FLAME),
    ICE(0x7AD7FF, ParticleTypes.SNOWFLAKE),
    LIGHTNING(0xBFA6FF, ParticleTypes.ELECTRIC_SPARK),
    WATER(0x4CC3FF, ParticleTypes.BUBBLE_POP),
    WIND(0xCFFFFF, ParticleTypes.CLOUD),
    EARTH(0xC2A57A, ParticleTypes.FALLING_DUST),
    HOLY(0xFFF2A8, ParticleTypes.GLOW),
    DARK(0x5B4B7A, ParticleTypes.SMOKE);

    private final int rgb;
    private final ParticleOptions accentParticle;

    SkillElement(int rgb, ParticleOptions accentParticle) {
        this.rgb = rgb;
        this.accentParticle = accentParticle;
    }

    public int rgb() {
        return rgb;
    }

    /** Particle phụ để "glow"/accent khi cast hoặc khi hit. */
    public ParticleOptions accentParticle() {
        return accentParticle;
    }

    /** Dust particle (đẹp với shader) theo element. */
    public ParticleOptions dust() {
        // reuse helper dust warm color cho FIRE/HOLY; còn lại dùng màu RGB
        return switch (this) {
            case FIRE -> FxParticles.sunHot();
            case HOLY -> FxParticles.sunSlash();
            default -> {
                float r = ((rgb >> 16) & 0xFF) / 255f;
                float g = ((rgb >> 8) & 0xFF) / 255f;
                float b = (rgb & 0xFF) / 255f;
                yield new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(r, g, b), 1.2f);
            }
        };
    }

    public static SkillElement fromString(String s, SkillElement fallback) {
        if (s == null) return fallback;
        try {
            return SkillElement.valueOf(s.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
