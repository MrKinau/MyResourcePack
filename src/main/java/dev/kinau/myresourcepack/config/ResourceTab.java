package dev.kinau.myresourcepack.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.chat.Component;

@Getter
@RequiredArgsConstructor
public enum ResourceTab {
    OVERRIDE(Component.translatable("resourceSelectionScreen.tab.overridden.title")),
    ADDITION(Component.translatable("resourceSelectionScreen.tab.additional.title")),
    ;

    private final Component title;
}
