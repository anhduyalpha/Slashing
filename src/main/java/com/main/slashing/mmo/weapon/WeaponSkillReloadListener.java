package com.main.slashing.mmo.weapon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;

import java.util.*;

/**
 * Load mapping item/tag -> skill list.
 * File: data/<modid>/weapon_skills/*.json
 */
public final class WeaponSkillReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setLenient().create();

    public WeaponSkillReloadListener() {
        super(GSON, "weapon_skills");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, net.minecraft.server.packs.resources.ResourceManager rm, ProfilerFiller profiler) {
        Map<Item, WeaponSkillSet> out = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> e : objects.entrySet()) {
            if (!e.getValue().isJsonObject()) continue;
            JsonObject root = e.getValue().getAsJsonObject();

            JsonObject match = root.has("match") && root.get("match").isJsonObject() ? root.getAsJsonObject("match") : new JsonObject();
            List<Item> matchedItems = new ArrayList<>();

            // match.items
            if (match.has("items") && match.get("items").isJsonArray()) {
                for (JsonElement it : match.getAsJsonArray("items")) {
                    if (!it.isJsonPrimitive()) continue;
                    try {
                        ResourceLocation id = new ResourceLocation(it.getAsString());
                        Item item = BuiltInRegistries.ITEM.get(id);
                        if (item != null && item != net.minecraft.world.item.Items.AIR) matchedItems.add(item);
                    } catch (Exception ignored) {}
                }
            }

            // match.tags
            if (match.has("tags") && match.get("tags").isJsonArray()) {
                for (JsonElement tg : match.getAsJsonArray("tags")) {
                    if (!tg.isJsonPrimitive()) continue;
                    try {
                        ResourceLocation tagId = new ResourceLocation(tg.getAsString());
                        TagKey<Item> key = TagKey.create(Registries.ITEM, tagId);
                        Optional<net.minecraft.core.HolderSet.Named<Item>> opt = BuiltInRegistries.ITEM.getTag(key);
                        if (opt.isEmpty()) continue;
                        for (Holder<Item> h : opt.get()) {
                            matchedItems.add(h.value());
                        }
                    } catch (Exception ignored) {}
                }
            }

            // skills list
            List<ResourceLocation> skills = new ArrayList<>();
            if (root.has("skills") && root.get("skills").isJsonArray()) {
                JsonArray arr = root.getAsJsonArray("skills");
                for (JsonElement s : arr) {
                    if (!s.isJsonPrimitive()) continue;
                    try {
                        skills.add(new ResourceLocation(s.getAsString()));
                    } catch (Exception ignored) {}
                }
            }

            ResourceLocation def = null;
            if (root.has("default") && root.get("default").isJsonPrimitive()) {
                try { def = new ResourceLocation(root.get("default").getAsString()); } catch (Exception ignored) {}
            }

            if (skills.isEmpty() || matchedItems.isEmpty()) continue;

            WeaponSkillSet set = new WeaponSkillSet(List.copyOf(skills), def);
            for (Item item : matchedItems) {
                // merge nếu item bị match nhiều file
                WeaponSkillSet prev = out.get(item);
                if (prev == null) {
                    out.put(item, set);
                } else {
                    LinkedHashSet<ResourceLocation> merged = new LinkedHashSet<>();
                    merged.addAll(prev.skills());
                    merged.addAll(set.skills());
                    ResourceLocation mergedDef = prev.defaultSkill() != null ? prev.defaultSkill() : set.defaultSkill();
                    out.put(item, new WeaponSkillSet(new ArrayList<>(merged), mergedDef));
                }
            }
        }

        WeaponSkillRegistry.setAll(out);
    }
}
