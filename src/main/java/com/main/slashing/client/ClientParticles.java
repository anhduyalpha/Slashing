package com.main.slashing.client;

import com.main.slashing.SlashingAlphadMod;
import com.main.slashing.client.particle.SlashParticle;
import com.main.slashing.particle.ModParticles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SlashingAlphadMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientParticles {
    @SubscribeEvent
    public static void register(RegisterParticleProvidersEvent e) {
        e.registerSpriteSet(ModParticles.SLASH.get(), SlashParticle.Provider::new);
    }
}
