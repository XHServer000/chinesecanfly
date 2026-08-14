// Copyright (c) 2026 XHServer. Licensed under the MIT License.
package github.xhserver123.chinesecanfly;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.network.ServerPlayerEntity;

public class ChineseCanFlyEffect extends StatusEffect {
    public ChineseCanFlyEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x00FF00); // 绿色药水颜色
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayerEntity player) {
            // 启用创造模式式飞行（游戏模式本身不变，仍是生存/冒险）
            player.getAbilities().allowFlying = true;
            player.getAbilities().flying = true;
            player.sendAbilitiesUpdate();
        }
        super.onApplied(entity, amplifier);
    }
}
