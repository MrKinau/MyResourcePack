package dev.kinau.myresourcepack.expander;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;

import java.util.List;

public interface PackConfirmScreenExpander {

    List<ClientCommonPacketListenerImpl.PackConfirmScreen.PendingRequest> getRequests();

    Screen getParentScreen();
}
