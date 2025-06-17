package dev.kinau.myresourcepack.screen.components;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

@Getter
@Setter
@Accessors(fluent = true)
public abstract class ConfigButton extends AbstractButton {
    private static final ResourceLocation CONFIG_DISABLED_SPRITE = ResourceLocation.fromNamespaceAndPath("myresourcepack", "widget/config_disabled");
    private static final ResourceLocation CONFIG_HIGHLIGHTED_SPRITE = ResourceLocation.fromNamespaceAndPath("myresourcepack", "widget/config_highlighted");
    private static final ResourceLocation CONFIG_SPRITE = ResourceLocation.fromNamespaceAndPath("myresourcepack", "widget/config");

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
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, this.getX(), this.getY(), this.width, this.height, ARGB.white(this.alpha));
    }
}
