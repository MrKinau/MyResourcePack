package dev.kinau.myresourcepack.screen;

import dev.kinau.myresourcepack.MyResourcePack;
import dev.kinau.myresourcepack.config.ResourceTab;
import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.config.resource.ResourceFile;
import dev.kinau.myresourcepack.screen.components.tab.TreeViewTab;
import dev.kinau.myresourcepack.screen.components.treeview.TreeView;
import dev.kinau.myresourcepack.utils.ResourceBlockingUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;

public class ResourceSelectionScreen extends Screen {

    public static final ResourceLocation TAB_HEADER_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/tab_header_background.png");

    protected final ResourceDirectory rootDirectory;
    protected final Screen lastScreen;
    protected ResourceDirectory prevOverriddenDirectory;
    protected ResourceDirectory prevAdditionalDirectory;
    protected ResourceDirectory overriddenDirectory;
    protected ResourceDirectory additionalDirectory;

    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    @Nullable
    private TabNavigationBar tabNavigationBar;

    public ResourceSelectionScreen(Screen lastScreen, ResourceDirectory rootDirectory) {
        super(Component.translatable("enable_resource_blocking"));
        this.rootDirectory = rootDirectory;
        this.lastScreen = lastScreen;
    }

    private void closeScreen() {
        if (minecraft == null) return;
        minecraft.setScreen(lastScreen);
    }

    private TreeViewTab createTab(ResourceTab tab, ResourceDirectory rootDirectory) {
        return new TreeViewTab(
                tab.getTitle(),
                new TreeView(minecraft, width, layout, tab, rootDirectory),
                () -> width,
                layout
        );
    }

    private void applySelectedRules(ResourceDirectory directory) {
        if (directory == overriddenDirectory) {
            ResourceBlockingUtils.getServerSetting().ifPresent(serverSetting -> {
                serverSetting.overrideRules().forEach(directory::applyRule);
            });
        } else if (directory == additionalDirectory) {
            ResourceBlockingUtils.getServerSetting().ifPresent(serverSetting -> {
                serverSetting.additionRules().forEach(directory::applyRule);
            });
        }
    }

    private boolean hasChanges() {
        return !prevAdditionalDirectory.equals(additionalDirectory) || !prevOverriddenDirectory.equals(overriddenDirectory);
    }

    private void reloadAndClose() {
        if (minecraft != null) {
            minecraft.reloadResourcePacks().thenAccept(unused -> {
                closeScreen();
            });
        } else {
            closeScreen();
        }
    }

    private void done() {
        if (hasChanges()) {
            ResourceBlockingUtils.getServerSetting().ifPresent(serverSetting -> {
                serverSetting.overrideRules(rootDirectory.createRules(overriddenDirectory));
                serverSetting.additionRules(rootDirectory.createRules(additionalDirectory));
                try {
                    MyResourcePack.getInstance().getPackSettings().saveConfig();
                } catch (IOException e) {
                    MyResourcePack.LOGGER.error("Could not save config", e);
                }
            });
            reloadAndClose();
            return;
        }
        closeScreen();
    }

    @Override
    public void onClose() {
        done();
    }

    @Override
    protected void init() {
        this.overriddenDirectory = rootDirectory.clone().filter(resourceObject -> {
            if (resourceObject instanceof ResourceDirectory) return true;
            if (resourceObject instanceof ResourceFile resourceFile && resourceFile.overridesVanilla()) return true;
            return false;
        });
        applySelectedRules(overriddenDirectory);
        overriddenDirectory.flattenSort();
        this.prevOverriddenDirectory = overriddenDirectory.clone();

        this.additionalDirectory = rootDirectory.clone().filter(resourceObject -> {
            if (resourceObject instanceof ResourceDirectory) return true;
            if (resourceObject instanceof ResourceFile resourceFile && !resourceFile.overridesVanilla()) return true;
            return false;
        });
        applySelectedRules(additionalDirectory);
        additionalDirectory.flattenSort();
        this.prevAdditionalDirectory = additionalDirectory.clone();

        this.tabNavigationBar = TabNavigationBar.builder(tabManager, width)
                .addTabs(
                        createTab(ResourceTab.OVERRIDE, overriddenDirectory),
                        createTab(ResourceTab.ADDITION, additionalDirectory)
                ).build();
        addRenderableWidget(tabNavigationBar);

        LinearLayout footerLayout = layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footerLayout.addChild(Button.builder(Component.translatable("resourceSelectionScreen.reset"), button -> {
            ResourceBlockingUtils.getServerSetting().ifPresent(serverSetting -> {
                serverSetting.overrideRules(new ArrayList<>());
                serverSetting.additionRules(new ArrayList<>());
                try {
                    MyResourcePack.getInstance().getPackSettings().saveConfig();
                } catch (IOException e) {
                    MyResourcePack.LOGGER.error("Could not save config", e);
                }
            });
            reloadAndClose();
        }).build());
        footerLayout.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
            done();
        }).build());

        layout.visitWidgets(widget -> {
            widget.setTabOrderGroup(1);
            addRenderableWidget(widget);
        });
        tabNavigationBar.selectTab(0, false);

        repositionElements();
    }

    @Override
    public void repositionElements() {
        if (tabNavigationBar != null) {
            tabNavigationBar.setWidth(width);
            tabNavigationBar.arrangeElements();
            int var0 = tabNavigationBar.getRectangle().bottom();
            ScreenRectangle var1 = new ScreenRectangle(0, var0, width, height - layout.getFooterHeight() - var0);
            tabManager.setTabArea(var1);
            layout.setHeaderHeight(var0);
            layout.arrangeElements();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float gameTime) {
        super.render(graphics, x, y, gameTime);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR, 0, this.height - this.layout.getFooterHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
    }

    @Override
    protected void renderMenuBackground(GuiGraphics param0) {
        param0.blit(RenderPipelines.GUI_TEXTURED, TAB_HEADER_BACKGROUND, 0, 0, 0.0F, 0.0F, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.renderMenuBackground(param0, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }
}
