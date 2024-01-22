package dev.kinau.myresourcepack.mixin;

import dev.kinau.myresourcepack.MyResourcePack;
import dev.kinau.myresourcepack.config.ServerSetting;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {

    @Inject(method = "rebuildSelected", at = @At("RETURN"), cancellable = true)
    private void onRebuildSelected(Collection<String> collection, CallbackInfoReturnable<List<Pack>> cir) {
        List<Pack> packs = cir.getReturnValue();

        if (MyResourcePack.getInstance().getCurrentServer() == null) return;

        // save new pack order
        if (MyResourcePack.getInstance().isConfiguringPackOrder()) {
            MyResourcePack.getInstance().setConfiguringPackOrder(false);
            for (int i = 0; i < packs.size(); i++) {
                Pack pack = packs.get(i);
                if (pack.getPackSource() != PackSource.SERVER) continue;

                boolean onlyServerPacksAbove = true;
                for (int j = packs.size() - 1; j > i; j--) {
                    if (packs.get(j).getPackSource() != PackSource.SERVER) {
                        onlyServerPacksAbove = false;
                        break;
                    }
                }

                boolean onlyServerPacksBelow = true;
                for (int j = i -1; j >= 0; j--) {
                    if (packs.get(j).getPackSource() != PackSource.SERVER) {
                        onlyServerPacksBelow = false;
                        break;
                    }
                }

                ServerSetting setting = MyResourcePack.getInstance().getPackSettings().getConfigData().getSettings(MyResourcePack.getInstance().getCurrentServer());
                if (onlyServerPacksAbove)
                    setting.packOrder().remove(getServerPackId(pack));
                else if (onlyServerPacksBelow)
                    setting.packOrder().put(getServerPackId(pack), 0);
                else
                    setting.packOrder().put(getServerPackId(pack), i);
            }
            try {
                MyResourcePack.getInstance().getPackSettings().saveConfig();
            } catch (IOException e) {
                MyResourcePack.LOGGER.error("Could not save config", e);
            }
            return;
        }

        // apply saved pack order
        cir.setReturnValue(reorderPacks(packs));
    }

    private List<Pack> reorderPacks(List<Pack> packs) {
        List<Pack> reorderedList = new ArrayList<>(packs);
        ServerSetting setting = MyResourcePack.getInstance().getPackSettings().getConfigData().getSettings(MyResourcePack.getInstance().getCurrentServer());
        setting.packOrder().forEach((packId, orderIndex) -> {
            reorderedList.stream().filter(pack -> getServerPackId(pack).equals(packId)).findAny().ifPresent(pack -> {
                reorderedList.remove(pack);
                reorderedList.add(orderIndex, pack);
            });
        });
        return reorderedList;
    }

    private String getServerPackId(Pack pack) {
        if (pack.getPackSource() != PackSource.SERVER)
            return pack.getId();
        int lastSlashIndex = pack.getId().lastIndexOf('/');
        if (lastSlashIndex < 0)
            return pack.getId();
        return pack.getId().substring(lastSlashIndex + 1);
    }
}
