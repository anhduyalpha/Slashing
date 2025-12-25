package com.main.slashing.sun;

import com.main.slashing.fx.*;
import com.main.slashing.net.ModNet;
import com.main.slashing.net.S2C_ScreenShakePacket;
import com.main.slashing.skill.ISkill;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public final class SunBreathSkills {
    public static final String MODID = "slashing_alphad";

    public static final ResourceLocation FORM1 = new ResourceLocation(MODID, "sun_form_1");
    public static final ResourceLocation FORM2 = new ResourceLocation(MODID, "sun_form_2");
    public static final ResourceLocation FORM3 = new ResourceLocation(MODID, "sun_form_3");
    public static final ResourceLocation FORM4 = new ResourceLocation(MODID, "sun_form_4");
    public static final ResourceLocation FORM5 = new ResourceLocation(MODID, "sun_form_5");
    public static final ResourceLocation FORM6 = new ResourceLocation(MODID, "sun_form_6");
    public static final ResourceLocation FORM7 = new ResourceLocation(MODID, "sun_form_7");
    public static final ResourceLocation FORM8  = new ResourceLocation(MODID, "sun_form_8");
    public static final ResourceLocation FORM9  = new ResourceLocation(MODID, "sun_form_9");
    public static final ResourceLocation FORM10 = new ResourceLocation(MODID, "sun_form_10");
    public static final ResourceLocation FORM11 = new ResourceLocation(MODID, "sun_form_11");

    private SunBreathSkills() {}

    public static void registerAll(Consumer<ISkill> reg) {
        reg.accept(new Form1());
        reg.accept(new Form2());
        reg.accept(new Form3());
        reg.accept(new Form4());
        reg.accept(new Form5());
        reg.accept(new Form6());
        reg.accept(new Form7());
        reg.accept(new Form8());
        reg.accept(new Form9());
        reg.accept(new Form10());
        reg.accept(new Form11());
    }

    private static void shake(ServerPlayer p, float intensity, int duration, float freq) {
        ModNet.sendToPlayer(p, new S2C_ScreenShakePacket(intensity, duration, freq));
    }

    private static void soundSweep(ServerPlayer p) {
        p.serverLevel().playSound(null, p.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                1.0f, 1.0f + (p.getRandom().nextFloat() - 0.5f) * 0.15f);
        p.serverLevel().playSound(null, p.blockPosition(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS,
                0.7f, 1.1f + (p.getRandom().nextFloat() - 0.5f) * 0.10f);
    }

    /** Form I: tight fast arc */
    private static final class Form1 implements ISkill {
        private final ResourceLocation id = FORM1;
        @Override public ResourceLocation id() { return id; }
        @Override public String nameKey() { return "skill.slashing_alphad.sun_form_1"; }
        @Override public int cooldownTicks() { return 10; }

        @Override
        public void cast(ServerPlayer p, ItemStack stack, InteractionHand hand) {
            Vec3 origin = p.getEyePosition();
            Vec3 forward = p.getLookAngle().normalize();
            Vec3 center = origin.add(forward.scale(1.25));

            FxManager.schedule(new ArcFanFxTask(
                    p.serverLevel(), center, forward,
                    new double[]{1.10, 1.32},
                    6, Math.toRadians(130), 24,
                    true, false
            ));

            soundSweep(p);
            shake(p, 1.8f, 7, 0.90f);
        }
    }

    /** Form II: 3 rapid arcs */
    private static final class Form2 implements ISkill {
        private final ResourceLocation id = FORM2;
        @Override public ResourceLocation id() { return id; }
        @Override public String nameKey() { return "skill.slashing_alphad.sun_form_2"; }
        @Override public int cooldownTicks() { return 14; }

        @Override
        public void cast(ServerPlayer p, ItemStack stack, InteractionHand hand) {
            Vec3 origin = p.getEyePosition();
            Vec3 forward = p.getLookAngle().normalize();
            Vec3 center = origin.add(forward.scale(1.15));

            FxTask a = new ArcFanFxTask(p.serverLevel(), center, forward,
                    new double[]{1.05, 1.25, 1.45},
                    6, Math.toRadians(165), 28,
                    true, false);

            FxTask b = new ArcFanFxTask(p.serverLevel(), center, forward,
                    new double[]{1.00, 1.22, 1.44},
                    6, Math.toRadians(165), 28,
                    true, false);

            FxTask c = new ArcFanFxTask(p.serverLevel(), center, forward,
                    new double[]{0.95, 1.18, 1.40},
                    6, Math.toRadians(165), 28,
                    true, true);

            FxManager.schedule(a);
            FxManager.schedule(new DelayedFxTask(3, b));
            FxManager.schedule(new DelayedFxTask(6, c));

            soundSweep(p);
            shake(p, 2.6f, 12, 1.05f);
        }
    }

    /** Form III: spiral */
    private static final class Form3 implements ISkill {
        private final ResourceLocation id = FORM3;
        @Override public ResourceLocation id() { return id; }
        @Override public String nameKey() { return "skill.slashing_alphad.sun_form_3"; }
        @Override public int cooldownTicks() { return 16; }

        @Override
        public void cast(ServerPlayer p, ItemStack stack, InteractionHand hand) {
            Vec3 origin = p.getEyePosition().add(0, -0.15, 0);
            Vec3 forward = p.getLookAngle().normalize();

            FxManager.schedule(new SpiralFxTask(p.serverLevel(), origin, forward, 14, 38));

            p.serverLevel().playSound(null, p.blockPosition(),
                    SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS,
                    0.9f, 1.25f);

            shake(p, 2.2f, 14, 0.85f);
        }
    }

    /** Form IV: dash trail + finishing arc */
    private static final class Form4 implements ISkill {
        private final ResourceLocation id = FORM4;
        @Override public ResourceLocation id() { return id; }
        @Override public String nameKey() { return "skill.slashing_alphad.sun_form_4"; }
        @Override public int cooldownTicks() { return 18; }

        @Override
        public void cast(ServerPlayer p, ItemStack stack, InteractionHand hand) {
            Vec3 origin = p.getEyePosition();
            Vec3 forward = p.getLookAngle().normalize();

            FxManager.schedule(new DashTrailFxTask(p.serverLevel(), origin, forward, 10));

            Vec3 center = origin.add(forward.scale(2.4));
            FxManager.schedule(new DelayedFxTask(8, new ArcFanFxTask(
                    p.serverLevel(), center, forward,
                    new double[]{1.25, 1.50, 1.75},
                    7, Math.toRadians(150), 30,
                    true, true
            )));

            soundSweep(p);
            shake(p, 3.0f, 16, 0.95f);
        }
    }

    /** Form V: radiant wheel */
    private static final class Form5 implements ISkill {
        private final ResourceLocation id = FORM5;
        @Override public ResourceLocation id() { return id; }
        @Override public String nameKey() { return "skill.slashing_alphad.sun_form_5"; }
        @Override public int cooldownTicks() { return 18; }

        @Override
        public void cast(ServerPlayer p, ItemStack stack, InteractionHand hand) {
            Vec3 origin = p.getEyePosition();
            Vec3 forward = p.getLookAngle().normalize();
            Vec3 center = origin.add(forward.scale(0.95)).add(0, -0.1, 0);

            FxManager.schedule(new ArcFanFxTask(
                    p.serverLevel(), center, forward,
                    new double[]{1.35, 1.60, 1.85},
                    10, Math.toRadians(210), 36,
                    true, true
            ));

            p.serverLevel().playSound(null, p.blockPosition(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS,
                    0.8f, 0.95f);

            shake(p, 3.4f, 18, 0.80f);
        }
    }

    /** Form VI: solar dragon */
    private static final class Form6 implements ISkill {
        private final ResourceLocation id = FORM6;
        @Override public ResourceLocation id() { return id; }
        @Override public String nameKey() { return "skill.slashing_alphad.sun_form_6"; }
        @Override public int cooldownTicks() { return 22; }

        @Override
        public void cast(ServerPlayer p, ItemStack stack, InteractionHand hand) {
            Vec3 origin = p.getEyePosition().add(0, -0.2, 0);
            Vec3 forward = p.getLookAngle().normalize();

            FxManager.schedule(new DragonHelixFxTask(p.serverLevel(), origin, forward, 16));

            p.serverLevel().playSound(null, p.blockPosition(),
                    SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS,
                    0.7f, 1.15f);

            shake(p, 3.8f, 20, 0.75f);
        }
    }

    /** Form VII: sunrise burst */
    private static final class Form7 implements ISkill {
        private final ResourceLocation id = FORM7;
        @Override public ResourceLocation id() { return id; }
        @Override public String nameKey() { return "skill.slashing_alphad.sun_form_7"; }
        @Override public int cooldownTicks() { return 26; }

        @Override
        public void cast(ServerPlayer p, ItemStack stack, InteractionHand hand) {
            Vec3 center = p.position().add(0, 0.1, 0);

            FxManager.schedule(new RadialBurstFxTask(p.serverLevel(), center, 8));

            p.serverLevel().playSound(null, p.blockPosition(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS,
                    0.8f, 1.25f);

            shake(p, 4.5f, 22, 0.70f);
        }
    }

    /** Form VIII: Rising Crescent (hop + vertical slash) */
    private static final class Form8 implements ISkill {
        private final ResourceLocation id = FORM8;
        @Override public ResourceLocation id() { return id; }
        @Override public String nameKey() { return "skill.slashing_alphad.sun_form_8"; }
        @Override public int cooldownTicks() { return 18; }

        @Override
        public void cast(ServerPlayer p, ItemStack stack, InteractionHand hand) {
            FxManager.schedule(new RisingCrescentFxTask(p, 10, 7.5f, 0.8f, 0.75));
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 0.9f, 1.15f);
            soundSweep(p);
            shake(p, 3.2f, 16, 0.85f);
        }
    }

    /** Form IX: Flash Dash (3-step blink + trail) */
    private static final class Form9 implements ISkill {
        private final ResourceLocation id = FORM9;
        @Override public ResourceLocation id() { return id; }
        @Override public String nameKey() { return "skill.slashing_alphad.sun_form_9"; }
        @Override public int cooldownTicks() { return 20; }

        @Override
        public void cast(ServerPlayer p, ItemStack stack, InteractionHand hand) {
            FxManager.schedule(new FlashDashFxTask(p, 12, 8.0f, 0.75f, 0.75));
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.2f);
            shake(p, 3.8f, 18, 0.95f);
        }
    }

    /** Form X: Sky Cross Slash (air X) */
    private static final class Form10 implements ISkill {
        private final ResourceLocation id = FORM10;
        @Override public ResourceLocation id() { return id; }
        @Override public String nameKey() { return "skill.slashing_alphad.sun_form_10"; }
        @Override public int cooldownTicks() { return 22; }

        @Override
        public void cast(ServerPlayer p, ItemStack stack, InteractionHand hand) {
            FxManager.schedule(new SkyCrossFxTask(p, 14, 9.0f, 0.9f, 0.80));
            soundSweep(p);
            shake(p, 4.0f, 20, 0.80f);
        }
    }

    /** Form XI: Sunfall Crash (slam + shockwave ring) */
    private static final class Form11 implements ISkill {
        private final ResourceLocation id = FORM11;
        @Override public ResourceLocation id() { return id; }
        @Override public String nameKey() { return "skill.slashing_alphad.sun_form_11"; }
        @Override public int cooldownTicks() { return 30; }

        @Override
        public void cast(ServerPlayer p, ItemStack stack, InteractionHand hand) {
            FxManager.schedule(new SunfallCrashFxTask(p, 18, 12.0f, 1.15f));
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.8f, 1.15f);
            shake(p, 5.0f, 26, 0.70f);
        }
    }

}
