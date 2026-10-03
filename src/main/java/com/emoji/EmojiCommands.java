package com.emoji;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = EmojiMod.MODID, value = Dist.CLIENT)
public class EmojiCommands {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("emoji")
            .executes(ctx -> {
                ctx.getSource().sendSuccess(
                    () -> Component.literal("§a[Emoji Mod] §fType shortcodes like §e:sob:§f or §e:fire:§f directly in chat!"), 
                    false
                );
                return 1;
            });

        for (Map.Entry<String, String> entry : EmojiData.EMOJIS.entrySet()) {
            String shortName = entry.getKey().replace(":", "");
            String unicode = entry.getValue();

            root.then(Commands.literal(shortName)
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("§a[Emoji] §f" + entry.getKey() + " -> " + unicode), 
                        false
                    );
                    return 1;
                })
            );
        }

        event.getDispatcher().register(root);
    }
}
