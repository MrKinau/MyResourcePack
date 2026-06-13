package dev.kinau.myresourcepack.mixin;

import dev.kinau.myresourcepack.expander.ServerDataExpander;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ManageServerScreen.class)
public class ManageServerScreenMixin {

    @Shadow
    @Final
    private ServerData serverData;

    @ModifyArg(
            method = "init()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/ManageServerScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;",
                    ordinal = 0
            )
    )
    private GuiEventListener replaceWidget(GuiEventListener originalWidget) {
        if (originalWidget instanceof CycleButton<?> cycleButton) {
            ServerDataExpander.MappedPackStatus packStatus = serverData instanceof ServerDataExpander serverDataExpander && serverDataExpander.myResourcePack$getPackStatus() != null
                    ? serverDataExpander.myResourcePack$getPackStatus()
                    : ServerDataExpander.MappedPackStatus.getByMappedStatus(serverData.getResourcePackStatus());
            return CycleButton.builder(ServerDataExpander.MappedPackStatus::getName, packStatus)
                    .withValues(ServerDataExpander.MappedPackStatus.VALUES)
                    .create(cycleButton.getX(), cycleButton.getY(), cycleButton.getWidth(), cycleButton.getHeight(),
                            Component.translatable("manageServer.resourcePack"),
                            (button, value) -> {
                                this.serverData.setResourcePackStatus(value.getMappedStatus());
                                if (serverData instanceof ServerDataExpander serverDataExpander) {
                                    serverDataExpander.myResourcePack$setPackStatus(value);
                                }
                            }
                    );
        }
        return originalWidget;
    }
}
