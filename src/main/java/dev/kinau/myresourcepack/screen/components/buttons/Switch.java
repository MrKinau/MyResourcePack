package dev.kinau.myresourcepack.screen.components.buttons;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

@Getter
@Accessors(fluent = true)
public class Switch extends AbstractButton {
    private static final Identifier SWITCH_DISABLED_HIGHLIGHTED_SPRITE = Identifier.fromNamespaceAndPath("myresourcepack", "widget/disabled_highlighted");
    private static final Identifier SWITCH_DISABLED_SPRITE = Identifier.fromNamespaceAndPath( "myresourcepack", "widget/disabled");
    private static final Identifier SWITCH_ENABLED_HIGHLIGHTED_SPRITE = Identifier.fromNamespaceAndPath("myresourcepack", "widget/enabled_highlighted");
    private static final Identifier SWITCH_ENABLED_SPRITE = Identifier.fromNamespaceAndPath("myresourcepack", "widget/enabled");
    private static final int TEXT_COLOR = 0xE0E0E0;

    private boolean enabled;

    public Switch(int x, int y, int width, int height, Component component, boolean enabled) {
        super(x, y, width, height, component);
        this.enabled = enabled;
        setTooltip(Tooltip.create(Component.translatable("enable_resource_blocking_description")));
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        this.enabled = !enabled;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    private Identifier getResource() {
        if (enabled)
            return isHoveredOrFocused() ? SWITCH_ENABLED_HIGHLIGHTED_SPRITE : SWITCH_ENABLED_SPRITE;
        return isHoveredOrFocused() ? SWITCH_DISABLED_HIGHLIGHTED_SPRITE : SWITCH_DISABLED_SPRITE;
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Identifier resource = getResource();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, resource, this.getX(), this.getY(), 60, this.height, ARGB.white(this.alpha));
        graphics.text(font, this.getMessage(), this.getX() + 60 + 4, this.getY() + (this.height - 8) / 2, TEXT_COLOR | Mth.ceil(this.alpha * 255.0f) << 24);
    }
}