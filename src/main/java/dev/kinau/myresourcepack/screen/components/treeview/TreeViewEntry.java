package dev.kinau.myresourcepack.screen.components.treeview;

import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class TreeViewEntry extends ContainerObjectSelectionList.Entry<TreeViewEntry> {

    protected final List<AbstractWidget> children = new ArrayList<>();
    protected int height = 20;

    protected final TreeView treeView;
    @Nullable
    protected final ResourceDirectory parentDirectory;
    @Nullable
    protected final TreeViewDirectoryEntry parentEntry;
    protected final int depth;
    protected final Component label;

    public TreeViewEntry(TreeView treeView, @Nullable ResourceDirectory parentDirectory, @Nullable TreeViewDirectoryEntry parentEntry, int depth, Component label) {
        this.treeView = treeView;
        this.parentDirectory = parentDirectory;
        this.parentEntry = parentEntry;
        this.depth = depth;
        this.label = label;
    }

    protected int getRenderDepth() {
        return depth * 20;
    }

    protected int calculateHeight() {
        return 20;
    }

    protected void recalculateHeight() {
        this.height = calculateHeight();
    }

    protected void foreachParent(Consumer<TreeViewDirectoryEntry> consumer) {
        TreeViewDirectoryEntry entry = parentEntry;
        while (entry != null) {
            consumer.accept(entry);
            entry = entry.parentEntry;
        }
    }

    protected void triggerChange() {
        treeView.setScrollAmount(treeView.scrollAmount());
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return children;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    public List<? extends GuiEventListener> allChildrenIncludingInvisible() {
        return children;
    }

    protected abstract void renderElementContent(@NonNull GuiGraphicsExtractor graphics, int x, int y, int ox, int oy, boolean hovered, float alpha);

    @Override
    public void extractContent(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
        int x = getContentX();
        int y = getContentY();
        renderElementContent(graphics, x, y, mouseX, mouseY, hovered, a);
    }
}
