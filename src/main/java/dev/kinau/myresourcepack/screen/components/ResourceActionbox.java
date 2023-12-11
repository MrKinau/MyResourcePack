package dev.kinau.myresourcepack.screen.components;

import dev.kinau.myresourcepack.config.ResourceAction;
import dev.kinau.myresourcepack.config.ResourceTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Optional;

public class ResourceActionbox extends AbstractButton {
    private static final ResourceLocation CHECKBOX_PASS_HIGHLIGHTED_SPRITE = new ResourceLocation("myresourcepack", "textures/gui/sprites/widget/checkbox_pass_highlighted.png");
    private static final ResourceLocation CHECKBOX_PASS_SPRITE = new ResourceLocation("myresourcepack", "textures/gui/sprites/widget/checkbox_pass.png");
    private static final ResourceLocation CHECKBOX_BLOCK_HIGHLIGHTED_SPRITE = new ResourceLocation("myresourcepack", "textures/gui/sprites/widget/checkbox_block_highlighted.png");
    private static final ResourceLocation CHECKBOX_BLOCK_SPRITE = new ResourceLocation("myresourcepack", "textures/gui/sprites/widget/checkbox_block.png");
    private static final ResourceLocation CHECKBOX_UNKNOWN_HIGHLIGHTED_SPRITE = new ResourceLocation("myresourcepack", "textures/gui/sprites/widget/checkbox_unknown_highlighted.png");
    private static final ResourceLocation CHECKBOX_UNKNOWN_SPRITE = new ResourceLocation( "myresourcepack", "textures/gui/sprites/widget/checkbox_unknown.png");
    private static final ResourceLocation CHECKBOX_MERGE_HIGHLIGHTED_SPRITE = new ResourceLocation("myresourcepack", "textures/gui/sprites/widget/checkbox_merge_highlighted.png");
    private static final ResourceLocation CHECKBOX_MERGE_SPRITE = new ResourceLocation("myresourcepack", "textures/gui/sprites/widget/checkbox_merge.png");
    private static final int TEXT_COLOR = 0xE0E0E0;
    private ResourceAction action;
    private final ResourceTab resourceTab;
    private final boolean supportsMerging;

    public ResourceActionbox(int x, int y, int width, int height, Component component, ResourceAction action, ResourceTab resourceTab, boolean supportsMerging) {
        super(x, y, width, height, component);
        this.action = action;
        this.resourceTab = resourceTab;
        this.supportsMerging = supportsMerging && resourceTab != ResourceTab.ADDITION;
        updateToolTip();
    }

    @Override
    public void onPress() {
        if (action == null)
            action(ResourceAction.BLOCK);
        int index = (action.ordinal() + 1) % ResourceAction.values().length;
        ResourceAction nextAction = ResourceAction.values()[index];
        if (!supportsMerging && nextAction == ResourceAction.MERGE)
            nextAction = ResourceAction.PASS;
        action(nextAction);
        updateToolTip();
    }

    private void updateToolTip() {
        String toolTip = Optional.ofNullable(action()).map(action1 -> action1.getTranslationKey(resourceTab.name().toLowerCase())).orElse(ResourceAction.UNKNOWN_TRANSLATION_KEY);
        setTooltip(Tooltip.create(Component.translatable(toolTip)));
    }

    public ResourceAction action() {
        return this.action;
    }

    public void action(ResourceAction action) {
        this.action = action;
        updateToolTip();
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }

    private ResourceLocation getResource() {
        if (action == null)
            return isHovered() ? CHECKBOX_UNKNOWN_HIGHLIGHTED_SPRITE : CHECKBOX_UNKNOWN_SPRITE;
        return switch (action) {
            case PASS -> isHovered() ? CHECKBOX_PASS_HIGHLIGHTED_SPRITE : CHECKBOX_PASS_SPRITE;
            case MERGE -> isHovered() ? CHECKBOX_MERGE_HIGHLIGHTED_SPRITE : CHECKBOX_MERGE_SPRITE;
            default -> isHovered() ? CHECKBOX_BLOCK_HIGHLIGHTED_SPRITE : CHECKBOX_BLOCK_SPRITE;
        };
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        ResourceLocation resourceLocation = getResource();
        this.renderTexture(guiGraphics, resourceLocation, this.getX(), this.getY(), 0, 0, 0, this.width, this.height, this.width, this.height);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.drawString(font, this.getMessage(), this.getX() + this.width + 4, this.getY() + (this.height - 8) / 2, TEXT_COLOR | Mth.ceil(this.alpha * 255.0f) << 24);
    }
}