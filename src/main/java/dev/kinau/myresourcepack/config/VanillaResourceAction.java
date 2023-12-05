package dev.kinau.myresourcepack.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class VanillaResourceAction {
    private final ResourceAction action;
    private final boolean overridesVanilla;
}
