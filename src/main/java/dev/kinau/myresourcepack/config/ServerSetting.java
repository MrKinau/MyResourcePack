package dev.kinau.myresourcepack.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class ServerSetting {
    private String ip;
    private boolean overrideTextures;
    private List<ResourceRule> overrideRules = new ArrayList<>();
    private List<ResourceRule> additionRules = new ArrayList<>();
}
