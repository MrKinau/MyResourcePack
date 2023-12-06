package dev.kinau.myresourcepack.mixin;

import dev.kinau.myresourcepack.expander.PackConfirmScreenExpander;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ClientCommonPacketListenerImpl.PackConfirmScreen.class)
public abstract class PackConfirmScreenMixin implements PackConfirmScreenExpander {

    @Override
    @Accessor
    public abstract List<ClientCommonPacketListenerImpl.PackConfirmScreen.PendingRequest> getRequests();

    @Override
    @Accessor
    public abstract Screen getParentScreen();
}
