package dev.kinau.myresourcepack.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class Config {

    private List<ServerSetting> serverSettings = new ArrayList<>();

    public ServerSetting getSettings(String server) {
        Optional<ServerSetting> optSetting = serverSettings.stream()
                .filter(serverSetting -> serverSetting.ip().equals(server)).findAny();
        if (optSetting.isPresent())
            return optSetting.get();
        ServerSetting setting = new ServerSetting(server, true, new HashMap<>(), new ArrayList<>(), new ArrayList<>());
        serverSettings.add(setting);
        return setting;
    }
}
