package com.emoji;

import net.minecraft.resources.ResourceLocation;
import java.util.List;

public class EmojiData {

    public record EmojiEntry(String code, String character, ResourceLocation texture) {}

    public static final List<EmojiEntry> EMOJIS = List.of(
            new EmojiEntry("100", "\uE000", new ResourceLocation(EmojiMod.MODID, "textures/font/100.png")),
            new EmojiEntry("clown", "\uE001", new ResourceLocation(EmojiMod.MODID, "textures/font/clown_face.png")),
            new EmojiEntry("cry", "\uE002", new ResourceLocation(EmojiMod.MODID, "textures/font/cry.png")),
            new EmojiEntry("vomit", "\uE003", new ResourceLocation(EmojiMod.MODID, "textures/font/face_vomiting.png")),
            new EmojiEntry("heart", "\uE004", new ResourceLocation(EmojiMod.MODID, "textures/font/heart.png")),
            new EmojiEntry("hearteyes", "\uE005", new ResourceLocation(EmojiMod.MODID, "textures/font/heart_eyes.png")),
            new EmojiEntry("hearthands", "\uE006", new ResourceLocation(EmojiMod.MODID, "textures/font/heart_hands.png")),
            new EmojiEntry("joy", "\uE007", new ResourceLocation(EmojiMod.MODID, "textures/font/joy.png")),
            new EmojiEntry("moneymouth", "\uE008", new ResourceLocation(EmojiMod.MODID, "textures/font/money_mouth_face.png")),
            new EmojiEntry("moneywings", "\uE009", new ResourceLocation(EmojiMod.MODID, "textures/font/money_with_wings.png")),
            new EmojiEntry("party", "\uE00A", new ResourceLocation(EmojiMod.MODID, "textures/font/partying_face.png")),
            new EmojiEntry("angry", "\uE00B", new ResourceLocation(EmojiMod.MODID, "textures/font/rage.png")),
            new EmojiEntry("smile", "\uE00C", new ResourceLocation(EmojiMod.MODID, "textures/font/smiley.png")),
            new EmojiEntry("hearthearts", "\uE00D", new ResourceLocation(EmojiMod.MODID, "textures/font/smiling_face_with_3_hearts.png")),
            new EmojiEntry("devil", "\uE00E", new ResourceLocation(EmojiMod.MODID, "textures/font/smiling_imp.png")),
            new EmojiEntry("sob", "\uE00F", new ResourceLocation(EmojiMod.MODID, "textures/font/sob.png")),
            new EmojiEntry("wave", "\uE010", new ResourceLocation(EmojiMod.MODID, "textures/font/wave.png")),
            new EmojiEntry("skull", "\uE011", new ResourceLocation(EmojiMod.MODID, "textures/font/skull.png")),
            new EmojiEntry("pray", "\uE012", new ResourceLocation(EmojiMod.MODID, "textures/font/pray.png")),
            new EmojiEntry("hotdog", "\uE013", new ResourceLocation(EmojiMod.MODID, "textures/font/hotdog.png"))
    );

    public static boolean isEmojiChar(char c) {
        for (EmojiEntry entry : EMOJIS) {
            String character = entry.character();
            if (!character.isEmpty() && character.charAt(0) == c) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsEmoji(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (isEmojiChar(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static String replaceEmojis(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        String result = message;
        for (EmojiEntry entry : EMOJIS) {
            result = result.replace(":" + entry.code() + ":", entry.character());
        }
        return result;
    }
}