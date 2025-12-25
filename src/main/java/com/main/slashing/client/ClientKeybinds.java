package com.main.slashing.client;

import com.main.slashing.net.C2S_CastSkillPacket;
import com.main.slashing.net.C2S_CycleSkillPacket;
import com.main.slashing.net.ModNet;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "slashing_alphad", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientKeybinds {
    public static final String CAT = "key.categories.slashing_alphad";

    public static KeyMapping CAST;
    public static KeyMapping NEXT;
    public static KeyMapping PREV;

    private ClientKeybinds() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent e) {
        CAST = new KeyMapping("key.slashing_alphad.cast", GLFW.GLFW_KEY_G, CAT);
        NEXT = new KeyMapping("key.slashing_alphad.next_form", GLFW.GLFW_KEY_R, CAT);
        PREV = new KeyMapping("key.slashing_alphad.prev_form", GLFW.GLFW_KEY_Z, CAT);

        e.register(CAST);
        e.register(NEXT);
        e.register(PREV);
    }

    @Mod.EventBusSubscriber(modid = "slashing_alphad", value = Dist.CLIENT)
    public static final class Runtime {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent e) {
            if (e.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            while (CAST.consumeClick()) {
                ModNet.sendToServer(new C2S_CastSkillPacket(InteractionHand.MAIN_HAND));
            }
            while (NEXT.consumeClick()) {
                ModNet.sendToServer(new C2S_CycleSkillPacket(InteractionHand.MAIN_HAND, +1));
            }
            while (PREV.consumeClick()) {
                ModNet.sendToServer(new C2S_CycleSkillPacket(InteractionHand.MAIN_HAND, -1));
            }
        }
    }
}
