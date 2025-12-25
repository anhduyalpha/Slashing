package com.main.slashing.mmo.skill;

import com.main.slashing.fx.DelayedFxTask;
import com.main.slashing.fx.FxManager;
import com.main.slashing.mmo.ScreenFlashSpec;
import com.main.slashing.mmo.ScreenShakeSpec;
import com.main.slashing.mmo.SoundSpec;
import com.main.slashing.mmo.SkillElement;
import com.main.slashing.net.ModNet;
import com.main.slashing.net.S2C_ScreenFlashPacket;
import com.main.slashing.net.S2C_ScreenShakePacket;
import com.main.slashing.skill.ISkill;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Skill load từ JSON.
 * - Ưu tiên: dễ thêm skill mới chỉ bằng data file
 * - An toàn: mọi FX/Hit chạy server-side, client chỉ nhận shake/flash.
 */
public final class DataDrivenSkill implements ISkill {

    private final ResourceLocation id;
    private final String nameKey;
    private final int cooldownTicks;
    private final SkillElement element;

    private final SoundSpec castSound;
    private final ScreenShakeSpec castShake;
    private final ScreenFlashSpec castFlash;

    private final List<MmoAction> actions;

    public DataDrivenSkill(
            ResourceLocation id,
            String nameKey,
            int cooldownTicks,
            SkillElement element,
            SoundSpec castSound,
            ScreenShakeSpec castShake,
            ScreenFlashSpec castFlash,
            List<MmoAction> actions
    ) {
        this.id = id;
        this.nameKey = nameKey;
        this.cooldownTicks = Math.max(1, cooldownTicks);
        this.element = element == null ? SkillElement.PHYSICAL : element;
        this.castSound = castSound == null ? SoundSpec.none() : castSound;
        this.castShake = castShake == null ? ScreenShakeSpec.none() : castShake;
        this.castFlash = castFlash == null ? ScreenFlashSpec.none() : castFlash;
        this.actions = actions == null ? List.of() : actions;
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public String nameKey() {
        return nameKey;
    }

    public SkillElement element() {
        return element;
    }

    @Override
    public int cooldownTicks() {
        return cooldownTicks;
    }

    @Override
    public void cast(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        // ---- base sound ----
        if (castSound != null && castSound.eventId() != null) {
            castSound.play(player.serverLevel(), player.blockPosition());
        }

        // ---- cast shake/flash (chỉ player cast) ----
        if (castShake.intensity() > 0.001f) {
            ModNet.sendToPlayer(player, new S2C_ScreenShakePacket(
                    castShake.intensity(), castShake.durationTicks(), castShake.frequency()
            ));
        }
        if (castFlash.alpha() > 0.001f) {
            ModNet.sendToPlayer(player, new S2C_ScreenFlashPacket(
                    castFlash.rgb(), castFlash.alpha(), castFlash.durationTicks()
            ));
        }

        // ---- schedule FX tasks ----
        for (MmoAction a : actions) {
            if (a == null) continue;
            var task = a.build(player);
            if (task == null) continue;
            int d = Math.max(0, a.delayTicks());
            FxManager.schedule(d > 0 ? new DelayedFxTask(d, task) : task);
        }
    }
}
