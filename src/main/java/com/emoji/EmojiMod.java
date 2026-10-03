package com.emoji;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Mod(EmojiMod.MODID)
@Mod.EventBusSubscriber(modid = EmojiMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EmojiMod {

    public static final String MODID = "emoji";
    public static final ResourceLocation EMOJI_SHEET = new ResourceLocation(MODID, "textures/font/emojis.png");

    public record EmojiEntry(String code, String character, int index) {}

    public static final List<EmojiEntry> EMOJIS = List.of(
            new EmojiEntry("sob", "\uE000", 0),
            new EmojiEntry("skull", "\uE001", 1),
            new EmojiEntry("fire", "\uE002", 2),
            new EmojiEntry("joy", "\uE003", 3),
            new EmojiEntry("smile", "\uE004", 4),
            new EmojiEntry("heart", "\uE005", 5),
            new EmojiEntry("thumbsup", "\uE006", 6),
            new EmojiEntry("clown", "\uE007", 7),
            new EmojiEntry("eyes", "\uE008", 8),
            new EmojiEntry("100", "\uE009", 9)
    );

    private static int selectedIndex = 0;
    private static final List<EmojiEntry> currentMatches = new ArrayList<>();
    private static int boxX, boxY, boxW, boxH;

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String msg = event.getMessage();
        if (msg.startsWith("/")) return;

        for (EmojiEntry entry : EMOJIS) {
            msg = msg.replace(":" + entry.code() + ":", entry.character());
        }
        event.setMessage(msg);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof ChatScreen chatScreen)) return;

        EditBox editBox = getEditBox(chatScreen);
        if (editBox == null) return;

        updateMatches(editBox);
        if (currentMatches.isEmpty()) return;

        if (selectedIndex >= currentMatches.size()) selectedIndex = 0;

        GuiGraphics graphics = event.getGuiGraphics();
        int itemHeight = 14;
        boxW = 140;
        boxH = currentMatches.size() * itemHeight + 4;
        boxX = editBox.getX() + 2;
        boxY = editBox.getY() - boxH - 4;

        graphics.fill(boxX - 1, boxY - 1, boxX + boxW + 1, boxY + boxH + 1, 0xD0000000);

        RenderSystem.enableBlend();

        for (int i = 0; i < currentMatches.size(); i++) {
            EmojiEntry entry = currentMatches.get(i);
            int itemY = boxY + 2 + (i * itemHeight);

            if (i == selectedIndex) {
                graphics.fill(boxX, itemY, boxX + boxW, itemY + itemHeight, 0xFF0055AA);
            }

            graphics.blit(EMOJI_SHEET, boxX + 4, itemY + 2, entry.index() * 16, 0, 10, 10, 160, 16);
            graphics.drawString(Minecraft.getInstance().font, ":" + entry.code() + ":", boxX + 18, itemY + 3, i == selectedIndex ? 0xFFFFFF : 0xAAAAAA);
        }
        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof ChatScreen chatScreen)) return;
        if (currentMatches.isEmpty()) return;

        EditBox editBox = getEditBox(chatScreen);
        if (editBox == null) return;

        int key = event.getKeyCode();

        if (key == GLFW.GLFW_KEY_UP) {
            selectedIndex = (selectedIndex - 1 + currentMatches.size()) % currentMatches.size();
            event.setCanceled(true);
        } else if (key == GLFW.GLFW_KEY_DOWN) {
            selectedIndex = (selectedIndex + 1) % currentMatches.size();
            event.setCanceled(true);
        } else if (key == GLFW.GLFW_KEY_TAB || key == GLFW.GLFW_KEY_ENTER) {
            applySelection(editBox);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseButtonPressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof ChatScreen chatScreen)) return;
        if (currentMatches.isEmpty()) return;

        EditBox editBox = getEditBox(chatScreen);
        if (editBox == null) return;

        double mx = event.getMouseX();
        double my = event.getMouseY();

        if (mx >= boxX && mx <= boxX + boxW && my >= boxY && my <= boxY + boxH) {
            int clickedIdx = (int) ((my - boxY - 2) / 14);
            if (clickedIdx >= 0 && clickedIdx < currentMatches.size()) {
                selectedIndex = clickedIdx;
                applySelection(editBox);
                event.setCanceled(true);
            }
        }
    }

    private static void updateMatches(EditBox editBox) {
        currentMatches.clear();
        String text = editBox.getValue();
        int cursor = editBox.getCursorPosition();

        if (cursor <= 0 || cursor > text.length()) return;
        String sub = text.substring(0, cursor);
        int lastColon = sub.lastIndexOf(':');
        if (lastColon == -1) return;

        String query = sub.substring(lastColon + 1);
        if (query.contains(" ")) return;

        for (EmojiEntry entry : EMOJIS) {
            if (entry.code().toLowerCase().startsWith(query.toLowerCase())) {
                currentMatches.add(entry);
            }
        }
    }

    private static void applySelection(EditBox editBox) {
        if (selectedIndex < 0 || selectedIndex >= currentMatches.size()) return;

        EmojiEntry selected = currentMatches.get(selectedIndex);
        String text = editBox.getValue();
        int cursor = editBox.getCursorPosition();
        String sub = text.substring(0, cursor);
        int lastColon = sub.lastIndexOf(':');

        if (lastColon != -1) {
            String before = text.substring(0, lastColon);
            String after = text.substring(cursor);
            String replacement = ":" + selected.code() + ": ";

            editBox.setValue(before + replacement + after);
            editBox.setCursorPosition(before.length() + replacement.length());
            currentMatches.clear();
        }
    }

    private static EditBox getEditBox(ChatScreen screen) {
        return screen.children().stream()
                .filter(c -> c instanceof EditBox)
                .map(c -> (EditBox) c)
                .findFirst()
                .orElse(null);
    }
}
