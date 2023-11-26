package dev.kinau.myresourcepack.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class ServerSetting {
    private String ip;
    private boolean overrideTextures;
}
