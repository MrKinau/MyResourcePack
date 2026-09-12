package dev.kinau.myresourcepack.mixin;

import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.config.resource.ResourceObject;
import dev.kinau.myresourcepack.expander.PackResourceExpander;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.OverlayedPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(OverlayedPackResources.class)
public abstract class OverlayedPackResourcesMixin implements PackResourceExpander {

	@Shadow
	@Final
	private List<PackResources> packResourcesStack;

	@Override
	public ResourceDirectory myResourcePack$createResourceTree(PackType packType, String namespace) {
		ResourceDirectory resourceDirectory = new ResourceDirectory(Identifier.fromNamespaceAndPath(namespace, ""));
		for (PackResources packResources : packResourcesStack) {
			if (packResources instanceof PackResourceExpander) {
				ResourceDirectory childDir = ((PackResourceExpander) packResources).myResourcePack$createResourceTree(packType, namespace);
				for (ResourceObject child : childDir.children()) {
					resourceDirectory.merge(child);
				}
			}
		}
		return resourceDirectory;
	}
}