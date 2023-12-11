package dev.kinau.myresourcepack.screen.components.treeview;

import dev.kinau.myresourcepack.config.ResourceTab;
import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.config.resource.ResourceFile;
import dev.kinau.myresourcepack.config.resource.ResourceObject;
import dev.kinau.myresourcepack.screen.ResourceSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;

public class ResourceConfigurationTreeView extends ContainerObjectSelectionList<MyResourcePackEntry> {

    private final ResourceSelectionScreen screen;
    private final ResourceTab resourceTab;
    private final Minecraft minecraft;

    public ResourceConfigurationTreeView(ResourceSelectionScreen screen, int width, int height, ResourceTab resourceTab, ResourceDirectory rootDirectory, Minecraft minecraft) {
        super(minecraft, width, height, 43, height - 32, 24);
        this.screen = screen;
        this.minecraft = minecraft;
        this.resourceTab = resourceTab;
        rootDirectory.children().forEach(resourceObject -> {
            add(null, resourceObject, null, 0);
        });
    }

    private void add(ResourceConfigurationDirectoryEntry entry, ResourceObject obj, ResourceDirectory parent, int depth) {
        if (obj instanceof ResourceFile resourceFile)
            add(entry, new ResourceConfigurationFileEntry(screen, resourceTab, resourceFile, parent, depth, minecraft));
        else if (obj instanceof ResourceDirectory resourceDirectory) {
            ResourceConfigurationDirectoryEntry section = new ResourceConfigurationDirectoryEntry(screen, resourceTab, resourceDirectory, parent, depth, this, minecraft);
            add(entry, section);
            resourceDirectory.children().forEach(resourceObject -> {
                add(section, resourceObject, resourceDirectory, depth + 1);
            });
        }
    }

    private void add(ResourceConfigurationDirectoryEntry entry, MyResourcePackEntry toAdd) {
        if (entry == null)
            addEntry(toAdd);
        else
            entry.addEntry(toAdd);
    }

    @Override
    protected void renderList(GuiGraphics guiGraphics, int i, int j, float f) {
        int k = this.getRowLeft();
        int l = this.getRowWidth();
        int m = this.itemHeight - 4;
        int n = this.getItemCount();
        for (int o = 0; o < n; ++o) {
            int p = this.getRowTop(o);
            int q = this.getRowBottom(o);
            if (q < this.y0 || p > this.y1) continue;
            this.renderItem(guiGraphics, i, j, f, o, k, p, l, m);
        }
    }

    @Override
    protected int getRowTop(int i) {
        int prevHeight = 0;
        for (int j = 0; j < i; j++)
            prevHeight += getEntry(j).getHeight();
        return this.y0 + 4 - (int) this.getScrollAmount() + prevHeight + this.headerHeight;
    }

    @Override
    protected int getRowBottom(int i) {
        MyResourcePackEntry entry = getEntry(i);
        return getRowTop(i) + entry.getHeight();
    }

    @Override
    protected int getMaxPosition() {
        int prevHeight = 0;
        for (int j = 0; j < getItemCount(); j++)
            prevHeight += getEntry(j).getHeight();
        return prevHeight + this.headerHeight + 20;
    }

    @Override
    protected void ensureVisible(MyResourcePackEntry entry) {
        int k;
        int i = this.getRowTop(this.children().indexOf(entry));
        int j = i - this.y0 - 4 - entry.getHeight();
        if (j < 0) {
            this.scroll(j);
        }
        if ((k = this.y1 - i - (2 * entry.getHeight())) < 0) {
            this.scroll(-k);
        }
    }

    private boolean isScrolling(double x, double y, int button) {
        return button == 0 && x >= (double)this.getScrollbarPosition() && x < (double)(this.getScrollbarPosition() + 6);
    }

    protected final MyResourcePackEntry getMyEntryAtPosition(double x, double y) {
        int i = this.getRowWidth() / 2;
        int k = this.x0 + this.width / 2;
        if (x >= getScrollbarPosition())
            return null;
        if (x > k + i)
            return null;

        int clickHeight = Mth.floor(y - (double) this.y0) - this.headerHeight + (int) getScrollAmount() - 7;

        int prevHeight = 0;
        for (int j = 0; j < getItemCount(); j++) {
            MyResourcePackEntry curr = getEntry(j);
            if (prevHeight <= clickHeight) {
                prevHeight += curr.getHeight();
                if (prevHeight > clickHeight)
                    return curr;
            } else {
                break;
            }
        }

        return null;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        updateScrollingState(x, y, button);
        if (!isMouseOver(x, y)) {
            return false;
        }
        MyResourcePackEntry entry = getMyEntryAtPosition(x, y);
        if (entry != null) {
            if (entry.mouseClicked(x, y, button)) {
                GuiEventListener entry2 = getFocused();
                if (entry2 != entry && entry2 instanceof ContainerEventHandler) {
                    ContainerEventHandler containerEventHandler = (ContainerEventHandler)entry2;
                    containerEventHandler.setFocused(null);
                }
                setFocused(entry);
                setDragging(true);
                return true;
            }
        } else {
            this.clickedHeader((int)(x - (double)(this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int)(y - (double)this.y0) + (int)this.getScrollAmount() - 4);
            return true;
        }
        return isScrolling(x, y, button);
    }

    private void scroll(int i) {
        this.setScrollAmount(this.getScrollAmount() + (double) i);
    }

    @Override
    protected int getScrollbarPosition() {
        return (minecraft.screen.width / 6) * 5;
    }
}