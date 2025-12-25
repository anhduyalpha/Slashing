package com.main.slashing.skill;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IWeaponSkills {
    List<ResourceLocation> getSkills(ItemStack stack, Player player);

    default ResourceLocation defaultSkill(ItemStack stack, Player player) {
        List<ResourceLocation> s = getSkills(stack, player);
        return s.isEmpty() ? null : s.get(0);
    }
}
