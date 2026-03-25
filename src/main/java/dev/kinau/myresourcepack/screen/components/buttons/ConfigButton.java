package dev.kinau.myresourcepack.screen.components.buttons;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;

@Getter
@Setter
@Accessors(fluent = true)
public abstract class ConfigButton extends AbstractButton {
    private static final Identifier CONFIG_DISABLED_SPRITE = Identifier.fromNamespaceAndPath("myresourcepack", "widget/config_disabled");
    private static final Identifier CONFIG_HIGHLIGHTED_SPRITE = Identifier.fromNamespaceAndPath("myresourcepack", "widget/config_highlighted");
    private static final Identifier CONFIG_SPRITE = Identifier.fromNamespaceAndPath("myresourcepack", "widget/config");

    public ConfigButton(int x, int y, int width, int height, boolean enabled) {
        super(x, y, width, height, Component.empty());
        this.active = enabled;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    private Identifier getResource() {
        if (isActive())
            return isHoveredOrFocused() ? CONFIG_HIGHLIGHTED_SPRITE : CONFIG_SPRITE;
        return CONFIG_DISABLED_SPRITE;
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Identifier resource = getResource();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, resource, this.getX(), this.getY(), this.width, this.height, ARGB.white(this.alpha));
    }
}
