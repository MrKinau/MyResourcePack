package dev.kinau.myresourcepack.screen.components.tab;

import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import java.util.function.IntSupplier;

public class TreeViewTab extends GridLayoutTab {

    private final IntSupplier parentWidth;
    private final HeaderAndFooterLayout parentLayout;

    protected final AbstractSelectionList<?> child;

    public TreeViewTab(Component title, AbstractSelectionList<?> child, IntSupplier parentWidth, HeaderAndFooterLayout parentLayout) {
        super(title);
        this.parentWidth = parentWidth;
        this.parentLayout = parentLayout;
        this.child = child;
        layout.addChild(child, 1, 1);
    }

    @Override
    public void doLayout(ScreenRectangle screenRectangle) {
        this.child.updateSizeAndPosition(parentWidth.getAsInt(), parentLayout.getContentHeight(), parentLayout.getHeaderHeight());
        super.doLayout(screenRectangle);
    }
}
