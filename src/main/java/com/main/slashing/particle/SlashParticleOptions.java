package com.main.slashing.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public record SlashParticleOptions(
        float r, float g, float b,
        float a,
        float scale,
        int lifetime
) implements ParticleOptions {

    // Codec (world save / command / etc)
    public static final Codec<SlashParticleOptions> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.fieldOf("r").forGetter(SlashParticleOptions::r),
            Codec.FLOAT.fieldOf("g").forGetter(SlashParticleOptions::g),
            Codec.FLOAT.fieldOf("b").forGetter(SlashParticleOptions::b),
            Codec.FLOAT.fieldOf("a").forGetter(SlashParticleOptions::a),
            Codec.FLOAT.fieldOf("scale").forGetter(SlashParticleOptions::scale),
            Codec.INT.fieldOf("lifetime").forGetter(SlashParticleOptions::lifetime)
    ).apply(inst, SlashParticleOptions::new));

    public static final Deserializer<SlashParticleOptions> DESERIALIZER = new Deserializer<>() {
        @Override
        public SlashParticleOptions fromNetwork(ParticleType<SlashParticleOptions> type, FriendlyByteBuf buf) {
            return new SlashParticleOptions(
                    buf.readFloat(), buf.readFloat(), buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readVarInt()
            );
        }

        @Override
        public SlashParticleOptions fromCommand(ParticleType<SlashParticleOptions> type, StringReader reader)
                throws CommandSyntaxException {
            // Nếu bạn không cần /particle spawn custom options, trả mặc định để compile
            return new SlashParticleOptions(0.55f, 0.95f, 1.00f, 0.85f, 0.22f, 12);
        }
    };

    @Override
    public ParticleType<?> getType() {
        return ModParticles.SLASH.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(r); buf.writeFloat(g); buf.writeFloat(b);
        buf.writeFloat(a);
        buf.writeFloat(scale);
        buf.writeVarInt(lifetime);
    }

    @Override
    public String writeToString() {
        return "slash";
    }
}
