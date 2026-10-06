package com.emoji;

import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EmojiMod.MODID)
public class EmojiServerEvents {

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        String raw = event.getRawText();
        String replaced = replaceShortcodes(raw);

        if (!raw.equals(replaced)) {
            event.setMessage(Component.literal(replaced));
        }
    }

    @SubscribeEvent
    public static void onCommandExecute(CommandEvent event) {
        ParseResults<CommandSourceStack> parseResults = event.getParseResults();
        String command = parseResults.getReader().getString();

        if (command.startsWith("msg ") || command.startsWith("tell ") || command.startsWith("w ")
                || command.startsWith("/msg ") || command.startsWith("/tell ") || command.startsWith("/w ")) {

            String replaced = replaceShortcodes(command);

            if (!command.equals(replaced)) {
                CommandSourceStack source = parseResults.getContext().getSource();
                ParseResults<CommandSourceStack> newParseResults =
                        source.getServer().getCommands().getDispatcher().parse(replaced, source);
                event.setParseResults(newParseResults);
            }
        }
    }

    private static String replaceShortcodes(String input) {
        if (input == null || input.isEmpty()) return input;
        String result = input;

        for (EmojiData.EmojiEntry entry : EmojiData.EMOJIS) {
            String shortcode = ":" + entry.name() + ":";
            if (result.contains(shortcode)) {
                result = result.replace(shortcode, entry.codepoint());
            }
        }
        return result;
    }
}