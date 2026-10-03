package com.emoji;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EmojiMod.MODID, value = Dist.CLIENT)
public class EmojiClientEvents {

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String original = event.getMessage();
        String replaced = EmojiData.replaceEmojis(original);

        if (!original.equals(replaced)) {
            event.setMessage(replaced);
        }
    }
}