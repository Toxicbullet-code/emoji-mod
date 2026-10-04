package com.emoji;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = EmojiMod.MODID, value = Dist.CLIENT)
public class EmojiClientEvents {

    /**
     * Maximum number of glyph atlas pages a single font may create. The emoji font
     * only needs one, but we probe a few pages defensively.
     */
    private static final int MAX_FONT_ATLAS_PAGES = 8;

    /**
     * Last known GL texture id we applied linear filtering to, per resource location.
     * Used so filtering is only reapplied when a texture is (re)created, e.g. after
     * a resource reload, while still being robust against GL id reuse.
     */
    private static final Map<ResourceLocation, Integer> SMOOTHED_TEXTURES = new HashMap<>();

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String original = event.getMessage();

        if (original.startsWith("/")) return;

        String replaced = EmojiData.replaceEmojis(original);

        if (!original.equals(replaced)) {
            event.setMessage(replaced);
        }
    }

    /**
     * Routes emoji characters in incoming chat messages through the dedicated
     * emoji font so they can be rendered with smooth filtering.
     */
    @SubscribeEvent
    public static void onChatReceived(ClientChatReceivedEvent event) {
        Component original = event.getMessage();
        Component styled = EmojiStyle.apply(original);

        if (styled != original) {
            event.setMessage(styled);
        }
    }

    /**
     * Minecraft samples font glyphs with nearest-neighbour filtering, which makes
     * the 27x27 emoji artwork look blocky once it is scaled up in chat or in the
     * autocomplete popup. This tick switches every emoji-related texture to linear
     * filtering once it has been created.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        // Glyph atlas pages stitched for the dedicated emoji font (emoji:emoji/N).
        for (int i = 0; i < MAX_FONT_ATLAS_PAGES; i++) {
            smooth(textureManager, new ResourceLocation(EmojiMod.MODID, "emoji/" + i));
        }

        // Raw emoji PNGs used by the autocomplete popup icons.
        for (EmojiData.EmojiEntry entry : EmojiData.EMOJIS) {
            smooth(textureManager, entry.texture());
        }
    }

    private static void smooth(TextureManager textureManager, ResourceLocation location) {
        AbstractTexture texture = textureManager.getTexture(location, null);
        if (texture == null) return; // Not created/registered yet; retry next tick.

        int textureId = texture.getId();
        Integer previousId = SMOOTHED_TEXTURES.put(location, textureId);

        if (previousId == null || previousId != textureId) {
            // blur = true, mipmap = false -> GL_LINEAR for minification and magnification.
            texture.setFilter(true, false);
        }
    }
}