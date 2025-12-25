package com.main.slashing.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface ISkill {
    ResourceLocation id();
    String nameKey();

    default Component displayName() {
        return Component.translatable(nameKey());
    }

    default boolean canCast(ServerPlayer player, ItemStack stack, InteractionHand hand) { return true; }

    void cast(ServerPlayer player, ItemStack stack, InteractionHand hand);

    default int cooldownTicks() { return 10; }
}
