package com.main.slashing.rpg;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "slashing_alphad")
public final class RpgEvents {
    private static int regenTicker = 0;

    private RpgEvents() {}

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        RpgDataManager.loadAll();
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        RpgCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        RpgCastManager.tick(server);
        regenTicker++;
        if (regenTicker >= 20) {
            regenTicker = 0;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                RpgCastManager.tickResourcesAndStatuses(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RpgProfileManager.get(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RpgProfileManager.remove(player);
        }
    }
}
