package com.main.slashing.rpg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RpgDataManager {
    public static final String PACKS_DIR = "slashing_alphad/packs";
    private static final Logger LOGGER = LogManager.getLogger(RpgDataManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, RpgClassDef> CLASSES = new LinkedHashMap<>();
    private static final Map<String, RpgSkillDef> SKILLS = new LinkedHashMap<>();
    private static final Map<String, RpgStatusDef> STATUSES = new LinkedHashMap<>();

    private RpgDataManager() {}

    public static Map<String, RpgClassDef> getClasses() {
        return CLASSES;
    }

    public static Map<String, RpgSkillDef> getSkills() {
        return SKILLS;
    }

    public static Map<String, RpgStatusDef> getStatuses() {
        return STATUSES;
    }

    public static void ensureBasePackExists() {
        Path baseDir = getPacksRoot().resolve("base");
        Path classesDir = baseDir.resolve("classes");
        Path skillsDir = baseDir.resolve("skills");
        Path statusesDir = baseDir.resolve("statuses");
        try {
            if (!Files.exists(baseDir)) {
                Files.createDirectories(classesDir);
                Files.createDirectories(skillsDir);
                Files.createDirectories(statusesDir);
                writeSampleFiles(classesDir, skillsDir, statusesDir);
                LOGGER.info("Created base RPG pack at {}", baseDir.toAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create base RPG pack at {}", baseDir.toAbsolutePath(), e);
        }
    }

    public static void loadAll() {
        ensureBasePackExists();
        Map<String, RpgClassDef> newClasses = new LinkedHashMap<>();
        Map<String, RpgSkillDef> newSkills = new LinkedHashMap<>();
        Map<String, RpgStatusDef> newStatuses = new LinkedHashMap<>();

        Path root = getPacksRoot();
        if (!Files.exists(root)) {
            LOGGER.warn("RPG packs directory missing: {}", root.toAbsolutePath());
            CLASSES.clear();
            SKILLS.clear();
            STATUSES.clear();
            return;
        }

        try (var stream = Files.list(root)) {
            stream.filter(Files::isDirectory)
                .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                .forEach(pack -> {
                    loadTypeFromPack(pack, "classes", newClasses, RpgDataManager::parseClass);
                    loadTypeFromPack(pack, "skills", newSkills, RpgDataManager::parseSkill);
                    loadTypeFromPack(pack, "statuses", newStatuses, RpgDataManager::parseStatus);
                });
        } catch (IOException e) {
            LOGGER.error("Failed to read RPG packs from {}", root.toAbsolutePath(), e);
            return;
        }

        CLASSES.clear();
        CLASSES.putAll(newClasses);
        SKILLS.clear();
        SKILLS.putAll(newSkills);
        STATUSES.clear();
        STATUSES.putAll(newStatuses);

        LOGGER.info("Loaded RPG data: {} classes, {} skills, {} statuses", CLASSES.size(), SKILLS.size(), STATUSES.size());
    }

    private static Path getPacksRoot() {
        return FMLPaths.CONFIGDIR.get().resolve(PACKS_DIR);
    }

    private static <T> void loadTypeFromPack(Path packDir, String subDir, Map<String, T> out,
                                             Parser<T> parser) {
        Path dir = packDir.resolve(subDir);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) return;
        try {
            Files.list(dir)
                .filter(path -> path.toString().endsWith(".json"))
                .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                .forEach(path -> {
                    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                        JsonElement elem = JsonParser.parseReader(reader);
                        if (!elem.isJsonObject()) {
                            LOGGER.error("Invalid JSON (not object) at {}", path.toAbsolutePath());
                            return;
                        }
                        T parsed = parser.parse(path, elem.getAsJsonObject());
                        if (parsed != null) {
                            out.put(parser.idOf(parsed), parsed);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to read JSON at {}", path.toAbsolutePath(), e);
                    }
                });
        } catch (IOException e) {
            LOGGER.error("Failed to scan {} in pack {}", subDir, packDir.toAbsolutePath(), e);
        }
    }

    private static RpgClassDef parseClass(Path path, JsonObject root) {
        String id = getString(path, root, "id");
        if (id == null) return null;

        JsonObject baseStatsObj = getObject(path, root, "base_stats");
        Map<String, Integer> baseStats = new HashMap<>();
        if (baseStatsObj != null) {
            for (var entry : baseStatsObj.entrySet()) {
                if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber()) {
                    baseStats.put(entry.getKey(), entry.getValue().getAsInt());
                } else {
                    LOGGER.error("Invalid base_stats value for '{}' in {}", entry.getKey(), path.toAbsolutePath());
                }
            }
        }

        Map<String, RpgResourceDef> resources = new HashMap<>();
        JsonObject resObj = getObject(path, root, "resources");
        if (resObj != null) {
            for (var entry : resObj.entrySet()) {
                if (!entry.getValue().isJsonObject()) {
                    LOGGER.error("Invalid resource object for '{}' in {}", entry.getKey(), path.toAbsolutePath());
                    continue;
                }
                JsonObject res = entry.getValue().getAsJsonObject();
                Double max = getDouble(path, res, "max");
                Double regen = getDouble(path, res, "regen_per_sec");
                Double gain = getDouble(path, res, "gain_on_cast");
                if (max == null || regen == null || gain == null) continue;
                resources.put(entry.getKey(), new RpgResourceDef(max, regen, gain));
            }
        }

        return new RpgClassDef(id, baseStats, resources);
    }

    private static RpgStatusDef parseStatus(Path path, JsonObject root) {
        String id = getString(path, root, "id");
        if (id == null) return null;
        Long duration = getLong(path, root, "duration_ms");
        if (duration == null) return null;

        Map<String, Boolean> flags = new HashMap<>();
        JsonObject flagsObj = getObject(path, root, "flags");
        if (flagsObj != null) {
            for (var entry : flagsObj.entrySet()) {
                if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isBoolean()) {
                    flags.put(entry.getKey(), entry.getValue().getAsBoolean());
                } else {
                    LOGGER.error("Invalid flag value for '{}' in {}", entry.getKey(), path.toAbsolutePath());
                }
            }
        }
        return new RpgStatusDef(id, duration, flags);
    }

    private static RpgSkillDef parseSkill(Path path, JsonObject root) {
        String id = getString(path, root, "id");
        if (id == null) return null;

        JsonObject castObj = getObject(path, root, "cast");
        if (castObj == null) return null;
        Long cooldownMs = getLong(path, castObj, "cooldown_ms");
        Long timeMs = getLong(path, castObj, "time_ms");
        if (cooldownMs == null || timeMs == null) return null;

        Map<String, Double> cost = new HashMap<>();
        JsonObject costObj = getObject(path, castObj, "cost");
        if (costObj != null) {
            for (var entry : costObj.entrySet()) {
                if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber()) {
                    cost.put(entry.getKey(), entry.getValue().getAsDouble());
                } else {
                    LOGGER.error("Invalid cost value for '{}' in {}", entry.getKey(), path.toAbsolutePath());
                }
            }
        }

        RpgCastDef cast = new RpgCastDef(cooldownMs, timeMs, cost);

        JsonObject targetingObj = getObject(path, root, "targeting");
        if (targetingObj == null) return null;
        String type = getString(path, targetingObj, "type");
        if (type == null) return null;
        Double range = getDouble(path, targetingObj, "range");
        String fallback = getStringOptional(targetingObj, "fallback");
        if (range == null) return null;
        RpgTargetingDef targeting = new RpgTargetingDef(type, range, fallback);

        if (!root.has("pipeline") || !root.get("pipeline").isJsonArray()) {
            LOGGER.error("Missing or invalid 'pipeline' array in {}", path.toAbsolutePath());
            return null;
        }
        var pipelineArr = root.getAsJsonArray("pipeline");

        var actions = new java.util.ArrayList<RpgActionDef>();
        for (JsonElement elem : pipelineArr) {
            if (!elem.isJsonObject()) {
                LOGGER.error("Invalid action (not object) in {}", path.toAbsolutePath());
                continue;
            }
            JsonObject obj = elem.getAsJsonObject();
            String actionType = getString(path, obj, "do");
            if (actionType == null) continue;
            RpgActionDef action = RpgActionDef.parse(path, actionType, obj);
            if (action != null) actions.add(action);
        }

        return new RpgSkillDef(id, cast, targeting, actions);
    }

    private static String getString(Path path, JsonObject obj, String field) {
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

    private static String getStringOptional(JsonObject obj, String field) {
        if (!obj.has(field)) return null;
        if (!obj.get(field).isJsonPrimitive()) return null;
        return obj.get(field).getAsString();
    }

    private static JsonObject getObject(Path path, JsonObject obj, String field) {
        if (!obj.has(field)) {
            LOGGER.error("Missing required field '{}' in {}", field, path.toAbsolutePath());
            return null;
        }
        if (!obj.get(field).isJsonObject()) {
            LOGGER.error("Invalid field '{}' (not object) in {}", field, path.toAbsolutePath());
            return null;
        }
        return obj.getAsJsonObject(field);
    }

    private static Long getLong(Path path, JsonObject obj, String field) {
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

    private static Double getDouble(Path path, JsonObject obj, String field) {
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

    private static void writeSampleFiles(Path classesDir, Path skillsDir, Path statusesDir) throws IOException {
        JsonObject classJson = new JsonObject();
        classJson.addProperty("id", "reaper");
        JsonObject baseStats = new JsonObject();
        baseStats.addProperty("str", 5);
        baseStats.addProperty("agi", 8);
        baseStats.addProperty("vit", 4);
        classJson.add("base_stats", baseStats);
        JsonObject resources = new JsonObject();
        JsonObject soul = new JsonObject();
        soul.addProperty("max", 7);
        soul.addProperty("regen_per_sec", 0);
        soul.addProperty("gain_on_cast", 1);
        resources.add("soul", soul);
        classJson.add("resources", resources);

        JsonObject statusJson = new JsonObject();
        statusJson.addProperty("id", "stun");
        statusJson.addProperty("duration_ms", 400);
        JsonObject flags = new JsonObject();
        flags.addProperty("disable_move", true);
        statusJson.add("flags", flags);

        JsonObject skillJson = new JsonObject();
        skillJson.addProperty("id", "reaper:reaping_claw");
        JsonObject cast = new JsonObject();
        cast.addProperty("cooldown_ms", 8000);
        cast.addProperty("time_ms", 150);
        JsonObject cost = new JsonObject();
        cost.addProperty("soul", 0);
        cast.add("cost", cost);
        skillJson.add("cast", cast);
        JsonObject targeting = new JsonObject();
        targeting.addProperty("type", "look_entity");
        targeting.addProperty("range", 10);
        targeting.addProperty("fallback", "self");
        skillJson.add("targeting", targeting);

        var pipeline = new com.google.gson.JsonArray();
        JsonObject action1 = new JsonObject();
        action1.addProperty("do", "play_sound");
        action1.addProperty("sound", "minecraft:entity.wither.shoot");
        action1.addProperty("volume", 1);
        action1.addProperty("pitch", 1);
        pipeline.add(action1);

        JsonObject action2 = new JsonObject();
        action2.addProperty("do", "spawn_particles");
        action2.addProperty("particle", "minecraft:soul");
        action2.addProperty("count", 25);
        action2.addProperty("spread", 0.4);
        action2.addProperty("speed", 0);
        pipeline.add(action2);

        JsonObject action3 = new JsonObject();
        action3.addProperty("do", "apply_force");
        action3.addProperty("mode", "pull_to_caster");
        action3.addProperty("strength", 1.1);
        pipeline.add(action3);

        JsonObject action4 = new JsonObject();
        action4.addProperty("do", "damage");
        action4.addProperty("amount", "8 + 0.7*STAT.agi");
        action4.addProperty("type", "physical");
        pipeline.add(action4);

        JsonObject action5 = new JsonObject();
        action5.addProperty("do", "apply_status");
        action5.addProperty("status", "stun");
        action5.addProperty("duration_ms", 300);
        pipeline.add(action5);

        skillJson.add("pipeline", pipeline);

        Files.writeString(classesDir.resolve("reaper.json"), GSON.toJson(classJson), StandardCharsets.UTF_8);
        Files.writeString(statusesDir.resolve("stun.json"), GSON.toJson(statusJson), StandardCharsets.UTF_8);
        Files.writeString(skillsDir.resolve("reaping_claw.json"), GSON.toJson(skillJson), StandardCharsets.UTF_8);
    }

    private interface Parser<T> {
        T parse(Path path, JsonObject obj);
        default String idOf(T obj) {
            if (obj instanceof RpgClassDef def) return def.id();
            if (obj instanceof RpgSkillDef def) return def.id();
            if (obj instanceof RpgStatusDef def) return def.id();
            return null;
        }
    }
}
