package com.main.slashing.mmo.skill;

import com.google.gson.*;
import com.main.slashing.mmo.*;
import com.main.slashing.mmo.skill.actions.*;
import com.main.slashing.skill.SkillRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.sounds.SoundSource;

import java.util.*;

/**
 * Load skill definitions from datapack JSON:
 * data/<modid>/skills/*.json
 */
public final class MmoSkillReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setLenient().create();

    public MmoSkillReloadListener() {
        super(GSON, "skills");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, net.minecraft.server.packs.resources.ResourceManager rm, ProfilerFiller profiler) {
        SkillRegistry.clearData();

        for (Map.Entry<ResourceLocation, JsonElement> e : objects.entrySet()) {
            ResourceLocation id = e.getKey();
            if (!e.getValue().isJsonObject()) continue;
            JsonObject root = e.getValue().getAsJsonObject();

            try {
                DataDrivenSkill skill = parseSkill(id, root);
                if (skill != null) SkillRegistry.registerData(skill);
            } catch (Throwable ex) {
                // ignore bad file to avoid crash on /reload
            }
        }
    }

    private static DataDrivenSkill parseSkill(ResourceLocation id, JsonObject root) {
        String nameKey = getStr(root, "name_key", "skill." + id.getNamespace() + "." + id.getPath());
        int cd = getInt(root, "cooldown_ticks", 10);
        SkillElement element = SkillElement.fromString(getStr(root, "element", "physical"), SkillElement.PHYSICAL);

        SoundSpec castSound = parseSound(root.has("cast_sound") && root.get("cast_sound").isJsonObject() ? root.getAsJsonObject("cast_sound") : null);
        ScreenShakeSpec shake = parseShake(root.has("camera_shake") && root.get("camera_shake").isJsonObject() ? root.getAsJsonObject("camera_shake") : null);
        ScreenFlashSpec flash = parseFlash(root.has("screen_flash") && root.get("screen_flash").isJsonObject() ? root.getAsJsonObject("screen_flash") : null);

        List<MmoAction> actions = new ArrayList<>();
        if (root.has("actions") && root.get("actions").isJsonArray()) {
            for (JsonElement el : root.getAsJsonArray("actions")) {
                if (!el.isJsonObject()) continue;
                MmoAction a = parseAction(el.getAsJsonObject(), element);
                if (a != null) actions.add(a);
            }
        }

        return new DataDrivenSkill(id, nameKey, cd, element, castSound, shake, flash, List.copyOf(actions));
    }

    private static MmoAction parseAction(JsonObject obj, SkillElement skillElement) {
        String type = getStr(obj, "type", "").toLowerCase(Locale.ROOT);
        int delay = getInt(obj, "delay", 0);

        return switch (type) {
            case "arc_sweep" -> {
                int dur = getInt(obj, "duration", 7);
                double centerF = getD(obj, "center_forward", 1.25);
                double centerY = getD(obj, "center_y", -0.05);
                double arc = getD(obj, "arc_deg", 155);
                double sweep = getD(obj, "sweep_deg", 42);
                double[] radii = getDArr(obj, "radii", new double[]{1.05, 1.28, 1.52});
                int steps = getInt(obj, "steps", 26);
                int stride = getInt(obj, "sample_stride", 2);
                boolean follow = getBool(obj, "follow_caster", false);
                boolean sparks = getBool(obj, "add_sparks", true);

                JsonObject hitObj = obj.has("hit") && obj.get("hit").isJsonObject() ? obj.getAsJsonObject("hit") : new JsonObject();
                double hitRadius = getD(hitObj, "radius", 0.65);
                float dmg = (float) getD(hitObj, "damage", 6.0);
                float kb = (float) getD(hitObj, "knockback", 0.6);
                int fireSec = getInt(hitObj, "fire_seconds", 0);
                List<MobEffectInstance> effects = parseEffects(hitObj);
                DamageSpec dmgSpec = new DamageSpec(dmg, kb, fireSec, effects, skillElement);

                HitFxSpec hitFx = parseHitFx(obj.has("hit_fx") && obj.get("hit_fx").isJsonObject() ? obj.getAsJsonObject("hit_fx") : null);

                yield new ArcSweepAction(delay, dur, centerF, centerY, arc, sweep, radii, steps, stride, hitRadius, dmgSpec, hitFx, skillElement, follow, sparks);
            }
            case "arc_fan" -> {
                int dur = getInt(obj, "duration", 7);
                double centerF = getD(obj, "center_forward", 1.25);
                double centerY = getD(obj, "center_y", -0.05);
                double arc = getD(obj, "arc_deg", 165);
                double[] radii = getDArr(obj, "radii", new double[]{1.10, 1.32, 1.54});
                int steps = getInt(obj, "steps", 28);
                boolean sparks = getBool(obj, "add_sparks", true);
                boolean hot = getBool(obj, "hot", false);
                yield new ArcFanAction(delay, dur, centerF, centerY, arc, radii, steps, sparks, hot);
            }
            case "spiral" -> {
                int dur = getInt(obj, "duration", 14);
                int ppt = getInt(obj, "points_per_tick", 38);
                double off = getD(obj, "origin_forward", 0.0);
                double oy = getD(obj, "origin_y", -0.15);
                yield new SpiralAction(delay, dur, ppt, off, oy);
            }
            case "radial_burst" -> {
                int dur = getInt(obj, "duration", 8);
                double cy = getD(obj, "center_y", 0.10);
                yield new RadialBurstAction(delay, dur, cy);
            }
            default -> null;
        };
    }

    private static SoundSpec parseSound(JsonObject obj) {
        if (obj == null) return SoundSpec.none();
        try {
            ResourceLocation ev = new ResourceLocation(getStr(obj, "event", ""));
            SoundSource src = SoundSource.valueOf(getStr(obj, "source", "PLAYERS").toUpperCase(Locale.ROOT));
            float vol = (float) getD(obj, "volume", 1.0);
            float pit = (float) getD(obj, "pitch", 1.0);
            float pr = (float) getD(obj, "pitch_random", 0.0);
            return new SoundSpec(ev, src, vol, pit, pr);
        } catch (Exception ignored) {
            return SoundSpec.none();
        }
    }

    private static ScreenShakeSpec parseShake(JsonObject obj) {
        if (obj == null) return ScreenShakeSpec.none();
        float intensity = (float) getD(obj, "intensity", 0.0);
        int ticks = getInt(obj, "duration", 12);
        float freq = (float) getD(obj, "frequency", 0.9);
        return new ScreenShakeSpec(intensity, ticks, freq);
    }

    private static ScreenFlashSpec parseFlash(JsonObject obj) {
        if (obj == null) return ScreenFlashSpec.none();
        int rgb = parseColor(obj.get("color"), 0xFFFFFF);
        float a = (float) getD(obj, "alpha", 0.0);
        int ticks = getInt(obj, "duration", 6);
        return new ScreenFlashSpec(rgb, a, ticks);
    }

    private static HitFxSpec parseHitFx(JsonObject obj) {
        if (obj == null) return HitFxSpec.none();
        int extra = getInt(obj, "extra_particles", 8);

        float shakeI = 0f;
        int shakeT = 1;
        float shakeF = 1f;
        if (obj.has("hit_shake") && obj.get("hit_shake").isJsonObject()) {
            JsonObject h = obj.getAsJsonObject("hit_shake");
            shakeI = (float) getD(h, "intensity", 0.7);
            shakeT = getInt(h, "duration", 4);
            shakeF = (float) getD(h, "frequency", 1.2);
        }

        int flashRgb = 0xFFFFFF;
        float flashA = 0f;
        int flashT = 1;
        if (obj.has("hit_flash") && obj.get("hit_flash").isJsonObject()) {
            JsonObject f = obj.getAsJsonObject("hit_flash");
            flashRgb = parseColor(f.get("color"), 0xFFFFFF);
            flashA = (float) getD(f, "alpha", 0.15);
            flashT = getInt(f, "duration", 3);
        }

        SoundSpec s = SoundSpec.none();
        if (obj.has("hit_sound") && obj.get("hit_sound").isJsonObject()) {
            s = parseSound(obj.getAsJsonObject("hit_sound"));
        }

        return new HitFxSpec(extra, shakeI, shakeT, shakeF, flashRgb, flashA, flashT, s);
    }

    private static List<MobEffectInstance> parseEffects(JsonObject hitObj) {
        if (hitObj == null || !hitObj.has("effects") || !hitObj.get("effects").isJsonArray()) return List.of();
        ArrayList<MobEffectInstance> out = new ArrayList<>();
        for (JsonElement e : hitObj.getAsJsonArray("effects")) {
            if (!e.isJsonObject()) continue;
            JsonObject o = e.getAsJsonObject();
            try {
                ResourceLocation id = new ResourceLocation(getStr(o, "id", ""));
                MobEffect eff = BuiltInRegistries.MOB_EFFECT.get(id);
                if (eff == null) continue;
                int dur = getInt(o, "duration", 40);
                int amp = getInt(o, "amplifier", 0);
                boolean showP = getBool(o, "show_particles", true);
                boolean showI = getBool(o, "show_icon", true);
                out.add(new MobEffectInstance(eff, dur, amp, false, showP, showI));
            } catch (Exception ignored) {}
        }
        return out;
    }

    // ===== helpers =====
    private static String getStr(JsonObject o, String k, String def) {
        if (o == null || !o.has(k) || !o.get(k).isJsonPrimitive()) return def;
        return o.get(k).getAsString();
    }

    private static int getInt(JsonObject o, String k, int def) {
        if (o == null || !o.has(k) || !o.get(k).isJsonPrimitive()) return def;
        try { return o.get(k).getAsInt(); } catch (Exception ignored) { return def; }
    }

    private static double getD(JsonObject o, String k, double def) {
        if (o == null || !o.has(k) || !o.get(k).isJsonPrimitive()) return def;
        try { return o.get(k).getAsDouble(); } catch (Exception ignored) { return def; }
    }

    private static boolean getBool(JsonObject o, String k, boolean def) {
        if (o == null || !o.has(k) || !o.get(k).isJsonPrimitive()) return def;
        try { return o.get(k).getAsBoolean(); } catch (Exception ignored) { return def; }
    }

    private static double[] getDArr(JsonObject o, String k, double[] def) {
        if (o == null || !o.has(k) || !o.get(k).isJsonArray()) return def;
        JsonArray arr = o.getAsJsonArray(k);
        if (arr.isEmpty()) return def;
        double[] out = new double[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            try { out[i] = arr.get(i).getAsDouble(); } catch (Exception ignored) { out[i] = def[Math.min(def.length - 1, i)]; }
        }
        return out;
    }

    private static int parseColor(JsonElement el, int def) {
        if (el == null) return def;
        try {
            if (el.isJsonPrimitive()) {
                String s = el.getAsString().trim();
                if (s.startsWith("#")) s = s.substring(1);
                if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
                if (s.matches("^[0-9a-fA-F]{6}$")) return Integer.parseInt(s, 16);
                // allow int
                return el.getAsInt();
            }
            if (el.isJsonArray()) {
                JsonArray a = el.getAsJsonArray();
                int r = a.size() > 0 ? a.get(0).getAsInt() : 255;
                int g = a.size() > 1 ? a.get(1).getAsInt() : 255;
                int b = a.size() > 2 ? a.get(2).getAsInt() : 255;
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));
                return (r << 16) | (g << 8) | b;
            }
        } catch (Exception ignored) {}
        return def;
    }
}
