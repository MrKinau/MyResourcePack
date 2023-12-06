package dev.kinau.myresourcepack.mixin;

import dev.kinau.myresourcepack.expander.ClientCommonPacketListenerImplExpander;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin implements ClientCommonPacketListenerImplExpander {

    @Override
    @Accessor
    public abstract ServerData getServerData();
}
