package dev.kinau.myresourcepack.screen.components.treeview;

import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.screen.components.buttons.ExpandButton;
import dev.kinau.myresourcepack.screen.components.buttons.ResourceActionbox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.InputWithModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TreeViewDirectoryEntry extends TreeViewEntry {

    protected final ResourceDirectory resourceDirectory;

    protected final ExpandButton expandButton;
    protected final ResourceActionbox actionbox;

    protected final List<TreeViewEntry> directChildren = new ArrayList<>();
    protected boolean expanded = false;

    public TreeViewDirectoryEntry(TreeView treeView, @Nullable ResourceDirectory parentDirectory, @Nullable TreeViewDirectoryEntry parentEntry, int depth, ResourceDirectory resourceDirectory) {
        super(treeView, parentDirectory, parentEntry, depth, resourceDirectory.getLabel(parentDirectory));
        this.resourceDirectory = resourceDirectory;

        this.expandButton = new ExpandButton(0, 0, 12, 12) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                super.onPress(inputWithModifiers);
                TreeViewDirectoryEntry.this.expanded = isExpanded();

                recalculateHeight();
                foreachParent(TreeViewEntry::recalculateHeight);

                triggerChange();
            }
        };
        children.add(expandButton);

        this.actionbox = new ResourceActionbox(0, 0, 12, 12, label, resourceDirectory.action(), treeView.getResourceTab(), resourceDirectory.supportsMerging()) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                super.onPress(inputWithModifiers);
                resourceDirectory.ruleAction(action());

                for (TreeViewEntry directChild : directChildren) {
                    for (GuiEventListener child : directChild.allChildrenIncludingInvisible()) {
                        if (child instanceof ResourceActionbox childActionbox)
                            childActionbox.action(action());
                    }
                }

                foreachParent(parentDirectoryEntry -> {
                    parentDirectoryEntry.actionbox.action(parentDirectoryEntry.resourceDirectory.action());
                });
            }
        };
        children.add(actionbox);
    }

    @Override
    protected int calculateHeight() {
        if (!expanded) return super.calculateHeight();
        int height = super.calculateHeight();
        for (TreeViewEntry child : directChildren) {
            height += child.calculateHeight();
        }
        return height;
    }

    public <T extends TreeViewEntry> void addChild(T entry) {
        directChildren.add(entry);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        if (!expanded) return super.children();
        List<GuiEventListener> children = new ArrayList<>(super.children());
        for (TreeViewEntry directChild : directChildren) {
            children.addAll(directChild.children());
        }
        return children;
    }

    @Override
    public List<GuiEventListener> allChildrenIncludingInvisible() {
        List<GuiEventListener> children = new ArrayList<>(super.allChildrenIncludingInvisible());
        for (TreeViewEntry directChild : directChildren) {
            children.addAll(directChild.allChildrenIncludingInvisible());
        }
        return children;
    }

    @Override
    protected void renderElementContent(GuiGraphics graphics, int x, int y, int ox, int oy, boolean hovered, float alpha) {
        expandButton.setX(x + getRenderDepth());
        expandButton.setY(y);
        expandButton.render(graphics, ox, oy, alpha);

        actionbox.setX(x + getRenderDepth() + 20);
        actionbox.setY(y);
        actionbox.render(graphics, ox, oy, alpha);

        if (expanded) {
            y += 20;
            for (TreeViewEntry directChild : directChildren) {
                directChild.renderElementContent(graphics, x, y, ox, oy, hovered, alpha);
                y += directChild.getHeight();
            }
        }
    }
}
