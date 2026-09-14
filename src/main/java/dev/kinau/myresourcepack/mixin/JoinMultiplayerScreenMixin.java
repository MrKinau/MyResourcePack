package dev.kinau.myresourcepack.mixin;

import dev.kinau.myresourcepack.expander.ServerDataExpander;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin {

    @Shadow
    private ServerData editingServer;

    @Shadow
    protected ServerSelectionList serverSelectionList;

    @Inject(method = "editServerCallback", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ServerData;setResourcePackStatus(Lnet/minecraft/client/multiplayer/ServerData$ServerPackStatus;)V"))
    private void onEditServerCallback(CallbackInfo ci) {
        if (editingServer instanceof ServerDataExpander editingData) {
            ServerSelectionList.Entry entry = serverSelectionList.getSelected();
            if (entry instanceof ServerSelectionList.OnlineServerEntry onlineServerEntry && onlineServerEntry.getServerData() instanceof ServerDataExpander currentData) {
                currentData.myResourcePack$setPackStatus(editingData.myResourcePack$getPackStatus());
            }
        }
    }
}
