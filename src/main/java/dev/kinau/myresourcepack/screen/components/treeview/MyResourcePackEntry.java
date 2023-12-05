package dev.kinau.myresourcepack.screen.components.treeview;

import dev.kinau.myresourcepack.config.ResourceTab;
import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.screen.ResourceSelectionScreen;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
public abstract class MyResourcePackEntry extends ContainerObjectSelectionList.Entry<MyResourcePackEntry> {

    protected final ResourceSelectionScreen screen;
    protected final ResourceTab resourceTab;
    protected final ResourceDirectory parent;
    protected final int depth;

    protected int getHeight() {
        return 20;
    }

    protected void foreachParent(List<MyResourcePackEntry> entries, ResourceLocation currentLoc, Consumer<ResourceConfigurationDirectoryEntry> consumer) {
        for (MyResourcePackEntry child : entries) {
            if (child instanceof ResourceConfigurationDirectoryEntry dirEntry && currentLoc.toString().startsWith(dirEntry.getResourceDirectory().location().toString())) {
                consumer.accept(dirEntry);
                foreachParent(dirEntry.getChildren(), currentLoc, consumer);
            }
        }
    }

}