package dev.kinau.myresourcepack.screen.components;

import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Getter
public class ExpandButton extends AbstractButton {

    private static final ResourceLocation BUTTON_FOLD_HIGHLIGHTED_SPRITE = new ResourceLocation("myresourcepack", "textures/gui/sprites/widget/fold_highlighted.png");
    private static final ResourceLocation BUTTON_FOLD_SPRITE = new ResourceLocation( "myresourcepack", "textures/gui/sprites/widget/fold.png");
    private static final ResourceLocation BUTTON_EXPAND_HIGHLIGHTED_SPRITE = new ResourceLocation("myresourcepack", "textures/gui/sprites/widget/expand_highlighted.png");
    private static final ResourceLocation BUTTON_EXPAND_SPRITE = new ResourceLocation("myresourcepack", "textures/gui/sprites/widget/expand.png");

    private boolean expanded;

    public ExpandButton(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    @Override
    public void onPress() {
        this.expanded = !expanded;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    private ResourceLocation getResource() {
        if (expanded)
            return isHovered() ? BUTTON_FOLD_HIGHLIGHTED_SPRITE : BUTTON_FOLD_SPRITE;
        return isHovered() ? BUTTON_EXPAND_HIGHLIGHTED_SPRITE : BUTTON_EXPAND_SPRITE;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        ResourceLocation resourceLocation = getResource();
        this.renderTexture(guiGraphics, resourceLocation, this.getX(), this.getY(), 0, 0, 0, this.width, this.height, this.width, this.height);
    }
}