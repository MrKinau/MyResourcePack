package dev.kinau.myresourcepack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.kinau.myresourcepack.config.Config;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

public class PackSettings {

    private final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final File CONFIG_FOLDER = new File("config/MyResourcePack");
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
        if (!CONFIG_FOLDER.exists()) {
            if (!CONFIG_FOLDER.mkdirs()) throw new IOException(String.format("Could not create folder %s", CONFIG_FOLDER));
        }
        this.configFile = new File(CONFIG_FOLDER, "config.json");
        if (configFile.exists()) {
            this.configData = GSON.fromJson(new FileReader(configFile), Config.class);
        } else {
            this.configData = new Config();
        }
    }

    public void saveConfig() throws IOException {
        if (configData == null) return;
        String data = GSON.toJson(configData);
        Files.writeString(configFile.toPath(), data);
    }
}
