package com.main.slashing.rpg;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RpgProfileManager {
    private static final Map<UUID, PlayerProfile> PROFILES = new ConcurrentHashMap<>();

    private RpgProfileManager() {}

    public static PlayerProfile get(ServerPlayer player) {
        return PROFILES.computeIfAbsent(player.getUUID(), id -> new PlayerProfile());
    }

    public static void remove(ServerPlayer player) {
        PROFILES.remove(player.getUUID());
    }

    public static void applyClass(ServerPlayer player, RpgClassDef def) {
        PlayerProfile profile = get(player);
        profile.setClassId(def.id());
        profile.stats().clear();
        profile.stats().putAll(def.baseStats());
        profile.resources().clear();
        def.resources().forEach((key, res) -> {
            profile.resources().put(key, new ResourceState(res.max(), res.regenPerSec(), res.gainOnCast(), res.max()));
        });
    }
}
