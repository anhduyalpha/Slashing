package com.main.slashing;

import com.main.slashing.item.ModItems;
import com.main.slashing.particle.ModParticles;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SlashingAlphadMod.MODID)
public class SlashingAlphadMod {
    public static final String MODID = "slashing_alphad";

    public SlashingAlphadMod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();

        // Register registries
        ModItems.ITEMS.register(modBus);
        ModParticles.PARTICLES.register(modBus);

        // Add items to vanilla tabs
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.COMBAT) {
            e.accept(ModItems.SLASH_WAND);
        }
    }
}
