package com.main.slashing.fx;

import com.main.slashing.particle.SlashParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.LinkedList;

@Mod.EventBusSubscriber
public final class ServerSkillFx {

    private static final LinkedList<SlashTask> TASKS = new LinkedList<>();
    private static final RandomSource RAND = RandomSource.create();

    private ServerSkillFx() {}

    /** Gọi hàm này để tạo 1 đòn chém: 2–3 lớp arc + trail kéo dài nhiều tick */
    public static void spawnSlashSweep(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        Vec3 origin = player.getEyePosition();
        Vec3 forward = player.getLookAngle().normalize();
        Vec3 center = origin.add(forward.scale(1.25)); // trước mặt

        // 2–3 vòng arc (radius khác nhau)
        double[] radii = new double[] { 1.10, 1.32, 1.54 };

        // Trail dài: 8 tick quét
        TASKS.add(new SlashTask(level, center, forward, radii,
                8,                      // duration ticks
                Math.toRadians(165),     // arc angle
                26                      // points per tick (mịn hơn = nặng hơn)
        ));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (TASKS.isEmpty()) return;

        Iterator<SlashTask> it = TASKS.iterator();
        while (it.hasNext()) {
            SlashTask t = it.next();
            if (t.ticksLeft-- <= 0) { it.remove(); continue; }
            runSlashTick(t);
        }
    }

    private static void runSlashTick(SlashTask t) {
        float prog = 1f - (t.ticksLeft / (float) t.totalTicks);

        // Mỗi tick quét lệch góc => tạo “trail”
        double sweepShift = Mth.lerp(prog, -0.70, 0.70);

        // Basis (right/up) vuông góc forward
        Vec3 f = t.forward.normalize();
        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 right = f.cross(worldUp);
        if (right.lengthSqr() < 1.0e-6) right = new Vec3(1, 0, 0);
        right = right.normalize();
        Vec3 up = right.cross(f).normalize();

        double start = -t.arcAngleRad * 0.5 + sweepShift;
        double end   =  t.arcAngleRad * 0.5 + sweepShift;

        // Màu mặc định “water-ish” (bạn đổi style sau)
        float r = 0.55f, g = 0.95f, b = 1.00f;

        for (double radius : t.radii) {
            for (int i = 0; i <= t.steps; i++) {
                double tt = (double) i / (double) t.steps;
                double ang = start + (end - start) * tt;

                Vec3 p = t.center
                        .add(right.scale(radius * Math.cos(ang)))
                        .add(up.scale(radius * Math.sin(ang)));

                // Hạt slash chính (texture đẹp)
                SlashParticleOptions opt = new SlashParticleOptions(
                        r, g, b,
                        0.85f,
                        (float)(0.22 + 0.06 * RAND.nextDouble()),
                        10 + RAND.nextInt(6)
                );
                t.level.sendParticles(opt, p.x, p.y, p.z, 1, 0, 0, 0, 0);

                // Spark phụ để “đã”
                if ((i & 3) == 0) {
                    SlashParticleOptions spark = new SlashParticleOptions(
                            1.00f, 1.00f, 1.00f,
                            0.70f,
                            0.12f,
                            6 + RAND.nextInt(4)
                    );
                    t.level.sendParticles(spark, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0);
                }
            }
        }
    }

    private static final class SlashTask {
        final ServerLevel level;
        final Vec3 center;
        final Vec3 forward;
        final double[] radii;
        final int totalTicks;
        final double arcAngleRad;
        final int steps;
        int ticksLeft;

        SlashTask(ServerLevel level, Vec3 center, Vec3 forward, double[] radii,
                  int totalTicks, double arcAngleRad, int steps) {
            this.level = level;
            this.center = center;
            this.forward = forward;
            this.radii = radii;
            this.totalTicks = totalTicks;
            this.arcAngleRad = arcAngleRad;
            this.steps = steps;
            this.ticksLeft = totalTicks;
        }
    }
}
