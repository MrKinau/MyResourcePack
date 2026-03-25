package dev.kinau.myresourcepack.screen.components.treeview;

import dev.kinau.myresourcepack.config.ResourceTab;
import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.config.resource.ResourceFile;
import dev.kinau.myresourcepack.config.resource.ResourceObject;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@Getter
public class TreeView extends ContainerObjectSelectionList<TreeViewEntry> {

    private final ResourceTab resourceTab;

    public TreeView(Minecraft client, int parentWidth, HeaderAndFooterLayout parentLayout, ResourceTab resourceTab, ResourceDirectory rootDirectory) {
        super(client, parentWidth, parentLayout.getContentHeight(), 33 /* y */, 14 /* defaultEntryHeight (overridden) */);
        this.resourceTab = resourceTab;
        rootDirectory.children().forEach(resourceObject -> {
            addResourceEntry(null, resourceObject, null, 0);
        });
    }

    private void addResourceEntry(@Nullable TreeViewDirectoryEntry parentEntry, ResourceObject obj, @Nullable ResourceDirectory parent, int depth) {
        if (obj instanceof ResourceFile resourceFile) {
            addResourceEntry(parentEntry, new TreeViewFileEntry(this, parent, parentEntry, depth, resourceFile));
        } else if (obj instanceof ResourceDirectory resourceDirectory) {
            TreeViewDirectoryEntry section = new TreeViewDirectoryEntry(this, parent, parentEntry, depth, resourceDirectory);
            addResourceEntry(parentEntry, section);
            for (ResourceObject resourceObject : resourceDirectory.children()) {
                addResourceEntry(section, resourceObject, resourceDirectory, depth + 1);
            }
        }
    }

    private <T extends TreeViewEntry> void addResourceEntry(@Nullable TreeViewDirectoryEntry parentEntry, T toAdd) {
        if (parentEntry == null)
            addEntry(toAdd);
        else
            parentEntry.addChild(toAdd);
    }

    @Override
    public int getRowWidth() {
        return (int) (width * 0.75);
    }

    @Override
    protected void extractListBackground(@NonNull GuiGraphicsExtractor graphics) {}

    @Override
    protected void extractListSeparators(@NonNull GuiGraphicsExtractor graphics) {}
}
