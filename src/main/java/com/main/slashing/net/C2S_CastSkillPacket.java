package com.main.slashing.net;

import com.main.slashing.skill.SkillManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class C2S_CastSkillPacket {
    private final InteractionHand hand;

    public C2S_CastSkillPacket(InteractionHand hand) {
        this.hand = hand;
    }

    public static void encode(C2S_CastSkillPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.hand);
    }

    public static C2S_CastSkillPacket decode(FriendlyByteBuf buf) {
        return new C2S_CastSkillPacket(buf.readEnum(InteractionHand.class));
    }

    public static void handle(C2S_CastSkillPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        ServerPlayer sp = c.getSender();
        c.enqueueWork(() -> {
            if (sp == null) return;
            SkillManager.castSelected(sp, msg.hand);
        });
        c.setPacketHandled(true);
    }
}
