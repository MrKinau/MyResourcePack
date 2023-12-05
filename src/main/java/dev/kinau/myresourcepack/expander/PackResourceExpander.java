package dev.kinau.myresourcepack.expander;

import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import net.minecraft.server.packs.PackType;

public interface PackResourceExpander {

    default ResourceDirectory myResourcePack$createResourceTree(PackType packType, String namespace) {
        return null;
    }

}
