package dev.kinau.myresourcepack.screen.components.tabs;

import dev.kinau.myresourcepack.config.ResourceTab;
import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.screen.ResourceSelectionScreen;
import dev.kinau.myresourcepack.screen.components.treeview.TreeViewWidget;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TreeViewTab implements Tab {

    @Getter
    private final ResourceTab resourceTab;
    private final Component title;
    @Getter
    private final TreeViewWidget treeViewWidget;

    private final List<AbstractWidget> widgets = new ArrayList<>();

    public TreeViewTab(int width, int height, Minecraft minecraft, ResourceSelectionScreen screen, ResourceTab resourceTab, Component title, ResourceDirectory rootDirectory) {
        this.resourceTab = resourceTab;
        this.title = title;
        this.treeViewWidget = new TreeViewWidget(0, 0, width, height, screen, resourceTab, minecraft, rootDirectory);
        widgets.add(treeViewWidget);
    }

    @Override
    public Component getTabTitle() {
        return title;
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        widgets.forEach(consumer);
    }

    @Override
    public void doLayout(ScreenRectangle screenRectangle) {}
}
