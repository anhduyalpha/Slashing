package com.main.slashing.skill;

import com.main.slashing.fx.FxManager;
import com.main.slashing.fx.SlashSweepFxTask;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class SlashSweepSkill implements ISkill {

    public static final ResourceLocation ID = new ResourceLocation("slashing_alphad", "slash_sweep");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String nameKey() {
        return "skill.slashing_alphad.slash_sweep";
    }

    @Override
    public int cooldownTicks() {
        return 10;
    }

    @Override
    public void cast(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        Vec3 origin = player.getEyePosition();
        Vec3 forward = player.getLookAngle().normalize();
        Vec3 center = origin.add(forward.scale(1.25));

        // duration 7 ticks, dmg 6, kb 0.6, hit radius 0.65
        FxManager.schedule(new SlashSweepFxTask(
                player,
                center,
                forward,
                7,
                6.0f,
                0.6f,
                0.65,
                true
        ));

        player.serverLevel().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                1.0f, 1.0f);
    }
}
