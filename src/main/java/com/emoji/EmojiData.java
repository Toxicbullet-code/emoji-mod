package com.emoji;

import java.util.LinkedHashMap;
import java.util.Map;

public class EmojiData {
    public static final Map<String, String> EMOJIS = new LinkedHashMap<>();

    static {
        EMOJIS.put(":100:", "\uE000");        // 💯
        EMOJIS.put(":clown:", "\uE001");      // 🤡
        EMOJIS.put(":cry:", "\uE002");        // 😢
        EMOJIS.put(":vomit:", "\uE003");      // 🤮
        EMOJIS.put(":heart:", "\uE004");      // ❤️
        EMOJIS.put(":hearteyes:", "\uE005");  // 😍
        EMOJIS.put(":hearthands:", "\uE006"); // 🫶
        EMOJIS.put(":joy:", "\uE007");        // 😂
        EMOJIS.put(":moneymouth:", "\uE008"); // 🤑
        EMOJIS.put(":moneywings:", "\uE009"); // 💸
        EMOJIS.put(":party:", "\uE00A");      // 🥳
        EMOJIS.put(":angry:", "\uE00B");      // 😡
        EMOJIS.put(":smile:", "\uE00C");      // 😄
        EMOJIS.put(":hearthearts:", "\uE00D");// 🥰
        EMOJIS.put(":imp:", "\uE00E");        // 😈
        EMOJIS.put(":sob:", "\uE00F");        // 😭
        EMOJIS.put(":wave:", "\uE010");       // 👋

    }

    public static String replaceEmojis(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        String result = message;
        for (Map.Entry<String, String> entry : EMOJIS.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
}