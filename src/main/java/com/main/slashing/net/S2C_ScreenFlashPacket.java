package com.main.slashing.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Flash toàn màn hình ở client (giống hit/skill flash MMO).
 * Chỉ render overlay UI, tương thích với shader mods tốt hơn so với post-processing.
 */
public final class S2C_ScreenFlashPacket {
    private final int rgb;
    private final float alpha;
    private final int durationTicks;

    public S2C_ScreenFlashPacket(int rgb, float alpha, int durationTicks) {
        this.rgb = rgb;
        this.alpha = Math.max(0f, Math.min(1f, alpha));
        this.durationTicks = Math.max(1, durationTicks);
    }

    public static void encode(S2C_ScreenFlashPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.rgb);
        buf.writeFloat(msg.alpha);
        buf.writeVarInt(msg.durationTicks);
    }

    public static S2C_ScreenFlashPacket decode(FriendlyByteBuf buf) {
        return new S2C_ScreenFlashPacket(buf.readInt(), buf.readFloat(), buf.readVarInt());
    }

    public static void handle(S2C_ScreenFlashPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            com.main.slashing.client.ClientScreenFlash.addFlash(msg.rgb, msg.alpha, msg.durationTicks);
        }));
        c.setPacketHandled(true);
    }
}
