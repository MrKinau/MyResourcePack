package dev.kinau.myresourcepack.screen.components.buttons;

import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

@Getter
public class ExpandButton extends AbstractButton {

    private static final ResourceLocation BUTTON_FOLD_HIGHLIGHTED_SPRITE = ResourceLocation.fromNamespaceAndPath("myresourcepack", "widget/fold_highlighted");
    private static final ResourceLocation BUTTON_FOLD_SPRITE = ResourceLocation.fromNamespaceAndPath( "myresourcepack", "widget/fold");
    private static final ResourceLocation BUTTON_EXPAND_HIGHLIGHTED_SPRITE = ResourceLocation.fromNamespaceAndPath("myresourcepack", "widget/expand_highlighted");
    private static final ResourceLocation BUTTON_EXPAND_SPRITE = ResourceLocation.fromNamespaceAndPath("myresourcepack", "widget/expand");

    private boolean expanded;

    public ExpandButton(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        this.expanded = !expanded;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    private ResourceLocation getResource() {
        if (expanded)
            return isHoveredOrFocused() ? BUTTON_FOLD_HIGHLIGHTED_SPRITE : BUTTON_FOLD_SPRITE;
        return isHoveredOrFocused() ? BUTTON_EXPAND_HIGHLIGHTED_SPRITE : BUTTON_EXPAND_SPRITE;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        ResourceLocation resourceLocation = getResource();
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha));
    }
}