package com.emoji;

import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmojiData {

    public static final String MODID = "emoji";

    public record EmojiEntry(String name, ResourceLocation texture, String codepoint) {}

    public static final List<EmojiEntry> EMOJIS;

    static {
        List<EmojiEntry> list = new ArrayList<>();

        list.add(new EmojiEntry("100", new ResourceLocation(MODID, "textures/font/100.png"), "\uE000"));
        list.add(new EmojiEntry("clown_face", new ResourceLocation(MODID, "textures/font/clown_face.png"), "\uE001"));
        list.add(new EmojiEntry("cry", new ResourceLocation(MODID, "textures/font/cry.png"), "\uE002"));
        list.add(new EmojiEntry("face_vomiting", new ResourceLocation(MODID, "textures/font/face_vomiting.png"), "\uE003"));
        list.add(new EmojiEntry("heart", new ResourceLocation(MODID, "textures/font/heart.png"), "\uE004"));
        list.add(new EmojiEntry("heart_eyes", new ResourceLocation(MODID, "textures/font/heart_eyes.png"), "\uE005"));
        list.add(new EmojiEntry("heart_hands", new ResourceLocation(MODID, "textures/font/heart_hands.png"), "\uE006"));
        list.add(new EmojiEntry("hotdog", new ResourceLocation(MODID, "textures/font/hotdog.png"), "\uE007"));
        list.add(new EmojiEntry("joy", new ResourceLocation(MODID, "textures/font/joy.png"), "\uE008"));
        list.add(new EmojiEntry("money_mouth_face", new ResourceLocation(MODID, "textures/font/money_mouth_face.png"), "\uE009"));
        list.add(new EmojiEntry("money_with_wings", new ResourceLocation(MODID, "textures/font/money_with_wings.png"), "\uE00A"));
        list.add(new EmojiEntry("partying_face", new ResourceLocation(MODID, "textures/font/partying_face.png"), "\uE00B"));
        list.add(new EmojiEntry("pray", new ResourceLocation(MODID, "textures/font/pray.png"), "\uE00C"));
        list.add(new EmojiEntry("rage", new ResourceLocation(MODID, "textures/font/rage.png"), "\uE00D"));
        list.add(new EmojiEntry("skull", new ResourceLocation(MODID, "textures/font/skull.png"), "\uE00E"));
        list.add(new EmojiEntry("smiley", new ResourceLocation(MODID, "textures/font/smiley.png"), "\uE00F"));
        list.add(new EmojiEntry("smiling_face_with_3_hearts", new ResourceLocation(MODID, "textures/font/smiling_face_with_3_hearts.png"), "\uE010"));
        list.add(new EmojiEntry("devil", new ResourceLocation(MODID, "textures/font/smiling_imp.png"), "\uE011"));
        list.add(new EmojiEntry("sob", new ResourceLocation(MODID, "textures/font/sob.png"), "\uE012"));
        list.add(new EmojiEntry("wave", new ResourceLocation(MODID, "textures/font/wave.png"), "\uE013"));
        list.add(new EmojiEntry("broken_heart", new ResourceLocation(MODID, "textures/font/broken_heart.png"), "\uE014"));

        EMOJIS = Collections.unmodifiableList(list);
    }

    public static EmojiEntry getByName(String name) {
        for (EmojiEntry entry : EMOJIS) {
            if (entry.name().equalsIgnoreCase(name)) {
                return entry;
            }
        }
        return null;
    }
}