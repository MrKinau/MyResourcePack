package dev.kinau.myresourcepack.screen;

import dev.kinau.myresourcepack.MyResourcePack;
import dev.kinau.myresourcepack.config.ResourceTab;
import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.config.resource.ResourceFile;
import dev.kinau.myresourcepack.screen.components.tabs.TreeViewTab;
import dev.kinau.myresourcepack.screen.components.treeview.ResourceConfigurationTreeView;
import dev.kinau.myresourcepack.utils.ResourceBlockingUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.ArrayList;

import static net.minecraft.client.gui.screens.worldselection.CreateWorldScreen.LIGHT_DIRT_BACKGROUND;

public class ResourceSelectionScreen extends Screen {

    private final Component OVERRIDDEN_TITLE = Component.translatable("resourceSelectionScreen.tab.overridden.title");
    private final Component ADDITIONAL_TITLE = Component.translatable("resourceSelectionScreen.tab.additional.title");

    protected final Screen lastScreen;
    protected final ResourceDirectory rootDirectory;
    protected ResourceDirectory prevOverriddenDirectory;
    protected ResourceDirectory prevAdditionalDirectory;
    protected ResourceDirectory overriddenDirectory;
    protected ResourceDirectory additionalDirectory;

    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);

    protected Button resetButton;
    protected Button doneButton;
    protected TabNavigationBar tabNavigationBar;

    public ResourceSelectionScreen(Screen lastScreen, ResourceDirectory rootDirectory) {
        super(Component.empty());
        this.rootDirectory = rootDirectory;
        this.lastScreen = lastScreen;
    }

    @Override
    public void onClose() {
        done();
    }

    private void closeScreen() {
        this.minecraft.setScreen(this.lastScreen);
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

        this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width).addTabs(
                new TreeViewTab(width, height - 28, minecraft, this, ResourceTab.OVERRIDE, OVERRIDDEN_TITLE, overriddenDirectory),
                new TreeViewTab(width, height - 28, minecraft, this, ResourceTab.ADDITION, ADDITIONAL_TITLE, additionalDirectory)
        ).build();
        this.addRenderableWidget(this.tabNavigationBar);

        this.resetButton = this.addRenderableWidget(Button.builder(Component.translatable("resourceSelectionScreen.reset"), button -> {
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
        }).bounds(this.width / 2 - 155, this.height - 29, 150, 20).build());

        this.addRenderableWidget(doneButton = Button.builder(CommonComponents.GUI_DONE, button -> {
            done();
        }).bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20).build());

        this.tabNavigationBar.selectTab(0, false);
        repositionElements();
    }

    @Override
    public void repositionElements() {
        if (this.tabNavigationBar == null) {
            return;
        }
        this.tabNavigationBar.setWidth(this.width);
        this.tabNavigationBar.arrangeElements();
        int i = this.tabNavigationBar.getRectangle().bottom();
        ScreenRectangle screenRectangle = new ScreenRectangle(0, i, this.width, this.height - i);
        this.tabManager.setTabArea(screenRectangle);
    }


    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        this.renderDirtBackground(guiGraphics);
    }

    @Override
    public void renderDirtBackground(GuiGraphics guiGraphics) {
        guiGraphics.blit(LIGHT_DIRT_BACKGROUND, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, 32, 32);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (tabNavigationBar.keyPressed(i)) {
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, i, j, f);
    }

    public ResourceConfigurationTreeView getCurrentTree() {
        Tab tab = tabManager.getCurrentTab();
        if (!(tab instanceof TreeViewTab treeViewTab)) return null;
        return treeViewTab.getTreeViewWidget().getTreeView();
    }
}
