package dev.kinau.myresourcepack.mixin;

import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.config.resource.ResourceObject;
import dev.kinau.myresourcepack.expander.PackResourceExpander;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(CompositePackResources.class)
public abstract class CompositePackResourcesMixin implements PackResourceExpander {

	@Shadow
	@Final
	private List<PackResources> packResourcesStack;

	@Override
	public ResourceDirectory myResourcePack$createResourceTree(PackType packType, String namespace) {
		ResourceDirectory resourceDirectory = new ResourceDirectory(new ResourceLocation(namespace, ""));
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