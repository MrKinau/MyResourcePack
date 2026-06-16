package dev.kinau.myresourcepack.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class Config {

    private List<ServerSettings> serverSettings = new ArrayList<>();

    public ServerSettings getSettings(String server) {
        return getSettings(server, true);
    }

    public ServerSettings getSettings(String server, boolean createIfMissing) {
        Optional<ServerSettings> optSetting = serverSettings.stream()
                .filter(serverSetting -> serverSetting.ip().equals(server)).findAny();
        if (optSetting.isPresent())
            return optSetting.get();
        if (!createIfMissing) return null;
        ServerSettings setting = new ServerSettings(server, false, true, new HashMap<>(), new ArrayList<>(), new ArrayList<>());
        serverSettings.add(setting);
        return setting;
    }
}
