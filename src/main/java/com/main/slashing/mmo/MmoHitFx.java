package com.main.slashing.mmo;

import com.main.slashing.net.ModNet;
import com.main.slashing.net.S2C_ScreenFlashPacket;
import com.main.slashing.net.S2C_ScreenShakePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** FX helper cho on-hit. */
public final class MmoHitFx {
    private MmoHitFx() {}

    public static void onHit(ServerPlayer caster, LivingEntity target, SkillElement element, HitFxSpec spec) {
        if (caster == null || target == null) return;
        ServerLevel level = caster.serverLevel();

        // ===== particles quanh target (giống "hit spark") =====
        Vec3 p = target.position().add(0, target.getBbHeight() * 0.55, 0);
        int count = Math.max(0, spec.extraParticles());
        if (count > 0) {
            level.sendParticles(element.accentParticle(), p.x, p.y, p.z,
                    count,
                    0.15, 0.20, 0.15,
                    0.02);
        }
        // thêm 1 chút dust (đẹp hơn với shader)
        level.sendParticles(element.dust(), p.x, p.y, p.z,
                Math.min(8, Math.max(0, count / 2 + 2)),
                0.10, 0.12, 0.10,
                0.01);

        // ===== sound hit =====
        if (spec.hitSound() != null) {
            spec.hitSound().play(level, target.blockPosition());
        }

        // ===== hit confirm: shake + flash chỉ cho caster =====
        if (spec.hitScreenShakeIntensity() > 0.001f) {
            ModNet.sendToPlayer(caster, new S2C_ScreenShakePacket(
                    spec.hitScreenShakeIntensity(),
                    spec.hitScreenShakeTicks(),
                    spec.hitScreenShakeFreq()
            ));
        }
        if (spec.hitFlashAlpha() > 0.001f) {
            ModNet.sendToPlayer(caster, new S2C_ScreenFlashPacket(
                    spec.hitFlashRgb(),
                    spec.hitFlashAlpha(),
                    spec.hitFlashTicks()
            ));
        }
    }
}
