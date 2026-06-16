package dev.kinau.myresourcepack;

import dev.kinau.myresourcepack.command.MyResourcePackCommand;
import dev.kinau.myresourcepack.config.ServerSettings;
import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.expander.ClientCommonPacketListenerImplExpander;
import dev.kinau.myresourcepack.expander.PackConfirmScreenExpander;
import dev.kinau.myresourcepack.expander.PackResourceExpander;
import dev.kinau.myresourcepack.expander.ServerDataExpander;
import dev.kinau.myresourcepack.screen.ResourceSelectionScreen;
import dev.kinau.myresourcepack.screen.components.buttons.ConfigButton;
import dev.kinau.myresourcepack.screen.components.buttons.Switch;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @Setter
    private boolean configuringPackOrder = false;
    @Setter
    private boolean shouldOpenConfigGui = false;
    private ServerData pendingServerData;
    private ClientConfigurationPacketListenerImpl pendingConnection;

    @Override
    public void onInitialize() {
        instance = this;
        this.packSettings = new PackSettings();
        ClientConfigurationConnectionEvents.INIT.register(this::setPendingConnection);
        ClientConfigurationConnectionEvents.COMPLETE.register(this::resetPendingConnection);
        ClientConfigurationConnectionEvents.DISCONNECT.register(this::resetPendingConnection);
        registerGui();
        registerCommand();
    }

    private void setPendingConnection(ClientConfigurationPacketListenerImpl handler, Minecraft client) {
        this.pendingConnection = handler;
        this.pendingServerData = ((ClientCommonPacketListenerImplExpander) handler).getServerData();
    }

    private void resetPendingConnection(ClientConfigurationPacketListenerImpl handler, Minecraft client) {
        this.pendingConnection = null;
        this.pendingServerData = null;
    }

    public ServerData getCurrentServerData() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isSingleplayer()) return null;
        ServerData serverData = minecraft.getCurrentServer();
        if (serverData == null) return pendingServerData;
        return serverData;
    }

    public String getCurrentServer() {
        ServerData serverData = getCurrentServerData();
        if (serverData == null) return null;
        return serverData.ip;
    }

    private void registerGui() {
        try {
            ScreenEvents.AFTER_INIT.register((_, screen, scaledWidth, scaledHeight) -> {
                if (!(screen instanceof PackSelectionScreen)) return;
                Minecraft minecraft = Screens.getMinecraft(screen);
                boolean hasServerPack = minecraft.getResourcePackRepository().getSelectedPacks().stream().anyMatch(pack -> pack.getPackSource() == PackSource.SERVER);
                if (!hasServerPack) return;
                String currentServer = getCurrentServer();
                if (currentServer == null) return;

                ServerSettings settings = packSettings.getConfigData().getSettings(currentServer);

                List<AbstractWidget> buttons = Screens.getWidgets(screen);

                ConfigButton configButton = new ConfigButton(scaledWidth - 20 - 5, scaledHeight - 20 - 6, 20, 20, !settings.overrideTextures()) {
                    @Override
                    public void onPress(@NonNull InputWithModifiers inputWithModifiers) {
                        pressConfigButton(minecraft);
                    }
                };
                Switch switchButton = (Switch) createToggle(minecraft, scaledWidth, settings, scaledHeight - 20 - 6, overrideTextures -> {
                    configButton.active = !overrideTextures;
                }, true);

                try {
                    buttons.removeIf(abstractWidget -> abstractWidget instanceof ConfigButton);
                    buttons.add(configButton);
                    buttons.removeIf(abstractWidget -> abstractWidget instanceof Switch);
                    buttons.add(switchButton);

                    buttons.forEach(abstractWidget -> {
                        if (abstractWidget instanceof Button button) {
                            if (button.getMessage().getContents() instanceof TranslatableContents translatableContents && translatableContents.getKey().equals("gui.done")) {
                                button.setX(scaledWidth / 2 - 50);
                                button.setWidth(Math.max(65, switchButton.getX() - button.getX() - 5));
                            } else if (button.getMessage().getContents() instanceof TranslatableContents translatableContents && translatableContents.getKey().equals("pack.openFolder")) {
                                button.setX(scaledWidth / 2 - button.getWidth() - 50 - 5);
                            }
                        }
                    });
                } catch (IndexOutOfBoundsException ex) {
                    // Lunarclient fails to add buttons to this screen
                }
            });
            ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, _) -> {
                if (!(screen instanceof ClientCommonPacketListenerImpl.PackConfirmScreen confirmScreen)) return;
                boolean required = confirmScreen.getTitle().getContents() instanceof TranslatableContents translatableContents
                        && translatableContents.getKey().startsWith("multiplayer.requiredTexturePrompt.line");

                String currentServer = getCurrentServer();
                if (currentServer == null) return;

                ServerSettings settings = packSettings.getConfigData().getSettings(currentServer);

                List<AbstractWidget> buttons = Screens.getWidgets(screen);
                buttons.removeIf(abstractWidget -> (abstractWidget instanceof Checkbox checkbox && checkbox.getMessage().getContents() instanceof TranslatableContents cbContents && cbContents.getKey().equals("override_textures_button")) ||
                        (abstractWidget instanceof Button button && button.getMessage().getContents() instanceof TranslatableContents buttonContents && buttonContents.getKey().equals("ignore_resource_pack")));
                buttons.forEach(abstractWidget -> {
                    abstractWidget.setPosition(abstractWidget.getX(), abstractWidget.getY() + 15);
                });

                int y = screen.children().getFirst().getRectangle().position().y() - 30;

                AbstractButton checkbox = createToggle(client, scaledWidth, settings, y, _ -> {}, false);
                buttons.add(checkbox);

                if (required) {
                    buttons.stream().filter(widget -> widget instanceof Button button
                                    && button.getMessage().getContents() instanceof TranslatableContents contents
                                    && contents.getKey().equals("gui.proceed"))
                            .findAny()
                            .ifPresent(proceedButton -> {
                                int newButtonY = proceedButton.getY() + proceedButton.getHeight() + 8;
                                buttons.add(Button.builder(Component.translatable("ignore_resource_pack"), _ -> {
                                    ClientCommonPacketListenerImpl packetListener = client.getConnection();
                                    if (packetListener == null) {
                                        packetListener = pendingConnection;
                                        if (packetListener == null) return;
                                    }
                                    if (!(confirmScreen instanceof PackConfirmScreenExpander packScreen)) return;

                                    ServerData serverData = getPendingServerData();
                                    if (serverData instanceof ServerDataExpander serverDataExpander) {
                                        serverData.setResourcePackStatus(ServerDataExpander.MappedPackStatus.IGNORED.getMappedStatus());
                                        serverDataExpander.myResourcePack$setPackStatus(ServerDataExpander.MappedPackStatus.IGNORED);
                                        ServerList.saveSingleServer(serverData);
                                    }

                                    for (ClientCommonPacketListenerImpl.PackConfirmScreen.PendingRequest pendingRequest : packScreen.getRequests()) {
                                        packetListener.send(new ServerboundResourcePackPacket(pendingRequest.id(), ServerboundResourcePackPacket.Action.ACCEPTED));
                                        packetListener.send(new ServerboundResourcePackPacket(pendingRequest.id(), ServerboundResourcePackPacket.Action.DOWNLOADED));
                                        packetListener.send(new ServerboundResourcePackPacket(pendingRequest.id(), ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED));
                                    }

                                    client.setScreen(packScreen.getParentScreen());
                                }).bounds(proceedButton.getX(), newButtonY, proceedButton.getWidth(), proceedButton.getHeight()).build());
                            });
                }
            });
        } catch (NoClassDefFoundError ex) {
            LOGGER.error("Couldn't register screen handler as Fabric Screen isn't installed", ex);
        }
    }

    @NotNull
    private AbstractButton createToggle(Minecraft minecraft, int scaledWidth, ServerSettings settings, int y, Consumer<Boolean> callBack, boolean reloadResources) {
        int height = 20;
        Consumer<Boolean> onPress = (pressed) -> {
            settings.overrideTextures(pressed);
            packSettings.saveConfigPrintError();
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
                ? new Switch(scaledWidth - width - 24 - 5, y, width, height, component, !settings.overrideTextures()) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                super.onPress(inputWithModifiers);
                callBack.accept(!enabled());
                onPress.accept(!enabled());
            }
        }
                : Checkbox.builder(component, minecraft.font)
                .pos(scaledWidth / 2 - (width / 2), y)
                .selected(settings.overrideTextures())
                .onValueChange((_, selected) -> {
                    callBack.accept(selected);
                    onPress.accept(selected);
                })
                .build();
    }

    private void pressConfigButton(Minecraft minecraft) {
        List<Pack> serverPacks = minecraft.getResourcePackRepository().getSelectedPacks().stream()
                .filter(pack -> pack.getPackSource() == PackSource.SERVER)
                .toList();
        if (serverPacks.isEmpty()) return;
        List<ResourceDirectory> packDirectories = new ArrayList<>();
        for (Pack pack : serverPacks) {
            ResourceDirectory root = new ResourceDirectory(Identifier.fromNamespaceAndPath("", ""));
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
        ResourceDirectory merged = packDirectories.getFirst();
        for (int i = 1; i < packDirectories.size(); i++) {
            merged = merged.merge(packDirectories.get(i));
        }
        minecraft.setScreen(new ResourceSelectionScreen(minecraft.screen, merged));
    }

    private void registerCommand() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (shouldOpenConfigGui) {
                this.shouldOpenConfigGui = false;
                pressConfigButton(client);
            }
        });
        ClientCommandRegistrationCallback.EVENT.register(new MyResourcePackCommand(this));
    }

}