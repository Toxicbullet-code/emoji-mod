package com.emoji;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EmojiStyle {

    public static final ResourceLocation EMOJI_FONT = new ResourceLocation(EmojiMod.MODID, "emoji");
    private static final Pattern SHORTCODE_PATTERN = Pattern.compile(":([a-zA-Z0-9_]+):");

    private EmojiStyle() {}

    public static Component apply(Component component) {
        return transform(component);
    }

    private static Component transform(Component component) {
        if (component == null) return null;

        ComponentContents contents = component.getContents();
        Style style = component.getStyle();

        boolean siblingsChanged = false;
        List<Component> siblings = new ArrayList<>(component.getSiblings().size());
        for (Component sibling : component.getSiblings()) {
            Component transformed = transform(sibling);
            siblingsChanged |= (transformed != sibling);
            siblings.add(transformed);
        }

        if (contents instanceof LiteralContents literal
                && !EMOJI_FONT.equals(style.getFont())
                && containsShortcode(literal.text())) {
            MutableComponent wrapper = Component.empty();
            wrapper.setStyle(style);
            appendTextRuns(wrapper, literal.text());
            for (Component sibling : siblings) {
                wrapper.append(sibling);
            }
            return wrapper;
        }

        ComponentContents newContents = contents;
        if (contents instanceof TranslatableContents translatable) {
            Object[] args = translatable.getArgs();
            Object[] transformedArgs = null;

            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Component arg) {
                    Component transformed = transform(arg);
                    if (transformed != arg) {
                        if (transformedArgs == null) {
                            transformedArgs = args.clone();
                        }
                        transformedArgs[i] = transformed;
                    }
                } else if (args[i] instanceof String str) {
                    if (containsShortcode(str)) {
                        MutableComponent argWrapper = Component.empty();
                        appendTextRuns(argWrapper, str);
                        if (transformedArgs == null) {
                            transformedArgs = args.clone();
                        }
                        transformedArgs[i] = argWrapper;
                    }
                }
            }

            if (transformedArgs != null) {
                newContents = new TranslatableContents(translatable.getKey(), translatable.getFallback(), transformedArgs);
            }
        }

        if (!siblingsChanged && newContents == contents) {
            return component;
        }

        MutableComponent rebuilt = MutableComponent.create(newContents);
        rebuilt.setStyle(style);
        for (Component sibling : siblings) {
            rebuilt.append(sibling);
        }
        return rebuilt;
    }

    private static boolean containsShortcode(String text) {
        Matcher matcher = SHORTCODE_PATTERN.matcher(text);
        while (matcher.find()) {
            if (EmojiData.getByName(matcher.group(1)) != null) {
                return true;
            }
        }
        return false;
    }

    private static void appendTextRuns(MutableComponent target, String text) {
        Matcher matcher = SHORTCODE_PATTERN.matcher(text);
        int lastPos = 0;

        while (matcher.find()) {
            String shortcode = matcher.group(1);
            EmojiData.EmojiEntry entry = EmojiData.getByName(shortcode);

            if (entry != null) {
                if (matcher.start() > lastPos) {
                    target.append(Component.literal(text.substring(lastPos, matcher.start())));
                }

                target.append(Component.literal(entry.codepoint())
                        .withStyle(Style.EMPTY.withFont(EMOJI_FONT)));

                lastPos = matcher.end();
            }
        }

        if (lastPos < text.length()) {
            target.append(Component.literal(text.substring(lastPos)));
        }
    }
}