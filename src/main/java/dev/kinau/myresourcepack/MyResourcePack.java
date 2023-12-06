package dev.kinau.myresourcepack;

import dev.kinau.myresourcepack.config.ServerSetting;
import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.expander.ClientCommonPacketListenerImplExpander;
import dev.kinau.myresourcepack.expander.PackConfirmScreenExpander;
import dev.kinau.myresourcepack.expander.PackResourceExpander;
import dev.kinau.myresourcepack.screen.ResourceSelectionScreen;
import dev.kinau.myresourcepack.screen.components.ConfigButton;
import dev.kinau.myresourcepack.screen.components.Switch;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class MyResourcePack implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("myresourcepack");
    @Getter
    private static MyResourcePack instance;

    private PackSettings packSettings;
    @Setter
    private boolean reloadResources = false;
    private ServerData pendingServerData;
    private ClientConfigurationPacketListenerImpl pendingConnection;

    @Override
    public void onInitialize() {
        instance = this;
        this.packSettings = new PackSettings();
        ClientConfigurationConnectionEvents.INIT.register(this::setPendingConnection);
        ClientConfigurationConnectionEvents.READY.register(this::resetPendingConnection);
        ClientConfigurationConnectionEvents.DISCONNECT.register(this::resetPendingConnection);
        registerGui();
    }

    private void setPendingConnection(ClientConfigurationPacketListenerImpl handler, Minecraft client) {
        this.pendingConnection = handler;
        this.pendingServerData = ((ClientCommonPacketListenerImplExpander)handler).getServerData();
    }

    private void resetPendingConnection(ClientConfigurationPacketListenerImpl handler, Minecraft client) {
        this.pendingConnection = null;
        this.pendingServerData = null;
    }

    public String getCurrentServer() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isSingleplayer()) return null;
        ServerData serverData = minecraft.getCurrentServer();
        if (serverData == null) {
            if (pendingServerData == null)
                return null;
            return pendingServerData.ip;
        }
        return serverData.ip;
    }

    private void registerGui() {
        try {
            ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                if (!(screen instanceof PackSelectionScreen)) return;
                Minecraft minecraft = Screens.getClient(screen);
                boolean hasServerPack = minecraft.getResourcePackRepository().getSelectedPacks().stream().anyMatch(pack -> pack.getPackSource() == PackSource.SERVER);
                if (!hasServerPack) return;
                String currentServer = getCurrentServer();
                if (currentServer == null) return;

                ServerSetting setting = packSettings.getConfigData().getSettings(currentServer);

                List<AbstractWidget> buttons = Screens.getButtons(screen);
                ConfigButton configButton;
                buttons.add(configButton = new ConfigButton(scaledWidth / 2 + 184, scaledHeight - 20 - 5, 20, 20, !setting.overrideTextures()) {
                    @Override
                    public void onPress() {
                        List<Pack> serverPacks = minecraft.getResourcePackRepository().getSelectedPacks().stream()
                                .filter(pack -> pack.getPackSource() == PackSource.SERVER)
                                .toList();
                        if (serverPacks.isEmpty()) return;
                        List<ResourceDirectory> packDirectories = new ArrayList<>();
                        for (Pack pack : serverPacks) {
                            ResourceDirectory root = new ResourceDirectory(new ResourceLocation("", ""));
                            try (PackResources packResources = pack.open()) {
                                packResources.getNamespaces(PackType.CLIENT_RESOURCES).forEach(namespace -> {
                                    if (packResources instanceof CompositePackResources || packResources instanceof FilePackResources) {
                                        ResourceDirectory directory = ((PackResourceExpander) packResources).myResourcePack$createResourceTree(PackType.CLIENT_RESOURCES, namespace);
                                        root.addChild(directory);
                                    }
                                });
                            }
                            packDirectories.add(root);
                        }
                        ResourceDirectory merged = packDirectories.get(0);
                        for (int i = 1; i < packDirectories.size(); i++) {
                            merged = merged.merge(packDirectories.get(i));
                        }
                        minecraft.setScreen(new ResourceSelectionScreen(minecraft.screen, merged));
                    }
                });
                Switch switchButton = (Switch) createToggle(minecraft, scaledWidth, setting, scaledHeight - 20 - 5, overrideTextures -> {
                    configButton.active = !overrideTextures;
                }, true);
                buttons.add(switchButton);
            });
            ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                if (!(screen instanceof ClientCommonPacketListenerImpl.PackConfirmScreen confirmScreen)) return;
                boolean required = confirmScreen.getTitle().getContents() instanceof TranslatableContents translatableContents
                        && translatableContents.getKey().startsWith("multiplayer.requiredTexturePrompt.line");

                String currentServer = getCurrentServer();
                if (currentServer == null) return;

                ServerSetting setting = packSettings.getConfigData().getSettings(currentServer);

                List<AbstractWidget> buttons = Screens.getButtons(screen);
                buttons.forEach(abstractWidget -> {
                    abstractWidget.setPosition(abstractWidget.getX(), abstractWidget.getY() + 15);
                });

                int y = screen.children().get(0).getRectangle().position().y() - 30;

                AbstractButton checkbox = createToggle(client, scaledWidth, setting, y, a -> {
                }, false);
                buttons.add(checkbox);

                if (required) {
                    buttons.stream().filter(widget -> widget instanceof Button button
                                    && button.getMessage().getContents() instanceof TranslatableContents contents
                                    && contents.getKey().equals("gui.proceed"))
                            .findAny()
                            .ifPresent(proceedButton -> {
                                int newButtonY = proceedButton.getY() + proceedButton.getHeight() + 8;
                                buttons.add(Button.builder(Component.translatable("ignore_resource_pack"), button -> {
                                    ClientCommonPacketListenerImpl packetListener = client.getConnection();
                                    if (packetListener == null) {
                                        packetListener = pendingConnection;
                                        if (packetListener == null) return;
                                    }
                                    if (!(confirmScreen instanceof PackConfirmScreenExpander packScreen)) return;

                                    // not changing this may cause issues, but this is just meant to be a temporary ignore feature
//                                    minecraft.getCurrentServer().setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);

                                    for (ClientCommonPacketListenerImpl.PackConfirmScreen.PendingRequest pendingRequest : packScreen.getRequests()) {
                                        packetListener.send(new ServerboundResourcePackPacket(pendingRequest.id(), ServerboundResourcePackPacket.Action.ACCEPTED));
                                        packetListener.send(new ServerboundResourcePackPacket(pendingRequest.id(), ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED));
                                    }

                                    client.setScreen(((PackConfirmScreenExpander)confirmScreen).getParentScreen());
                                }).bounds(scaledWidth / 2 - 155, newButtonY, 150, 20).build());
                            });
                }
            });
        } catch (NoClassDefFoundError ex) {
            LOGGER.error("Couldn't register screen handler as Fabric Screen isn't installed", ex);
        }
    }

    @NotNull
    private AbstractButton createToggle(Minecraft minecraft, int scaledWidth, ServerSetting setting, int y, Consumer<Boolean> callBack, boolean reloadResources) {
        int height = 20;
        Consumer<Boolean> onPress = (pressed) -> {
            setting.overrideTextures(pressed);
            try {
                packSettings.saveConfig();
            } catch (IOException ex) {
                LOGGER.error("Couldn't save config", ex);
            }
            if (reloadResources)
                MyResourcePack.this.reloadResources = !MyResourcePack.this.reloadResources;
        };
        Component component = reloadResources
                ? Component.translatable("enable_resource_blocking")
                : Component.translatable("override_textures_button");
        int width = reloadResources
                ? 60 + 4 + minecraft.font.width(component)
                : 20 + 4 + minecraft.font.width(component);
        return reloadResources
                ? new Switch(scaledWidth / 2 + 4, y, width, height, component, !setting.overrideTextures()) {
            @Override
            public void onPress() {
                super.onPress();
                callBack.accept(!enabled());
                onPress.accept(!enabled());
            }
        }
                : Checkbox.builder(component, minecraft.font)
                .pos(scaledWidth / 2 - (width / 2), y)
                .selected(setting.overrideTextures())
                .onValueChange((checkbox, selected) -> {
                    callBack.accept(selected);
                    onPress.accept(selected);
                })
                .build();
    }

}