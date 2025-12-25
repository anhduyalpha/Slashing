package com.main.slashing.sun;

import com.main.slashing.net.ModNet;
import com.main.slashing.skill.SkillRegistry;
import com.main.slashing.skill.SlashSweepSkill;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = "slashing_alphad", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SunBreathContent {
    private static boolean attached = false;

    private SunBreathContent() {}

    public static void attach(IEventBus modBus) {
        if (attached) return;
        attached = true;
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            ModNet.init();
            SunBreathSkills.registerAll(SkillRegistry::register);

            SkillRegistry.register(new SlashSweepSkill());

        });
    }
}
