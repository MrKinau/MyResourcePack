package dev.kinau.myresourcepack.mixin;

import dev.kinau.myresourcepack.MyResourcePack;
import dev.kinau.myresourcepack.config.Config;
import dev.kinau.myresourcepack.config.ServerSettings;
import dev.kinau.myresourcepack.expander.ServerDataExpander;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerData.class)
public class ServerDataMixin implements ServerDataExpander {

    @Unique
    private MappedPackStatus myResourcePack$packStatus;

    @Unique
    private static ServerSettings getServerSetting(ServerData serverData, boolean createIfMissing) {
        if (serverData == null) return null;
        Config config = MyResourcePack.getInstance().getPackSettings().getConfigData();
        if (config == null) {
            MyResourcePack.LOGGER.error("THIS SHOULD NOT HAPPEN");
            return null;
        }
        return config.getSettings(serverData.ip, createIfMissing);
    }

    @Unique
    private static boolean ignoreAllPacks(ServerData serverData) {
        ServerSettings settings = getServerSetting(serverData, false);
        if (settings == null) return false;
        return settings.ignoreAllPacks();
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void onRead(CompoundTag tag, CallbackInfoReturnable<ServerData> cir) {
        ServerData serverData = cir.getReturnValue();
        if (serverData instanceof ServerDataExpander serverDataExpander) {
            if (!ignoreAllPacks(serverData)) return;
            MyResourcePack.LOGGER.info("Ignoring all packs for {}", serverData.ip);
            serverDataExpander.myResourcePack$setPackStatus(MappedPackStatus.IGNORED);
        }
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void onWrite(CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        if (myResourcePack$getPackStatus() != null) {
            ServerData serverData = (ServerData) (Object) this;
            ServerSettings settings = getServerSetting(serverData, false);
            if (settings == null) {
                if (!myResourcePack$getPackStatus().getInternalName().equals(MappedPackStatus.IGNORED.getInternalName()))
                    return;
                settings = getServerSetting(serverData, true);
                if (settings == null) return;
            }
            boolean ignoreAllPacks = settings.ignoreAllPacks();
            boolean newValue = myResourcePack$getPackStatus().getInternalName().equals(MappedPackStatus.IGNORED.getInternalName());
            if (ignoreAllPacks == newValue) return;
            settings.ignoreAllPacks(newValue);
            MyResourcePack.getInstance().getPackSettings().saveConfigPrintError();
            tag.putString("myResourcePack$packStatus", myResourcePack$getPackStatus().getInternalName());
        }
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void onCopyFrom(ServerData editingServer, CallbackInfo ci) {
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
