package com.main.slashing.mmo;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.ForgeRegistries;

/** Thông số sound phát khi cast/hit. */
public record SoundSpec(ResourceLocation eventId, SoundSource source, float volume, float pitch, float pitchRandom) {

    public static SoundSpec none() {
        return new SoundSpec(null, SoundSource.PLAYERS, 1.0f, 1.0f, 0.0f);
    }

    public void play(ServerLevel level, BlockPos pos) {
        if (level == null || eventId == null) return;
        SoundEvent ev = ForgeRegistries.SOUND_EVENTS.getValue(eventId);
        if (ev == null) return;

        float p = pitch;
        if (pitchRandom > 0f) {
            float rnd = (level.getRandom().nextFloat() - 0.5f) * 2f; // [-1..1]
            p += rnd * pitchRandom;
        }
        level.playSound(null, pos, ev, source, volume, p);
    }
}
