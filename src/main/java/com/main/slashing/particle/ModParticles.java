package com.main.slashing.particle;

import com.main.slashing.SlashingAlphadMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SlashingAlphadMod.MODID);

    public static final RegistryObject<ParticleType<SlashParticleOptions>> SLASH =
            PARTICLES.register("slash", SlashParticleType::new);

    private ModParticles() {}
}
