package dev.kinau.myresourcepack.mixin;

import com.google.common.collect.ImmutableList;
import dev.kinau.myresourcepack.MyResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Options.class)
public abstract class OptionsMixin {

    @Shadow
    public List<String> resourcePacks;

    @Inject(method = "updateResourcePacks", at = @At("HEAD"), cancellable = true)
    private void onUpdateResourcePacks(PackRepository packRepository, CallbackInfo ci) {
        ImmutableList<String> prevPacksList = ImmutableList.copyOf(this.resourcePacks);
        List<String> newResourcePacks = new ArrayList<>();
        for (Pack pack : packRepository.getSelectedPacks()) {
            if (pack.isFixedPosition()) continue;
            newResourcePacks.add(pack.getId());
        }
        ImmutableList<String> updatedPacksList = ImmutableList.copyOf(newResourcePacks);
        if (updatedPacksList.equals(prevPacksList) && MyResourcePack.getInstance().isReloadResources()) {
            MyResourcePack.getInstance().setReloadResources(false);
            Minecraft.getInstance().reloadResourcePacks();
        }

    }
}
