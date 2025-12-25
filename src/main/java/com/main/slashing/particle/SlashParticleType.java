package com.main.slashing.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

public class SlashParticleType extends ParticleType<SlashParticleOptions> {
    public SlashParticleType() {
        super(false, SlashParticleOptions.DESERIALIZER);
    }

    @Override
    public Codec<SlashParticleOptions> codec() {
        return SlashParticleOptions.CODEC;
    }
}
