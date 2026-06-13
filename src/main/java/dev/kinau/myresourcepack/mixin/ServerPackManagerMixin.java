package dev.kinau.myresourcepack.mixin;

import com.google.common.hash.HashCode;
import dev.kinau.myresourcepack.MyResourcePack;
import dev.kinau.myresourcepack.expander.ServerDataExpander;
import net.minecraft.client.resources.server.PackLoadFeedback;
import net.minecraft.client.resources.server.ServerPackManager;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URL;
import java.util.UUID;

@Mixin(ServerPackManager.class)
public class ServerPackManagerMixin {

    @Shadow
    private ServerPackManager.PackPromptStatus packPromptStatus;

    @Shadow
    @Final
    private PackLoadFeedback packLoadFeedback;

    @Inject(method = "pushPack", at = @At("HEAD"), cancellable = true)
    private void onPushPack(UUID id, URL url, @Nullable HashCode hash, CallbackInfo ci) {
        if (packPromptStatus != ServerPackManager.PackPromptStatus.ALLOWED) return;
        if (!(MyResourcePack.getInstance().getPendingServerData() instanceof ServerDataExpander serverDataExpander)) return;
        if (serverDataExpander.myResourcePack$getPackStatus() == null) return;
        if (!serverDataExpander.myResourcePack$getPackStatus().getInternalName().equals(ServerDataExpander.MappedPackStatus.IGNORED.getInternalName())) return;
        ci.cancel();
        packLoadFeedback.reportUpdate(id, PackLoadFeedback.Update.ACCEPTED);
        packLoadFeedback.reportUpdate(id, PackLoadFeedback.Update.DOWNLOADED);
        packLoadFeedback.reportFinalResult(id, PackLoadFeedback.FinalResult.APPLIED);
    }
}
