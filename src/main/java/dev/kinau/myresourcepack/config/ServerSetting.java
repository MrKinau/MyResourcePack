package dev.kinau.myresourcepack.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class ServerSetting {
    private String ip;
    private boolean overrideTextures;
    private Map<String, Integer> packOrder = new HashMap<>();
    private List<ResourceRule> overrideRules = new ArrayList<>();
    private List<ResourceRule> additionRules = new ArrayList<>();
}
