package com.main.slashing.skill;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import com.main.slashing.mmo.weapon.WeaponSkillRegistry;

public final class SkillManager {
    private static final String NBT_SELECTED = "slashing_selected_skill";

    private SkillManager() {}

    public static List<ResourceLocation> getWeaponSkills(ServerPlayer player, ItemStack stack) {
        Item item = stack.getItem();
        Set<ResourceLocation> merged = new LinkedHashSet<>();

        if (item instanceof IWeaponSkills ws) {
            merged.addAll(ws.getSkills(stack, player));
        }
        merged.addAll(WeaponSkillRegistry.getSkills(stack, player));

        if (merged.isEmpty()) return List.of();
        return new ArrayList<>(merged);
    }

    public static ResourceLocation getSelectedSkill(ServerPlayer player, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(NBT_SELECTED)) {
            String s = tag.getString(NBT_SELECTED);
            try { return new ResourceLocation(s); } catch (Exception ignored) {}
        }

        Item item = stack.getItem();
        // Ưu tiên default từ item (nếu có), fallback sang WeaponSkillRegistry
        if (item instanceof IWeaponSkills ws) {
            ResourceLocation d = ws.defaultSkill(stack, player);
            if (d != null) return d;
        }
        return WeaponSkillRegistry.getDefault(stack, player);
    }

    public static void setSelectedSkill(ItemStack stack, ResourceLocation id) {
        if (id == null) return;
        stack.getOrCreateTag().putString(NBT_SELECTED, id.toString());
    }

    public static void cycleSkill(ServerPlayer player, ItemStack stack, int dir) {
        List<ResourceLocation> list = getWeaponSkills(player, stack);
        if (list.isEmpty()) return;

        ResourceLocation cur = getSelectedSkill(player, stack);
        int idx = Math.max(0, list.indexOf(cur));
        int next = (idx + dir) % list.size();
        if (next < 0) next += list.size();
        setSelectedSkill(stack, list.get(next));
    }

    public static ISkill getSelectedSkillObj(ServerPlayer player, ItemStack stack) {
        ResourceLocation id = getSelectedSkill(player, stack);
        return id == null ? null : SkillRegistry.get(id);
    }

    public static void castSelected(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        ISkill skill = getSelectedSkillObj(player, stack);
        if (skill == null) return;
        if (!skill.canCast(player, stack, hand)) return;

        skill.cast(player, stack, hand);
        player.getCooldowns().addCooldown(stack.getItem(), skill.cooldownTicks());
    }
}
