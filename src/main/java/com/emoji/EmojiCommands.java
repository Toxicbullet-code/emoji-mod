package com.emoji;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EmojiData.MODID, value = Dist.CLIENT)
public class EmojiCommands {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("emoji")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(
                            () -> Component.literal("§a[Emoji Mod] §fType shortcodes like §e:sob:§f directly in chat!"),
                            false
                    );
                    return 1;
                });

        for (EmojiData.EmojiEntry entry : EmojiData.EMOJIS) {
            root.then(Commands.literal(entry.name())
                    .executes(ctx -> {
                        ctx.getSource().sendSuccess(
                                () -> Component.literal("§a[Emoji] §fRegistered shortcode: :" + entry.name() + ":"),
                                false
                        );
                        return 1;
                    })
            );
        }

        event.getDispatcher().register(root);
    }
}