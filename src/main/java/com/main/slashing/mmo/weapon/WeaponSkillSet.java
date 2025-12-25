package com.main.slashing.mmo.weapon;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record WeaponSkillSet(List<ResourceLocation> skills, ResourceLocation defaultSkill) {
    public static WeaponSkillSet empty() {
        return new WeaponSkillSet(List.of(), null);
    }
}
