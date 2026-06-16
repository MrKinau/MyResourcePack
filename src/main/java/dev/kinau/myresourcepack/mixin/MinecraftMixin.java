package dev.kinau.myresourcepack.mixin;

import dev.kinau.myresourcepack.MyResourcePack;
import dev.kinau.myresourcepack.config.ServerSettings;
import net.minecraft.client.GameLoadCookie;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Unique
    private boolean failedWithActiveBlocking = false;

    @Inject(method = "clearResourcePacksOnError", at = @At(value = "HEAD"), cancellable = true)
    public void onClearResourcePacksOnError(Throwable t, @Nullable Component message, @Nullable GameLoadCookie loadCookie, CallbackInfo ci) {
        String server = MyResourcePack.getInstance().getCurrentServer();
        if (server == null) return;
        ServerSettings setting = MyResourcePack.getInstance().getPackSettings().getConfigData().getSettings(server, false);
        if (setting == null || setting.overrideTextures()) return;
        setting.overrideTextures(true);
        this.failedWithActiveBlocking = true;
        Minecraft.getInstance().execute(() -> {
            SystemToast.add(Minecraft.getInstance().gui.toastManager(), SystemToast.SystemToastId.PACK_LOAD_FAILURE, Component.translatable("resourcePack.load_fail"), Component.translatable("myResourcePack.notification.disabledDueToError"));
        });
    }

    @Inject(method = "addResourcePackLoadFailToast", at = @At(value = "RETURN"), cancellable = true)
    public void onAddResourcePackLoadFailToast(@Nullable Component description, CallbackInfo ci) {
        if (failedWithActiveBlocking)
            SystemToast.add(Minecraft.getInstance().gui.toastManager(), SystemToast.SystemToastId.PACK_LOAD_FAILURE, Component.translatable("myResourcePack.notification.disabledDueToError.title"), Component.translatable("myResourcePack.notification.disabledDueToError.description"));
        this.failedWithActiveBlocking = false;
    }
}
