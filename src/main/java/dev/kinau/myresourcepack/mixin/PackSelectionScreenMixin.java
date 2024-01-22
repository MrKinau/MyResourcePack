package dev.kinau.myresourcepack.mixin;

import dev.kinau.myresourcepack.MyResourcePack;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackSelectionScreen.class)
public class PackSelectionScreenMixin {

    @Inject(method = "init", at = @At("HEAD"))
    public void onInit(CallbackInfo ci) {
        if (MyResourcePack.getInstance().getCurrentServer() != null)
            MyResourcePack.getInstance().setConfiguringPackOrder(true);
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    public void onClose(CallbackInfo ci) {
        if (MyResourcePack.getInstance().getCurrentServer() != null)
            MyResourcePack.getInstance().setConfiguringPackOrder(true);
    }
}
