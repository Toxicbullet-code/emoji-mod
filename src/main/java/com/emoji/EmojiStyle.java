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

/**
 * Re-styles emoji characters so they render through the dedicated {@code emoji:emoji}
 * font (which uses smooth linear filtering) instead of the vanilla default font
 * (which uses nearest-neighbour filtering and therefore looks pixelated).
 *
 * <p>The transform is idempotent: components that already use the emoji font are
 * returned unchanged, and components without emoji characters are returned with
 * their original identity so callers can cheaply detect "nothing changed".</p>
 */
public final class EmojiStyle {

    /** The font that renders emoji glyphs with smooth filtering. */
    public static final ResourceLocation EMOJI_FONT = new ResourceLocation(EmojiMod.MODID, "emoji");

    private EmojiStyle() {
    }

    /**
     * Applies the emoji font to every emoji character inside the given component tree.
     *
     * @param component the component to transform
     * @return the transformed component, or the exact same instance when nothing changed
     */
    public static Component apply(Component component) {
        return transform(component);
    }

    private static Component transform(Component component) {
        ComponentContents contents = component.getContents();
        Style style = component.getStyle();

        boolean siblingsChanged = false;
        List<Component> siblings = new ArrayList<>(component.getSiblings().size());
        for (Component sibling : component.getSiblings()) {
            Component transformed = transform(sibling);
            siblingsChanged |= transformed != sibling;
            siblings.add(transformed);
        }

        // Plain text containing emoji characters: split it into styled runs.
        if (contents instanceof LiteralContents literal
                && !EMOJI_FONT.equals(style.getFont())
                && EmojiData.containsEmoji(literal.text())) {
            MutableComponent wrapper = Component.empty();
            wrapper.setStyle(style);
            appendTextRuns(wrapper, literal.text());
            for (Component sibling : siblings) {
                wrapper.append(sibling);
            }
            return wrapper;
        }

        // Translations: emoji characters may live inside the translation arguments.
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
                }
            }

            if (transformedArgs != null) {
                contents = new TranslatableContents(translatable.getKey(), translatable.getFallback(), transformedArgs);
            }
        }

        if (!siblingsChanged && contents == component.getContents()) {
            return component;
        }

        MutableComponent rebuilt = MutableComponent.create(contents);
        rebuilt.setStyle(style);
        for (Component sibling : siblings) {
            rebuilt.append(sibling);
        }
        return rebuilt;
    }

    /**
     * Splits plain text into runs of regular text and runs of emoji characters,
     * appending each run to the target. Emoji runs are styled with the emoji font.
     */
    private static void appendTextRuns(MutableComponent target, String text) {
        StringBuilder run = new StringBuilder();
        boolean emojiRun = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean isEmoji = EmojiData.isEmojiChar(c);

            if (run.length() > 0 && isEmoji != emojiRun) {
                flushRun(target, run, emojiRun);
            }

            emojiRun = isEmoji;
            run.append(c);
        }

        flushRun(target, run, emojiRun);
    }

    private static void flushRun(MutableComponent target, StringBuilder run, boolean emojiRun) {
        if (run.length() == 0) {
            return;
        }

        String value = run.toString();
        run.setLength(0);

        if (emojiRun) {
            target.append(Component.literal(value).withStyle(Style.EMPTY.withFont(EMOJI_FONT)));
        } else {
            target.append(Component.literal(value));
        }
    }
}
