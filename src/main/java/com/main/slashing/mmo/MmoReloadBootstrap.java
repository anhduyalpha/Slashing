package com.main.slashing.mmo;

import com.main.slashing.mmo.skill.MmoSkillReloadListener;
import com.main.slashing.mmo.weapon.WeaponSkillReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Đăng ký reload listeners để skill/weapon skills load từ datapack.
 * Hỗ trợ /reload mà không cần restart server.
 */
@Mod.EventBusSubscriber(modid = "slashing_alphad")
public final class MmoReloadBootstrap {
    private MmoReloadBootstrap() {}

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent e) {
        e.addListener(new MmoSkillReloadListener());
        e.addListener(new WeaponSkillReloadListener());
    }
}
