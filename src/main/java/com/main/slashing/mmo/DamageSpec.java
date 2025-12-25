package com.main.slashing.mmo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Thông số damage/on-hit cho skill. */
public record DamageSpec(
        float amount,
        float knockback,
        int setOnFireSeconds,
        List<MobEffectInstance> effects,
        SkillElement element
) {
    public static DamageSpec none() {
        return new DamageSpec(0f, 0f, 0, List.of(), SkillElement.PHYSICAL);
    }

    public void apply(ServerPlayer caster, LivingEntity target) {
        if (caster == null || target == null) return;
        ServerLevel level = caster.serverLevel();

        if (amount > 0f) {
            DamageSource src = level.damageSources().playerAttack(caster);
            target.hurt(src, amount);
        }

        if (knockback > 0f) {
            Vec3 fwd = caster.getLookAngle().normalize();
            target.knockback(knockback, -fwd.x, -fwd.z);
        }

        if (setOnFireSeconds > 0) {
            target.setSecondsOnFire(setOnFireSeconds);
        }

        if (effects != null) {
            for (MobEffectInstance inst : effects) {
                if (inst != null) target.addEffect(new MobEffectInstance(inst));
            }
        }
    }
}
