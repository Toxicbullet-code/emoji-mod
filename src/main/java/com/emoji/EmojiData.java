package com.emoji;

import java.util.LinkedHashMap;
import java.util.Map;

public class EmojiData {
    public static final Map<String, String> EMOJIS = new LinkedHashMap<>();

    static {
        EMOJIS.put(":sob:", "😭");
        EMOJIS.put(":angry:", "😡");
        EMOJIS.put(":smile:", "😄");
        EMOJIS.put(":heart:", "❤️");
        EMOJIS.put(":fire:", "🔥");
        EMOJIS.put(":skull:", "💀");
        EMOJIS.put(":thumbsup:", "👍");
        EMOJIS.put(":clown:", "🤡");
        EMOJIS.put(":eyes:", "👀");
        EMOJIS.put(":100:", "💯");
        EMOJIS.put(":skull:", "☠️");
        EMOJIS.put(":star:", "⭐");
        EMOJIS.put(":check:", "✅");
        EMOJIS.put(":x:", "❌");
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