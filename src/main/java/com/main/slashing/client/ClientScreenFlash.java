package com.main.slashing.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Flash overlay toàn màn hình.
 * Dùng cho "skill impact" / "hit confirm" kiểu MMO.
 */
@Mod.EventBusSubscriber(modid = "slashing_alphad", value = Dist.CLIENT)
public final class ClientScreenFlash {

    private static final LinkedList<Flash> FLASHES = new LinkedList<>();

    private ClientScreenFlash() {}

    public static void addFlash(int rgb, float alpha, int durationTicks) {
        FLASHES.add(new Flash(rgb, alpha, durationTicks));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (FLASHES.isEmpty()) return;

        Iterator<Flash> it = FLASHES.iterator();
        while (it.hasNext()) {
            Flash f = it.next();
            if (--f.ticksLeft <= 0) it.remove();
        }
    }

    /**
     * Vẽ sau cùng lên ALL overlay. Nếu bị mod khác chặn, bạn có thể đổi sang Post/Pre khác.
     */
    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Post e) {
        if (FLASHES.isEmpty()) return;
        if (Minecraft.getInstance().player == null) return;

        // Render 1 lớp duy nhất: cộng dồn alpha + blend. (Nhẹ hơn so với vẽ nhiều layer)
        float r = 0f, g = 0f, b = 0f, a = 0f;
        for (Flash f : FLASHES) {
            float t = 1f - (f.ticksLeft / (float) f.totalTicks);
            // fade in nhanh, fade out chậm
            float fade = t < 0.25f ? (t / 0.25f) : (1f - (t - 0.25f) / 0.75f);
            if (fade < 0f) fade = 0f;
            int rgb = f.rgb;
            float fr = ((rgb >> 16) & 0xFF) / 255f;
            float fg = ((rgb >> 8) & 0xFF) / 255f;
            float fb = (rgb & 0xFF) / 255f;
            float fa = f.alpha * fade;
            r += fr * fa;
            g += fg * fa;
            b += fb * fa;
            a += fa;
        }

        if (a <= 0.001f) return;

        // clamp
        r = Math.min(1f, r);
        g = Math.min(1f, g);
        b = Math.min(1f, b);
        a = Math.min(0.85f, a); // tránh trắng xóa

        GuiGraphics gg = e.getGuiGraphics();

        RenderSystem.enableBlend();
        // gg.fill dùng ARGB
        int argb = ((int) (a * 255) << 24)
                | ((int) (r * 255) << 16)
                | ((int) (g * 255) << 8)
                | ((int) (b * 255));
        gg.fill(0, 0, gg.guiWidth(), gg.guiHeight(), argb);
        RenderSystem.disableBlend();
    }

    private static final class Flash {
        final int rgb;
        final float alpha;
        final int totalTicks;
        int ticksLeft;

        Flash(int rgb, float alpha, int durationTicks) {
            this.rgb = rgb;
            this.alpha = Math.max(0f, Math.min(1f, alpha));
            this.totalTicks = Math.max(1, durationTicks);
            this.ticksLeft = this.totalTicks;
        }
    }
}
