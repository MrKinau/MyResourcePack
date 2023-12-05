package dev.kinau.myresourcepack.screen.components.treeview;

import dev.kinau.myresourcepack.config.ResourceTab;
import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.screen.ResourceSelectionScreen;
import dev.kinau.myresourcepack.screen.components.ExpandButton;
import dev.kinau.myresourcepack.screen.components.ResourceActionbox;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ResourceConfigurationDirectoryEntry extends MyResourcePackEntry {

    @Getter
    private final ResourceDirectory resourceDirectory;
    private final Component name;
    @Getter
    private final List<MyResourcePackEntry> children = new ArrayList<>();
    protected ExpandButton expandButton = null;
    protected final ResourceActionbox box;
    private final Minecraft minecraft;

    private boolean expanded = false;

    public ResourceConfigurationDirectoryEntry(ResourceSelectionScreen screen, ResourceTab resourceTab, ResourceDirectory resourceDirectory, ResourceDirectory parent, int depth, ResourceConfigurationTreeView treeView, Minecraft minecraft) {
        super(screen, resourceTab, parent, depth);
        this.minecraft = minecraft;
        this.resourceDirectory = resourceDirectory;
        String renderedText = resourceDirectory.location().toString().substring(Optional.ofNullable(parent)
                .map(dir -> dir.location().toString().length())
                .orElse(0));
        if (renderedText.startsWith("/"))
            renderedText = renderedText.substring(1);
        this.name = Component.literal(renderedText).withStyle(ChatFormatting.GRAY);

        this.expandButton = new ExpandButton(0, 0, 12, 12) {
            @Override
            public void onPress() {
                super.onPress();
                expanded = isExpanded();
                if (expanded) {
                    children.forEach(myResourcePackEntry -> {
                        myResourcePackEntry.children().forEach(guiEventListener -> {
                            if (guiEventListener instanceof AbstractWidget widget)
                                widget.visible = true;
                        });
                    });
                } else {
                    children.forEach(myResourcePackEntry -> {
                        myResourcePackEntry.children().forEach(guiEventListener -> {
                            if (guiEventListener instanceof AbstractWidget widget)
                                widget.visible = false;
                        });
                    });
                }
            }
        };
        expandButton.visible = depth == 0;
        this.box = new ResourceActionbox(0, 0, 12, 12, this.name, resourceDirectory.action(), resourceTab, resourceDirectory.supportsMerging()) {
            @Override
            public void onPress() {
                super.onPress();
                resourceDirectory.ruleAction(action());
                foreachChild(children, widget -> {
                    if (widget instanceof ResourceActionbox actionbox) {
                        actionbox.action(action());
                    }
                });
                foreachParent(screen.getCurrentTree().children(), resourceDirectory.location(), dir -> {
                    dir.box.action(dir.getResourceDirectory().action());
                });
            }
        };
        box.visible = depth == 0;
    }

    protected void foreachChild(List<MyResourcePackEntry> entries, Consumer<AbstractWidget> consumer) {
        for (MyResourcePackEntry child : entries) {
            for (GuiEventListener guiEventListener : child.children()) {
                if (guiEventListener instanceof AbstractWidget widget)
                    consumer.accept(widget);
            }
            if (child instanceof ResourceConfigurationDirectoryEntry section) {
                foreachChild(section.children, consumer);
            }
        }
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return Collections.emptyList();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
        int x = minecraft.screen.width / 6 + (depth * 28);

        expandButton.setX(x);
        expandButton.setY(j + m - minecraft.font.lineHeight - 1);
        box.setX(x + expandButton.getWidth() + 4);
        box.setY(j + m - minecraft.font.lineHeight - 1);

        expandButton.render(guiGraphics, n, o, f);
        box.render(guiGraphics, n, o, f);

        if (!expanded) return;
        int nextY = j + super.getHeight();
        for (MyResourcePackEntry entry : children) {
            entry.render(guiGraphics, i, nextY, k, l, m, n, o, bl, f);
            nextY += entry.getHeight();
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> allChildren = new ArrayList<>(List.of(expandButton, box));
        children.forEach(myResourcePackEntry -> allChildren.addAll(myResourcePackEntry.children()));
        return allChildren;
    }

    public void addEntry(MyResourcePackEntry entry) {
        children.add(entry);
    }

    @Override
    protected int getHeight() {
        int height = super.getHeight();
        if (!expanded) return height;
        for (MyResourcePackEntry child : children) {
            height += child.getHeight();
        }
        return height;
    }

}