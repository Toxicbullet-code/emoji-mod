package com.emoji;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.api.distmarker.Dist;
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

    private static int selectedIndex = 0;
    private static final List<EmojiData.EmojiEntry> currentMatches = new ArrayList<>();
    private static int boxX, boxY, boxW, boxH;

    // Emoji picker panel, shown on the right side of the chat screen.
    private static final int PICKER_WIDTH = 104;
    private static final int PICKER_ITEM_HEIGHT = 14;
    private static int pickerX, pickerY, pickerH;

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof ChatScreen chatScreen)) return;

        EditBox editBox = getEditBox(chatScreen);
        if (editBox == null) return;

        updateMatches(editBox);

        drawEmojiPicker(event.getGuiGraphics(), chatScreen, editBox, event.getMouseX(), event.getMouseY());

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
            EmojiData.EmojiEntry entry = currentMatches.get(i);
            int itemY = boxY + 2 + (i * itemHeight);

            if (i == selectedIndex) {
                graphics.fill(boxX, itemY, boxX + boxW, itemY + itemHeight, 0xFF0055AA);
            }

            graphics.blit(entry.texture(), boxX + 4, itemY + 2, 0, 0, 10, 10, 10, 10);
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
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;

        EditBox editBox = getEditBox(chatScreen);
        if (editBox == null) return;

        double mx = event.getMouseX();
        double my = event.getMouseY();

        // Autocomplete popup has priority when the click lands inside it.
        if (!currentMatches.isEmpty() && mx >= boxX && mx <= boxX + boxW && my >= boxY && my <= boxY + boxH) {
            int clickedIdx = (int) ((my - boxY - 2) / 14);
            if (clickedIdx >= 0 && clickedIdx < currentMatches.size()) {
                selectedIndex = clickedIdx;
                applySelection(editBox);
                event.setCanceled(true);
            }
            return;
        }

        int pickerIdx = pickerCellAt(mx, my);
        if (pickerIdx >= 0) {
            insertShortcode(editBox, EmojiData.EMOJIS.get(pickerIdx));
            event.setCanceled(true);
        }
    }

    /**
     * Draws a small emoji picker on the right side of the chat screen while the
     * chat input is open. Clicking an entry inserts its shortcode into the input.
     */
    private static void drawEmojiPicker(GuiGraphics graphics, ChatScreen screen, EditBox editBox, double mouseX, double mouseY) {
        int panelH = EmojiData.EMOJIS.size() * PICKER_ITEM_HEIGHT + 4;
        int x = screen.width - PICKER_WIDTH - 4;
        int y = Math.max(4, editBox.getY() - panelH - 4);

        pickerX = x;
        pickerY = y;
        pickerH = panelH;

        int hovered = pickerCellAt(mouseX, mouseY);

        graphics.fill(x - 1, y - 1, x + PICKER_WIDTH + 1, y + panelH + 1, 0xD0000000);

        RenderSystem.enableBlend();

        for (int i = 0; i < EmojiData.EMOJIS.size(); i++) {
            EmojiData.EmojiEntry entry = EmojiData.EMOJIS.get(i);
            int itemY = y + 2 + i * PICKER_ITEM_HEIGHT;

            if (i == hovered) {
                graphics.fill(x, itemY, x + PICKER_WIDTH, itemY + PICKER_ITEM_HEIGHT, 0xFF0055AA);
            }

            graphics.blit(entry.texture(), x + 4, itemY + 2, 0, 0, 10, 10, 10, 10);
            graphics.drawString(Minecraft.getInstance().font, ":" + entry.code() + ":", x + 18, itemY + 3, i == hovered ? 0xFFFFFF : 0xAAAAAA);
        }

        RenderSystem.disableBlend();
    }

    /** Returns the picker row index under the given mouse position, or -1. */
    private static int pickerCellAt(double mouseX, double mouseY) {
        if (pickerH <= 0) return -1;
        if (mouseX < pickerX || mouseX >= pickerX + PICKER_WIDTH) return -1;

        int row = (int) ((mouseY - pickerY - 2) / PICKER_ITEM_HEIGHT);
        return (row >= 0 && row < EmojiData.EMOJIS.size()) ? row : -1;
    }

    /**
     * Inserts the shortcode of the picked emoji at the cursor. If the text right
     * before the cursor is an incomplete shortcode fragment (e.g. ":so"), that
     * fragment is replaced; otherwise the shortcode is inserted as-is, so users
     * can still type shortcodes manually.
     */
    private static void insertShortcode(EditBox editBox, EmojiData.EmojiEntry entry) {
        String text = editBox.getValue();
        int cursor = Math.max(0, Math.min(editBox.getCursorPosition(), text.length()));
        String before = text.substring(0, cursor);
        String after = text.substring(cursor);

        int lastColon = before.lastIndexOf(':');
        if (lastColon != -1) {
            String fragment = before.substring(lastColon + 1);
            boolean looksLikeFragment = fragment.chars().allMatch(Character::isLetterOrDigit);
            if (looksLikeFragment) {
                before = before.substring(0, lastColon);
            }
        }

        String insertion = ":" + entry.code() + ": ";
        editBox.setValue(before + insertion + after);
        editBox.setCursorPosition(before.length() + insertion.length());

        selectedIndex = 0;
        currentMatches.clear();
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

        for (EmojiData.EmojiEntry entry : EmojiData.EMOJIS) {
            if (entry.code().toLowerCase().startsWith(query.toLowerCase())) {
                currentMatches.add(entry);
            }
        }
    }

    private static void applySelection(EditBox editBox) {
        if (selectedIndex < 0 || selectedIndex >= currentMatches.size()) return;

        EmojiData.EmojiEntry selected = currentMatches.get(selectedIndex);
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