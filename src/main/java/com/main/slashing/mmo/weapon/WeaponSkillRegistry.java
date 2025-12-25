package com.main.slashing.mmo.weapon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapping item -> danh sách skill (load từ datapack: data/<modid>/weapon_skills/*.json).
 *
 * Mục tiêu: thêm skill vào bất kì vũ khí nào mà KHÔNG cần sửa Java của item.
 */
public final class WeaponSkillRegistry {
    private static final Map<Item, WeaponSkillSet> MAP = new HashMap<>();

    private WeaponSkillRegistry() {}

    public static void setAll(Map<Item, WeaponSkillSet> newMap) {
        MAP.clear();
        if (newMap != null) MAP.putAll(newMap);
    }

    public static List<ResourceLocation> getSkills(ItemStack stack, ServerPlayer player) {
        if (stack == null) return List.of();
        WeaponSkillSet set = MAP.get(stack.getItem());
        if (set == null || set.skills() == null) return List.of();
        return set.skills();
    }

    public static ResourceLocation getDefault(ItemStack stack, ServerPlayer player) {
        if (stack == null) return null;
        WeaponSkillSet set = MAP.get(stack.getItem());
        return set == null ? null : set.defaultSkill();
    }

    public static Map<Item, WeaponSkillSet> snapshot() {
        return Collections.unmodifiableMap(MAP);
    }
}
