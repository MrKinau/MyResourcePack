package dev.kinau.myresourcepack.mixin;

import dev.kinau.myresourcepack.MyResourcePack;
import dev.kinau.myresourcepack.config.ServerSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Unique
    private boolean failedWithActiveBlocking = false;

    @Shadow
    public abstract ToastComponent getToasts();

    @Inject(method = "clearResourcePacksOnError", at = @At(value = "HEAD"), cancellable = true)
    public void onClearResourcePacksOnError(Throwable throwable, @Nullable Component component, @Nullable Minecraft.GameLoadCookie gameLoadCookie, CallbackInfo ci) {
        String server = MyResourcePack.getInstance().getCurrentServer();
        if (server == null) return;
        ServerSetting setting = MyResourcePack.getInstance().getPackSettings().getConfigData().getSettings(server);
        if (setting == null || setting.overrideTextures()) return;
        setting.overrideTextures(true);
        this.failedWithActiveBlocking = true;
        Minecraft.getInstance().execute(() -> {
            SystemToast.add(Minecraft.getInstance().getToasts(), SystemToast.SystemToastId.PACK_LOAD_FAILURE, Component.translatable("resourcePack.load_fail"), Component.translatable("myResourcePack.notification.disabledDueToError"));
        });
    }

    @Inject(method = "addResourcePackLoadFailToast", at = @At(value = "RETURN"), cancellable = true)
    public void onAddResourcePackLoadFailToast(@Nullable Component description, CallbackInfo ci) {
        if (failedWithActiveBlocking)
            SystemToast.add(getToasts(), SystemToast.SystemToastId.PACK_LOAD_FAILURE, Component.translatable("myResourcePack.notification.disabledDueToError.title"), Component.translatable("myResourcePack.notification.disabledDueToError.description"));
        failedWithActiveBlocking = false;
    }
}
