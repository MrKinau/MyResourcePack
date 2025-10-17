package dev.kinau.myresourcepack.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.kinau.myresourcepack.MyResourcePack;
import dev.kinau.myresourcepack.config.ServerSetting;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

import java.io.IOException;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@RequiredArgsConstructor
public class MyResourcePackCommand implements ClientCommandRegistrationCallback {

    private final MyResourcePack myResourcePack;

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(literal("myresourcepack")
                .then(literal("enable")
                        .executes(context -> {
                            String currentServer = myResourcePack.getCurrentServer();
                            if (currentServer == null) return Command.SINGLE_SUCCESS;

                            ServerSetting setting = myResourcePack.getPackSettings().getConfigData().getSettings(currentServer);
                            setting.overrideTextures(false);
                            Minecraft.getInstance().reloadResourcePacks();
                            try {
                                myResourcePack.getPackSettings().saveConfig();
                                context.getSource().sendFeedback(Component.literal("Successfully enabled resource blocking!"));
                            } catch (IOException ex) {
                                MyResourcePack.LOGGER.error("Couldn't save config", ex);
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("disable")
                        .executes(context -> {
                            String currentServer = myResourcePack.getCurrentServer();
                            if (currentServer == null) return Command.SINGLE_SUCCESS;

                            ServerSetting setting = myResourcePack.getPackSettings().getConfigData().getSettings(currentServer);
                            setting.overrideTextures(true);
                            Minecraft.getInstance().reloadResourcePacks();
                            try {
                                myResourcePack.getPackSettings().saveConfig();
                                context.getSource().sendFeedback(Component.literal("Successfully disabled resource blocking!"));
                            } catch (IOException ex) {
                                MyResourcePack.LOGGER.error("Couldn't save config", ex);
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("settings")
                        .executes(context -> {
                            String currentServer = myResourcePack.getCurrentServer();
                            if (currentServer == null) return Command.SINGLE_SUCCESS;

                            ServerSetting setting = myResourcePack.getPackSettings().getConfigData().getSettings(currentServer);
                            if (setting.overrideTextures()) {
                                context.getSource().sendFeedback(Component.literal("You need to enable resource blocking first: /myresourcepack enable!"));
                                return Command.SINGLE_SUCCESS;
                            }

                            myResourcePack.setShouldOpenConfigGui(true);

                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
