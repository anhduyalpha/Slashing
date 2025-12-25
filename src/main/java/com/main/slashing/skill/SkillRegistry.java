package com.main.slashing.skill;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

public final class SkillRegistry {
    private static final Map<ResourceLocation, ISkill> SKILLS = new HashMap<>();
    /** Skills loaded từ datapack/reload listener (để có thể refresh khi /reload). */
    private static final Set<ResourceLocation> DATA_SKILLS = new HashSet<>();

    private SkillRegistry() {}

    public static void register(ISkill skill) {
        if (skill == null) return;
        SKILLS.put(skill.id(), skill);
    }

    /** Register skill được load từ data pack (có thể bị thay thế khi /reload). */
    public static void registerData(ISkill skill) {
        if (skill == null) return;
        SKILLS.put(skill.id(), skill);
        DATA_SKILLS.add(skill.id());
    }

    /** Xóa toàn bộ skill đã được load từ datapack trước đó. */
    public static void clearData() {
        for (ResourceLocation id : DATA_SKILLS) {
            SKILLS.remove(id);
        }
        DATA_SKILLS.clear();
    }

    public static ISkill get(ResourceLocation id) {
        return SKILLS.get(id);
    }

    public static Collection<ISkill> all() {
        return Collections.unmodifiableCollection(SKILLS.values());
    }
}
