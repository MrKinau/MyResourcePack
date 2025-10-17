package dev.kinau.myresourcepack.screen.components.treeview;

import dev.kinau.myresourcepack.config.ResourceAction;
import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.config.resource.ResourceFile;
import dev.kinau.myresourcepack.screen.components.buttons.ResourceActionbox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import org.jetbrains.annotations.Nullable;

public class TreeViewFileEntry extends TreeViewEntry {

    protected final ResourceFile resourceFile;
    protected final ResourceActionbox actionbox;

    public TreeViewFileEntry(TreeView treeView, @Nullable ResourceDirectory parentDirectory, @Nullable TreeViewDirectoryEntry parentEntry, int depth, ResourceFile resourceFile) {
        super(treeView, parentDirectory, parentEntry, depth, resourceFile.getLabel(parentDirectory));
        this.resourceFile = resourceFile;
        this.actionbox = new ResourceActionbox(0, 0, 12, 12, label, resourceFile.action(), treeView.getResourceTab(), resourceFile.supportsMerging()) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                super.onPress(inputWithModifiers);
                foreachParent(parentDirectoryEntry -> {
                    parentDirectoryEntry.actionbox.action(parentDirectoryEntry.resourceDirectory.action());
                });
            }

            @Override
            public void action(ResourceAction action) {
                super.action(action);
                resourceFile.action(action);
            }
        };
        children.add(actionbox);
    }

    @Override
    protected void renderElementContent(GuiGraphics graphics, int x, int y, int ox, int oy, boolean hovered, float alpha) {
        actionbox.setX(x + getRenderDepth());
        actionbox.setY(y);
        actionbox.render(graphics, ox, oy, alpha);
    }
}
