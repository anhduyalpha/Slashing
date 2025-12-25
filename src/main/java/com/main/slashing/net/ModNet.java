package com.main.slashing.net;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNet {
    public static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation("slashing_alphad", "net"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    private static int id = 0;
    private static boolean inited = false;

    private ModNet() {}

    public static void init() {
        if (inited) return;
        inited = true;

        CHANNEL.messageBuilder(C2S_CastSkillPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2S_CastSkillPacket::encode)
                .decoder(C2S_CastSkillPacket::decode)
                .consumerMainThread(C2S_CastSkillPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2S_CycleSkillPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2S_CycleSkillPacket::encode)
                .decoder(C2S_CycleSkillPacket::decode)
                .consumerMainThread(C2S_CycleSkillPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2C_ScreenShakePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2C_ScreenShakePacket::encode)
                .decoder(S2C_ScreenShakePacket::decode)
                .consumerMainThread(S2C_ScreenShakePacket::handle)
                .add();

        CHANNEL.messageBuilder(S2C_ScreenFlashPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2C_ScreenFlashPacket::encode)
                .decoder(S2C_ScreenFlashPacket::decode)
                .consumerMainThread(S2C_ScreenFlashPacket::handle)
                .add();
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }

    public static void sendToPlayer(net.minecraft.server.level.ServerPlayer player, Object msg) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}
