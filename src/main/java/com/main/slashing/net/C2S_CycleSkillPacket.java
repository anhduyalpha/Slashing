package com.main.slashing.net;

import com.main.slashing.skill.SkillManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class C2S_CycleSkillPacket {
    private final InteractionHand hand;
    private final int dir;

    public C2S_CycleSkillPacket(InteractionHand hand, int dir) {
        this.hand = hand;
        this.dir = dir >= 0 ? 1 : -1;
    }

    public static void encode(C2S_CycleSkillPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.hand);
        buf.writeInt(msg.dir);
    }

    public static C2S_CycleSkillPacket decode(FriendlyByteBuf buf) {
        return new C2S_CycleSkillPacket(buf.readEnum(InteractionHand.class), buf.readInt());
    }

    public static void handle(C2S_CycleSkillPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        ServerPlayer sp = c.getSender();
        c.enqueueWork(() -> {
            if (sp == null) return;
            ItemStack stack = sp.getItemInHand(msg.hand);
            SkillManager.cycleSkill(sp, stack, msg.dir);

            var skill = SkillManager.getSelectedSkillObj(sp, stack);
            if (skill != null) sp.displayClientMessage(skill.displayName(), true);
        });
        c.setPacketHandled(true);
    }
}
