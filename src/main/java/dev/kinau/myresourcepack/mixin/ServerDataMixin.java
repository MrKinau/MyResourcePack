package dev.kinau.myresourcepack.mixin;

import dev.kinau.myresourcepack.expander.ServerDataExpander;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ServerData.class)
public class ServerDataMixin implements ServerDataExpander {

    @Unique
    private MappedPackStatus myResourcePack$packStatus;

    @Inject(method = "read", at = @At("RETURN"))
    private static void onRead(CompoundTag tag, CallbackInfoReturnable<ServerData> cir) {
        ServerData serverData = cir.getReturnValue();
        if (serverData instanceof ServerDataExpander serverDataExpander) {
            Optional<String> packStatus = tag.getString("myResourcePack$packStatus");
            packStatus.ifPresent(s -> serverDataExpander.myResourcePack$setPackStatus(MappedPackStatus.getByInternalName(s)));
        }
    }

    @Inject(method = "write", at = @At("RETURN"), cancellable = true)
    private void onWrite(CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        if (myResourcePack$getPackStatus() != null) {
            tag.putString("myResourcePack$packStatus", myResourcePack$getPackStatus().getInternalName());
        }
        cir.setReturnValue(tag);
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void onWrite(ServerData editingServer, CallbackInfo ci) {
        if (editingServer instanceof ServerDataExpander serverDataExpander) {
            myResourcePack$setPackStatus(serverDataExpander.myResourcePack$getPackStatus());
        }
    }

    @Override
    public MappedPackStatus myResourcePack$getPackStatus() {
        return myResourcePack$packStatus;
    }

    @Override
    public void myResourcePack$setPackStatus(MappedPackStatus packStatus) {
        this.myResourcePack$packStatus = packStatus;
    }
}
