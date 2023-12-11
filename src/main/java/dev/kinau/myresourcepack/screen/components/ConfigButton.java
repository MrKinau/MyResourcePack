package dev.kinau.myresourcepack.screen.components;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Getter
@Setter
@Accessors(fluent = true)
public abstract class ConfigButton extends AbstractButton {
    private static final ResourceLocation CONFIG_DISABLED_SPRITE = new ResourceLocation( "myresourcepack", "textures/gui/sprites/widget/config_disabled.png");
    private static final ResourceLocation CONFIG_HIGHLIGHTED_SPRITE = new ResourceLocation("myresourcepack", "textures/gui/sprites/widget/config_highlighted.png");
    private static final ResourceLocation CONFIG_SPRITE = new ResourceLocation("myresourcepack", "textures/gui/sprites/widget/config.png");

    public ConfigButton(int x, int y, int width, int height, boolean enabled) {
        super(x, y, width, height, Component.empty());
        this.active = enabled;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    private ResourceLocation getResource() {
        if (isActive())
            return isHovered() ? CONFIG_HIGHLIGHTED_SPRITE : CONFIG_SPRITE;
        return CONFIG_DISABLED_SPRITE;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        ResourceLocation resourceLocation = getResource();
        this.renderTexture(guiGraphics, resourceLocation, this.getX(), this.getY(), 0, 0, 0, this.width, this.height, this.width, this.height);
    }
}
