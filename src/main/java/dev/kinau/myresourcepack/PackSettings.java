package dev.kinau.myresourcepack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.kinau.myresourcepack.config.Config;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

public class PackSettings {

    private final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final File CONFIG_FOLDER = FabricLoader.getInstance().getConfigDir().resolve("MyResourcePack").toFile();
    private File configFile;

    @Getter
    private Config configData;

    public PackSettings() {
        try {
            loadConfig();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadConfig() throws IOException {
        this.configData = new Config();
        if (!CONFIG_FOLDER.exists()) {
            if (!CONFIG_FOLDER.mkdirs()) throw new IOException(String.format("Could not create folder %s", CONFIG_FOLDER));
        }
        this.configFile = new File(CONFIG_FOLDER, "config.json");
        if (configFile.exists()) {
            this.configData = GSON.fromJson(new FileReader(configFile), Config.class);
        }
    }

    public boolean saveConfigPrintError() {
        try {
            saveConfig();
            return true;
        } catch (IOException ex) {
            MyResourcePack.LOGGER.error("Could not save config", ex);
        }
        return false;
    }

    public void saveConfig() throws IOException {
        if (configData == null) return;
        String data = GSON.toJson(configData);
        Files.writeString(configFile.toPath(), data);
    }
}
