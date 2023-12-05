package dev.kinau.myresourcepack.screen.components.treeview;

import dev.kinau.myresourcepack.config.ResourceTab;
import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.screen.ResourceSelectionScreen;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Getter
public class TreeViewWidget extends AbstractWidget {

    private final ResourceConfigurationTreeView treeView;

    public TreeViewWidget(int x, int y, int width, int height, ResourceSelectionScreen screen, ResourceTab resourceTab, Minecraft minecraft, ResourceDirectory rootDirectory) {
        super(x, y, width, height, Component.empty());
        this.treeView = new ResourceConfigurationTreeView(screen, width, height, resourceTab, rootDirectory, minecraft);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        this.treeView.render(guiGraphics, i, j, f);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.treeView.updateNarration(narrationElementOutput);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        return treeView.mouseScrolled(d, e, f, g);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return treeView.mouseClicked(d, e, i);
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        return treeView.mouseDragged(d, e, i, f, g);
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        return treeView.mouseReleased(d, e, i);
    }

    @Override
    public void mouseMoved(double d, double e) {
        treeView.mouseMoved(d, e);
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return treeView.isMouseOver(d, e);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        return treeView.keyPressed(i, j, k);
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        return treeView.keyReleased(i, j, k);
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        return treeView.nextFocusPath(focusNavigationEvent);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return treeView.narrationPriority();
    }

//    @Override
//    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
//        treeView.render(guiGraphics, i, j, f);
//    }

//    @Override
//    public ScreenRectangle getRectangle() {
//        return treeView.getRectangle();
//    }

}
