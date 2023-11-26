package dev.kinau.myresourcepack;

import dev.kinau.myresourcepack.config.ServerSetting;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Getter
public class MyResourcePack implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("myresourcepack");
	@Getter
	private static MyResourcePack instance;

	private PackSettings packSettings;
	@Setter
	private boolean reloadResources = false;

	@Override
	public void onInitialize() {
		instance = this;
		this.packSettings = new PackSettings();
		registerGui();
	}

	public String getCurrentServer() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.isSingleplayer()) return null;
		ServerData serverData = minecraft.getCurrentServer();
		if (serverData == null) return null;
		return serverData.ip;
	}

	private void registerGui() {
		try {
			ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
				if (!(screen instanceof PackSelectionScreen)) return;
				if (!Minecraft.getInstance().getResourcePackRepository().isAvailable("server")) return;
				String currentServer = getCurrentServer();
				if (currentServer == null) return;

				ServerSetting setting = packSettings.getConfigData().getSettings(currentServer);

				Checkbox checkbox = createCheckbox(scaledWidth, setting, scaledHeight - 20 - 5, true);
				Screens.getButtons(screen).add(checkbox);
			});
			ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
				if (!(screen instanceof ConfirmScreen confirmScreen)) return;
				if (!(confirmScreen.getTitle().getContents() instanceof TranslatableContents translatableContents
						&& translatableContents.getKey().startsWith("multiplayer.requiredTexturePrompt.line"))) return;
				String currentServer = getCurrentServer();
				if (currentServer == null) return;

				ServerSetting setting = packSettings.getConfigData().getSettings(currentServer);

				Screens.getButtons(screen).forEach(abstractWidget -> {
					abstractWidget.setPosition(abstractWidget.getX(), abstractWidget.getY() + 15);
				});

				int y = screen.children().get(0).getRectangle().position().y() - 25;

				Checkbox checkbox = createCheckbox(scaledWidth, setting, y, false);
				Screens.getButtons(screen).add(checkbox);
			});
		} catch (NoClassDefFoundError ex) {
			LOGGER.error("Couldn't register screen handler as Fabric Screen isn't installed", ex);
		}
	}

	@NotNull
	private Checkbox createCheckbox(int scaledWidth, ServerSetting setting, int y, boolean reloadResources) {
		int width = 200;
		int height = 20;
        return new Checkbox(scaledWidth / 2 - (width / 2) + 10, y, width, height, Component.translatable("override_textures_button"), setting.overrideTextures(), true) {
			@Override
			public void onPress() {
				super.onPress();
				setting.overrideTextures(selected());
				try {
					packSettings.saveConfig();
				} catch (IOException ex) {
					LOGGER.error("Couldn't save config", ex);
				}
				MyResourcePack.this.reloadResources = reloadResources;
			}
		};
	}
}