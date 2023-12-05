package dev.kinau.myresourcepack.screen.components;

import com.mojang.blaze3d.systems.RenderSystem;
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
    private static final ResourceLocation CONFIG_DISABLED_SPRITE = new ResourceLocation( "myresourcepack", "widget/config_disabled");
    private static final ResourceLocation CONFIG_HIGHLIGHTED_SPRITE = new ResourceLocation("myresourcepack", "widget/config_highlighted");
    private static final ResourceLocation CONFIG_SPRITE = new ResourceLocation("myresourcepack", "widget/config");

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
        RenderSystem.enableDepthTest();
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        ResourceLocation resourceLocation = getResource();
        guiGraphics.blitSprite(resourceLocation, this.getX(), this.getY(), this.width, this.height);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
