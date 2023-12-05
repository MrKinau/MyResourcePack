package dev.kinau.myresourcepack.screen.components.treeview;

import dev.kinau.myresourcepack.config.ResourceAction;
import dev.kinau.myresourcepack.config.ResourceTab;
import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.config.resource.ResourceFile;
import dev.kinau.myresourcepack.screen.ResourceSelectionScreen;
import dev.kinau.myresourcepack.screen.components.ResourceActionbox;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ResourceConfigurationFileEntry extends MyResourcePackEntry {

    private final ResourceFile resourceFile;
    private final Component name;
    private final ResourceActionbox box;
    private final Minecraft minecraft;

    public ResourceConfigurationFileEntry(ResourceSelectionScreen screen, ResourceTab resourceTab, ResourceFile resourceFile, ResourceDirectory parent, int depth, Minecraft minecraft) {
        super(screen, resourceTab, parent, depth);
        this.minecraft = minecraft;
        this.resourceFile = resourceFile;
        String renderedText = resourceFile.location().toString().substring(Optional.ofNullable(parent).map(dir -> dir.location().toString().length()).orElse(0));
        if (renderedText.startsWith("/"))
            renderedText = renderedText.substring(1);
        this.name = Component.literal(renderedText).withStyle(ChatFormatting.WHITE);
        this.box = new ResourceActionbox(0, 0, 12, 12, this.name, resourceFile.action(), resourceTab, resourceFile.supportsMerging()) {
            @Override
            public void onPress() {
                super.onPress();
                foreachParent(screen.getCurrentTree().children(), resourceFile.location(), dir -> {
                    dir.box.action(dir.getResourceDirectory().action());
                });
            }

            @Override
            public void action(ResourceAction action) {
                super.action(action);
                resourceFile.action(action);
            }
        };
        box.visible = depth == 0;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return Collections.emptyList();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
        box.setX(minecraft.screen.width / 6 + (depth * 28));
        box.setY(j + m - minecraft.font.lineHeight - 1);
        box.render(guiGraphics, n, o, f);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.singletonList(box);
    }

}