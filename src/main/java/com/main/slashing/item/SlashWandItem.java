package com.main.slashing.item;

import com.main.slashing.skill.IWeaponSkills;
import com.main.slashing.skill.SkillManager;
import com.main.slashing.sun.SunBreathSkills;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class SlashWandItem extends Item implements IWeaponSkills {
    public SlashWandItem(Properties props) { super(props); }

    @Override
    public List<ResourceLocation> getSkills(ItemStack stack, Player player) {
        return List.of(
                SunBreathSkills.FORM1,
                SunBreathSkills.FORM2,
                SunBreathSkills.FORM3,
                SunBreathSkills.FORM4,
                SunBreathSkills.FORM5,
                SunBreathSkills.FORM6,
                SunBreathSkills.FORM7,
                SunBreathSkills.FORM8,
                SunBreathSkills.FORM9,
                SunBreathSkills.FORM10,
                SunBreathSkills.FORM11
                );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            SkillManager.castSelected(sp, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
