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
        if (original == null || original.isEmpty()) return;

        String replaced = original;
        for (EmojiData.EmojiEntry entry : EmojiData.EMOJIS) {
            String shortcode = ":" + entry.name() + ":";
            if (replaced.contains(shortcode)) {
                replaced = replaced.replace(shortcode, entry.codepoint());
            }
        }

        if (!replaced.equals(original)) {
            event.setMessage(replaced);
        }
    }
}