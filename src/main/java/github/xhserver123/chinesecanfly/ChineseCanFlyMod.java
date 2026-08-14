// Copyright (c) 2026 XHServer. Licensed under the MIT License.
package github.xhserver123.chinesecanfly;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChineseCanFlyMod implements ModInitializer {
    public static final String MOD_ID = "chinesecanfly";
    public static final Identifier EFFECT_ID = Identifier.of(MOD_ID, "chinesecanfly");
    public static final Identifier SOUND_ID = Identifier.of(MOD_ID, "chinacanfly");
    public static final Identifier ADVANCEMENT_ID = Identifier.of(MOD_ID, "chinese_can_fly");

    private static final ChineseCanFlyEffect EFFECT = new ChineseCanFlyEffect();
    private static RegistryEntry<StatusEffect> effectEntry;
    private static SoundEvent soundEvent;

    private static final Map<UUID, Long> COOLDOWN_MAP = new ConcurrentHashMap<>();
    // 记录当前因本效果而获得飞行能力的玩家，用于在效果结束/移除后正确关闭飞行
    private static final Set<UUID> FLYING_PLAYERS = ConcurrentHashMap.newKeySet();

    @Override
    public void onInitialize() {
        // 1. 注册自定义状态效果
        Registry.register(Registries.STATUS_EFFECT, EFFECT_ID, EFFECT);
        effectEntry = Registries.STATUS_EFFECT.getEntry(EFFECT);

        // 2. 注册声音事件
        soundEvent = Registry.register(Registries.SOUND_EVENT, SOUND_ID, SoundEvent.of(SOUND_ID));

        // 3. 聊天监听（服务端）：玩家在聊天框发送「我是中国人」触发飞行
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            String text = message.getContent().getString();
            if (!"我是中国人".equals(text)) return;

            ServerPlayerEntity player = sender;
            UUID uuid = player.getUuid();
            long now = System.currentTimeMillis();

            Long lastTrigger = COOLDOWN_MAP.get(uuid);
            if (lastTrigger != null && now - lastTrigger < 5 * 60 * 1000) {
                return; // 冷却中
            }

            // 给予效果（3分钟 = 3*60*20 = 3600 ticks），onApplied 会自动开启创造模式式飞行
            player.addStatusEffect(new StatusEffectInstance(
                    effectEntry,
                    3 * 60 * 20,   // 持续时间（tick）
                    0,             // 放大器
                    false,         // 是否显示粒子
                    true,          // 是否显示图标
                    true           // 是否在物品栏显示
            ));

            // 播放音乐（仅该玩家听到）
            player.playSound(soundEvent, 1.0F, 1.0F);

            // 授予成就（数据驱动的成就：data/chinesecanfly/advancement/chinese_can_fly.json）
            AdvancementEntry advancement = player.getEntityWorld().getServer().getAdvancementLoader().get(ADVANCEMENT_ID);
            if (advancement != null) {
                player.getAdvancementTracker().grantCriterion(advancement, "trigger");
            }

            // 记录冷却时间
            COOLDOWN_MAP.put(uuid, now);
        });

        // 4. 玩家加入时，若已有效果则恢复飞行
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            if (player.hasStatusEffect(effectEntry)) {
                enableFlying(player);
            }
        });

        // 5. 玩家重生（含维度切换）时检查
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (newPlayer.hasStatusEffect(effectEntry)) {
                enableFlying(newPlayer);
            }
        });

        // 6. 玩家断开时清理状态
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            FLYING_PLAYERS.remove(handler.player.getUuid());
        });

        // 7. 每 tick：
        //    - 有效果的玩家：维持 allowFlying=true，防止被任何逻辑重置导致「飞不了」
        //    - 效果消失的玩家：关闭飞行（非创造/旁观）
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID uuid = player.getUuid();
                if (player.hasStatusEffect(effectEntry)) {
                    FLYING_PLAYERS.add(uuid);
                    // 有 buff 期间每 tick 保持飞行权限（不强制 flying，玩家可自由落地/起飞）
                    if (!player.getAbilities().allowFlying) {
                        player.getAbilities().allowFlying = true;
                        player.sendAbilitiesUpdate();
                    }
                } else if (FLYING_PLAYERS.remove(uuid)) {
                    disableFlying(player);
                }
            }
        });
    }

    /** 开启创造模式式飞行（不改游戏模式） */
    private static void enableFlying(ServerPlayerEntity player) {
        player.getAbilities().allowFlying = true;
        player.getAbilities().flying = true;
        player.sendAbilitiesUpdate();
    }

    /** 关闭飞行（创造/旁观模式保持原样） */
    private static void disableFlying(ServerPlayerEntity player) {
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().allowFlying = false;
            player.getAbilities().flying = false;
            player.sendAbilitiesUpdate();
        }
    }
}
