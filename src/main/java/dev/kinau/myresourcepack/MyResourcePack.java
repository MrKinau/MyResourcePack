package dev.kinau.myresourcepack;

import dev.kinau.myresourcepack.config.ServerSetting;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
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
				if (!(screen instanceof PackSelectionScreen packSelectionScreen)) return;
				if (!Minecraft.getInstance().getResourcePackRepository().isAvailable("server")) return;
				String currentServer = getCurrentServer();
				if (currentServer == null) return;

				ServerSetting setting = packSettings.getConfigData().getSettings(currentServer);

				int width = 200;
				int height = 20;
				Checkbox checkbox = new Checkbox(scaledWidth / 2 - 60, scaledHeight - height - 5, width, height, Component.translatable("override_textures_button"), setting.overrideTextures(), true) {
					@Override
					public void onPress() {
						super.onPress();
						setting.overrideTextures(selected());
						try {
							packSettings.saveConfig();
						} catch (IOException ex) {
							LOGGER.error("Couldn't save config", ex);
						}
						reloadResources = true;
					}
				};
				Screens.getButtons(screen).add(checkbox);
			});
		} catch (NoClassDefFoundError ex) {
			LOGGER.error("Couldn't register screen handler as Fabric Screen isn't installed", ex);
		}
	}
}