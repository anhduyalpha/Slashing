package com.main.slashing.client.particle;

import com.main.slashing.particle.SlashParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;

public class SlashParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float baseAlpha;
    private final float baseScale;

    protected SlashParticle(ClientLevel level, double x, double y, double z, SlashParticleOptions opt, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;

        this.rCol = opt.r();
        this.gCol = opt.g();
        this.bCol = opt.b();

        this.baseAlpha = opt.a();
        this.baseScale = opt.scale();

        this.lifetime = Math.max(1, opt.lifetime());
        this.quadSize = baseScale;

        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(sprites);

        float t = age / (float) lifetime;
        this.alpha = baseAlpha * (1f - t);
        this.quadSize = baseScale * (1f - 0.35f * t);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SlashParticleOptions> {
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites) { this.sprites = sprites; }
        @Override
        public Particle createParticle(SlashParticleOptions opt, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new SlashParticle(level, x, y, z, opt, sprites);
        }
    }
}
