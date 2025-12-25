package com.main.slashing.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "slashing_alphad", value = Dist.CLIENT)
public final class ClientScreenShake {
    private static final LinkedList<Shake> SHAKES = new LinkedList<>();
    private static final Random RAND = new Random(1337);

    private ClientScreenShake() {}

    public static void addShake(float intensity, int durationTicks, float frequency) {
        SHAKES.add(new Shake(intensity, durationTicks, frequency, RAND.nextLong()));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (SHAKES.isEmpty()) return;

        Iterator<Shake> it = SHAKES.iterator();
        while (it.hasNext()) {
            Shake s = it.next();
            if (--s.ticksLeft <= 0) it.remove();
        }
    }

    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles e) {
        if (SHAKES.isEmpty()) return;

        float yaw = e.getYaw();
        float pitch = e.getPitch();
        float roll = e.getRoll();

        float addYaw = 0f, addPitch = 0f, addRoll = 0f;

        for (Shake s : SHAKES) {
            float t = (s.totalTicks - s.ticksLeft) + (float)e.getPartialTick();
            float fade = s.ticksLeft / (float) s.totalTicks;

            float n1 = (float)Math.sin((t * s.frequency) + (s.seed * 0.000001));
            float n2 = (float)Math.sin((t * s.frequency * 1.31) + (s.seed * 0.000002));
            float n3 = (float)Math.sin((t * s.frequency * 0.87) + (s.seed * 0.000003));

            float amp = s.intensity * fade;

            addYaw   += n1 * amp * 1.25f;
            addPitch += n2 * amp * 0.95f;
            addRoll  += n3 * amp * 0.55f;
        }

        e.setYaw(yaw + addYaw);
        e.setPitch(pitch + addPitch);
        e.setRoll(roll + addRoll);
    }

    private static final class Shake {
        final float intensity;
        final int totalTicks;
        final float frequency;
        final long seed;
        int ticksLeft;

        Shake(float intensity, int totalTicks, float frequency, long seed) {
            this.intensity = Math.max(0f, intensity);
            this.totalTicks = Math.max(1, totalTicks);
            this.frequency = Math.max(0.1f, frequency);
            this.seed = seed;
            this.ticksLeft = this.totalTicks;
        }
    }
}
