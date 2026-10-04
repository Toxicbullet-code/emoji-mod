package com.emoji;

import net.minecraft.network.chat.Component;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EmojiMod.MODID)
public class EmojiServerEvents {

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        String raw = event.getRawText();
        String replaced = EmojiData.replaceEmojis(raw);

        if (!raw.equals(replaced)) {
            event.setMessage(Component.literal(replaced));
        }
    }
}