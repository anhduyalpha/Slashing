package com.main.slashing.fx;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.LinkedList;

@Mod.EventBusSubscriber(modid = "slashing_alphad")
public final class FxManager {
    private static final LinkedList<FxTask> TASKS = new LinkedList<>();
    private static final int MAX_TASKS = 512;
    private static final int MAX_PARTICLES_PER_TICK = 3200;

    private FxManager() {}

    public static void schedule(FxTask task) {
        if (task == null) return;
        if (TASKS.size() >= MAX_TASKS) return;
        TASKS.add(task);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (TASKS.isEmpty()) return;

        int budget = MAX_PARTICLES_PER_TICK;
        Iterator<FxTask> it = TASKS.iterator();

        while (it.hasNext()) {
            FxTask t = it.next();

            int est = Math.max(0, t.estimatedParticlesThisTick());
            if (budget - est < 0) break;
            budget -= est;

            boolean keep;
            try {
                keep = t.tick();
            } catch (Throwable ex) {
                keep = false;
            }
            if (!keep) it.remove();
        }
    }
}
