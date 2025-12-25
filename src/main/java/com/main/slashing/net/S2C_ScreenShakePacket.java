package com.main.slashing.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class S2C_ScreenShakePacket {
    private final float intensity;
    private final int durationTicks;
    private final float frequency;

    public S2C_ScreenShakePacket(float intensity, int durationTicks, float frequency) {
        this.intensity = intensity;
        this.durationTicks = Math.max(1, durationTicks);
        this.frequency = Math.max(0.1f, frequency);
    }

    public static void encode(S2C_ScreenShakePacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.intensity);
        buf.writeInt(msg.durationTicks);
        buf.writeFloat(msg.frequency);
    }

    public static S2C_ScreenShakePacket decode(FriendlyByteBuf buf) {
        return new S2C_ScreenShakePacket(buf.readFloat(), buf.readInt(), buf.readFloat());
    }

    public static void handle(S2C_ScreenShakePacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            com.main.slashing.client.ClientScreenShake.addShake(msg.intensity, msg.durationTicks, msg.frequency);
        }));
        c.setPacketHandled(true);
    }
}
