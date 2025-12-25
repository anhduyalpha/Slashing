package com.main.slashing.rpg;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public sealed interface RpgActionDef permits RpgActionDef.Wait, RpgActionDef.PlaySound, RpgActionDef.SpawnParticles,
        RpgActionDef.Damage, RpgActionDef.ApplyForce, RpgActionDef.ApplyStatus {
    Logger LOGGER = LogManager.getLogger(RpgActionDef.class);

    String type();

    static RpgActionDef parse(Path path, String type, JsonObject obj) {
        return switch (type) {
            case "wait" -> {
                Long ms = getLong(path, obj, "ms");
                yield ms == null ? null : new Wait(ms);
            }
            case "play_sound" -> {
                String sound = getString(path, obj, "sound");
                Double volume = getDouble(path, obj, "volume");
                Double pitch = getDouble(path, obj, "pitch");
                if (sound == null || volume == null || pitch == null) yield null;
                yield new PlaySound(sound, volume.floatValue(), pitch.floatValue());
            }
            case "spawn_particles" -> {
                String particle = getString(path, obj, "particle");
                Double spread = getDouble(path, obj, "spread");
                Double speed = getDouble(path, obj, "speed");
                Integer count = getInt(path, obj, "count");
                if (particle == null || spread == null || speed == null || count == null) yield null;
                yield new SpawnParticles(particle, count, spread, speed);
            }
            case "damage" -> {
                String amount = getString(path, obj, "amount");
                String dmgType = getString(path, obj, "type");
                Double radius = getDoubleOptional(obj, "radius");
                if (amount == null || dmgType == null) yield null;
                yield new Damage(amount, dmgType, radius == null ? 0 : radius);
            }
            case "apply_force" -> {
                String mode = getString(path, obj, "mode");
                Double strength = getDouble(path, obj, "strength");
                if (mode == null || strength == null) yield null;
                yield new ApplyForce(mode, strength);
            }
            case "apply_status" -> {
                String status = getString(path, obj, "status");
                Long duration = getLong(path, obj, "duration_ms");
                if (status == null || duration == null) yield null;
                yield new ApplyStatus(status, duration);
            }
            default -> {
                LOGGER.error("Unknown action '{}' in {}", type, path.toAbsolutePath());
                yield null;
            }
        };
    }

    static String getString(Path path, JsonObject obj, String field) {
        if (!obj.has(field)) {
            LOGGER.error("Missing required field '{}' in {}", field, path.toAbsolutePath());
            return null;
        }
        if (!obj.get(field).isJsonPrimitive()) {
            LOGGER.error("Invalid field '{}' (not primitive) in {}", field, path.toAbsolutePath());
            return null;
        }
        return obj.get(field).getAsString();
    }

    static Double getDouble(Path path, JsonObject obj, String field) {
        if (!obj.has(field)) {
            LOGGER.error("Missing required field '{}' in {}", field, path.toAbsolutePath());
            return null;
        }
        if (!obj.get(field).isJsonPrimitive() || !obj.get(field).getAsJsonPrimitive().isNumber()) {
            LOGGER.error("Invalid field '{}' (not number) in {}", field, path.toAbsolutePath());
            return null;
        }
        return obj.get(field).getAsDouble();
    }

    static Double getDoubleOptional(JsonObject obj, String field) {
        if (!obj.has(field)) return null;
        if (!obj.get(field).isJsonPrimitive() || !obj.get(field).getAsJsonPrimitive().isNumber()) return null;
        return obj.get(field).getAsDouble();
    }

    static Long getLong(Path path, JsonObject obj, String field) {
        if (!obj.has(field)) {
            LOGGER.error("Missing required field '{}' in {}", field, path.toAbsolutePath());
            return null;
        }
        if (!obj.get(field).isJsonPrimitive() || !obj.get(field).getAsJsonPrimitive().isNumber()) {
            LOGGER.error("Invalid field '{}' (not number) in {}", field, path.toAbsolutePath());
            return null;
        }
        return obj.get(field).getAsLong();
    }

    static Integer getInt(Path path, JsonObject obj, String field) {
        if (!obj.has(field)) {
            LOGGER.error("Missing required field '{}' in {}", field, path.toAbsolutePath());
            return null;
        }
        if (!obj.get(field).isJsonPrimitive() || !obj.get(field).getAsJsonPrimitive().isNumber()) {
            LOGGER.error("Invalid field '{}' (not number) in {}", field, path.toAbsolutePath());
            return null;
        }
        return obj.get(field).getAsInt();
    }

    record Wait(long ms) implements RpgActionDef {
        @Override public String type() { return "wait"; }
    }

    record PlaySound(String sound, float volume, float pitch) implements RpgActionDef {
        @Override public String type() { return "play_sound"; }
    }

    record SpawnParticles(String particle, int count, double spread, double speed) implements RpgActionDef {
        @Override public String type() { return "spawn_particles"; }
    }

    record Damage(String amount, String dmgType, double radius) implements RpgActionDef {
        @Override public String type() { return "damage"; }
    }

    record ApplyForce(String mode, double strength) implements RpgActionDef {
        @Override public String type() { return "apply_force"; }
    }

    record ApplyStatus(String status, long durationMs) implements RpgActionDef {
        @Override public String type() { return "apply_status"; }
    }
}
