package dev.kinau.myresourcepack.screen.components;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Getter
@Accessors(fluent = true)
public class Switch extends AbstractButton {
    private static final ResourceLocation SWITCH_DISABLED_HIGHLIGHTED_SPRITE = new ResourceLocation("myresourcepack", "textures/gui/sprites/widget/disabled_highlighted.png");
    private static final ResourceLocation SWITCH_DISABLED_SPRITE = new ResourceLocation( "myresourcepack", "textures/gui/sprites/widget/disabled.png");
    private static final ResourceLocation SWITCH_ENABLED_HIGHLIGHTED_SPRITE = new ResourceLocation("myresourcepack", "textures/gui/sprites/widget/enabled_highlighted.png");
    private static final ResourceLocation SWITCH_ENABLED_SPRITE = new ResourceLocation("myresourcepack", "textures/gui/sprites/widget/enabled.png");
    private static final int TEXT_COLOR = 0xE0E0E0;

    private boolean enabled;

    public Switch(int x, int y, int width, int height, Component component, boolean enabled) {
        super(x, y, width, height, component);
        this.enabled = enabled;
        setTooltip(Tooltip.create(Component.translatable("enable_resource_blocking_description")));
    }

    @Override
    public void onPress() {
        this.enabled = !enabled;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    private ResourceLocation getResource() {
        if (enabled)
            return isHovered() ? SWITCH_ENABLED_HIGHLIGHTED_SPRITE : SWITCH_ENABLED_SPRITE;
        return isHovered() ? SWITCH_DISABLED_HIGHLIGHTED_SPRITE : SWITCH_DISABLED_SPRITE;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        ResourceLocation resourceLocation = getResource();
        this.renderTexture(guiGraphics, resourceLocation, this.getX(), this.getY(), 0, 0, 0, 60, this.height, 60, this.height);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.drawString(font, this.getMessage(), this.getX() + 60 + 4, this.getY() + (this.height - 8) / 2, TEXT_COLOR | Mth.ceil(this.alpha * 255.0f) << 24);
    }
}