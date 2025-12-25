package com.main.slashing.item;

import com.main.slashing.SlashingAlphadMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SlashingAlphadMod.MODID);

    public static final RegistryObject<Item> SLASH_WAND =
            ITEMS.register("slash_wand", () -> new SlashWandItem(new Item.Properties().stacksTo(1)));

    private ModItems() {}
}
