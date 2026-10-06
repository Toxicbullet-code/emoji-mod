package com.emoji;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.ResourceLocation;
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

    // Autocomplete suggestion box drawn above the chat input
    private static final int SUGGESTION_HEIGHT = 16;
    private static final int SUGGESTION_ICON = 14;

    // Emoji toggle button, drawn at the right end just above the chat input
    private static final int BUTTON_SIZE = 18;
    private static final int BUTTON_ICON = 16;

    // Emoji tab that opens above the button while active
    private static final int CELL = 22;
    private static final int CELL_ICON = 20;
    private static final int PANEL_COLUMNS = 5;
    private static final int PANEL_PADDING = 4;
    private static final int PANEL_TITLE_HEIGHT = 14;

    private static boolean panelOpen = false;
    private static int buttonX, buttonY;
    private static int panelX, panelY, panelW, panelH;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        panelOpen = false;
        currentMatches.clear();
        selectedIndex = 0;
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof ChatScreen chatScreen)) return;

        EditBox editBox = getEditBox(chatScreen);
        if (editBox == null) return;

        GuiGraphics graphics = event.getGuiGraphics();

        updateMatches(editBox);
        updateLayout(chatScreen, editBox);

        if (panelOpen) {
            drawPanel(graphics, event.getMouseX(), event.getMouseY());
        }
        drawButton(graphics, event.getMouseX(), event.getMouseY());
        if (!currentMatches.isEmpty()) {
            drawSuggestions(graphics, editBox, event.getMouseX(), event.getMouseY());
        }
    }

    /** Computes where the toggle button and the emoji tab sit on this screen. */
    private static void updateLayout(ChatScreen screen, EditBox editBox) {
        buttonX = screen.width - BUTTON_SIZE - 4;
        buttonY = editBox.getY() - BUTTON_SIZE - 2;

        int rows = (EmojiData.EMOJIS.size() + PANEL_COLUMNS - 1) / PANEL_COLUMNS;
        panelW = PANEL_PADDING * 2 + PANEL_COLUMNS * CELL;
        panelH = PANEL_TITLE_HEIGHT + PANEL_PADDING * 2 + rows * CELL;
        panelX = screen.width - panelW - 4;
        panelY = buttonY - panelH - 2;

        if (panelX < 4) panelX = 4;
        if (panelY < 4) panelY = 4;
    }

    /** Draws the ":" autocomplete suggestions above the chat input. */
    private static void drawSuggestions(GuiGraphics graphics, EditBox editBox, double mouseX, double mouseY) {
        if (selectedIndex >= currentMatches.size()) selectedIndex = 0;

        boxW = 150;
        boxH = currentMatches.size() * SUGGESTION_HEIGHT + 4;
        boxX = editBox.getX() + 2;
        boxY = editBox.getY() - boxH - 4;

        graphics.fill(boxX - 1, boxY - 1, boxX + boxW + 1, boxY + boxH + 1, 0xD0000000);

        RenderSystem.enableBlend();

        for (int i = 0; i < currentMatches.size(); i++) {
            EmojiData.EmojiEntry entry = currentMatches.get(i);
            int itemY = boxY + 2 + (i * SUGGESTION_HEIGHT);

            if (i == selectedIndex) {
                graphics.fill(boxX, itemY, boxX + boxW, itemY + SUGGESTION_HEIGHT, 0xFF0055AA);
            }

            drawEmoji(graphics, entry.texture(), boxX + 3, itemY + 1, SUGGESTION_ICON);
            graphics.drawString(Minecraft.getInstance().font, ":" + entry.name() + ":",
                    boxX + 21, itemY + 4, i == selectedIndex ? 0xFFFFFF : 0xAAAAAA);
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

        // Autocomplete popup priority
        if (!currentMatches.isEmpty() && inRect(mx, my, boxX, boxY, boxW, boxH)) {
            int clickedIdx = (int) ((my - boxY - 2) / SUGGESTION_HEIGHT);
            if (clickedIdx >= 0 && clickedIdx < currentMatches.size()) {
                selectedIndex = clickedIdx;
                applySelection(editBox);
                event.setCanceled(true);
            }
            return;
        }

        // Toggle button logic
        if (inRect(mx, my, buttonX, buttonY, BUTTON_SIZE, BUTTON_SIZE)) {
            panelOpen = !panelOpen;
            event.setCanceled(true);
            return;
        }

        if (panelOpen) {
            int index = panelCellAt(mx, my);
            if (index >= 0) {
                event.setCanceled(true);
                insertShortcode(editBox, EmojiData.EMOJIS.get(index));
                return;
            }

            if (inRect(mx, my, panelX, panelY, panelW, panelH)) {
                event.setCanceled(true);
                return;
            }

            panelOpen = false;
        }
    }

    /** Draws the emoji toggle button sitting just above the chat input. */
    private static void drawButton(GuiGraphics graphics, double mouseX, double mouseY) {
        boolean hover = inRect(mouseX, mouseY, buttonX, buttonY, BUTTON_SIZE, BUTTON_SIZE);

        graphics.fill(buttonX - 1, buttonY - 1, buttonX + BUTTON_SIZE + 1, buttonY + BUTTON_SIZE + 1, 0xD0000000);

        int background = panelOpen ? 0xFF0055AA : (hover ? 0xF03868A8 : 0xC0181818);
        graphics.fill(buttonX, buttonY, buttonX + BUTTON_SIZE, buttonY + BUTTON_SIZE, background);

        int iconOffset = (BUTTON_SIZE - BUTTON_ICON) / 2;
        EmojiData.EmojiEntry defaultIcon = EmojiData.getByName("smiley");
        if (defaultIcon != null) {
            drawEmoji(graphics, defaultIcon.texture(), buttonX + iconOffset, buttonY + iconOffset, BUTTON_ICON);
        }
    }

    /** Draws the emoji grid panel above the toggle button. */
    private static void drawPanel(GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.fill(panelX - 1, panelY - 1, panelX + panelW + 1, panelY + panelH + 1, 0xD0000000);
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xF0101010);
        graphics.fill(panelX, panelY, panelX + panelW, panelY + PANEL_TITLE_HEIGHT, 0xFF2A2A2A);

        RenderSystem.enableBlend();

        graphics.drawString(Minecraft.getInstance().font, "Emojis",
                panelX + PANEL_PADDING + 1, panelY + 3, 0xFFD0D0D0);

        int hovered = panelCellAt(mouseX, mouseY);

        for (int i = 0; i < EmojiData.EMOJIS.size(); i++) {
            EmojiData.EmojiEntry entry = EmojiData.EMOJIS.get(i);
            int cellX = panelX + PANEL_PADDING + (i % PANEL_COLUMNS) * CELL;
            int cellY = panelY + PANEL_TITLE_HEIGHT + PANEL_PADDING + (i / PANEL_COLUMNS) * CELL;

            if (i == hovered) {
                graphics.fill(cellX, cellY, cellX + CELL, cellY + CELL, 0xFF0055AA);
            }

            int iconOffset = (CELL - CELL_ICON) / 2;
            drawEmoji(graphics, entry.texture(), cellX + iconOffset, cellY + iconOffset, CELL_ICON);
        }

        RenderSystem.disableBlend();
    }

    /** Returns the emoji tab cell index under the given mouse position, or -1. */
    private static int panelCellAt(double mouseX, double mouseY) {
        if (!panelOpen) return -1;
        if (!inRect(mouseX, mouseY, panelX, panelY, panelW, panelH)) return -1;

        int localX = (int) mouseX - panelX - PANEL_PADDING;
        int localY = (int) mouseY - panelY - PANEL_TITLE_HEIGHT - PANEL_PADDING;

        if (localX < 0 || localY < 0) return -1;

        int column = localX / CELL;
        int row = localY / CELL;

        if (column >= PANEL_COLUMNS || column < 0) return -1;

        int index = row * PANEL_COLUMNS + column;
        return (index >= 0 && index < EmojiData.EMOJIS.size()) ? index : -1;
    }

    private static boolean inRect(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /** Draws a full emoji texture scaled into a size x size box. */
    private static void drawEmoji(GuiGraphics graphics, ResourceLocation texture, int x, int y, int size) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(texture, x, y, 0, 0, size, size, size, size);
        RenderSystem.disableBlend();
    }

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

        String insertion = ":" + entry.name() + ": ";
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
            if (entry.name().toLowerCase().startsWith(query.toLowerCase())) {
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
            String replacement = ":" + selected.name() + ": ";

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