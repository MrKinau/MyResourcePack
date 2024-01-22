package dev.kinau.myresourcepack.mixin;

import dev.kinau.myresourcepack.config.ResourceAction;
import dev.kinau.myresourcepack.config.VanillaResourceAction;
import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.config.resource.ResourceFile;
import dev.kinau.myresourcepack.expander.PackResourceExpander;
import dev.kinau.myresourcepack.utils.ResourceBlockingUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Mixin(FilePackResources.class)
public abstract class FilePackResourcesMixin implements PackResourceExpander {

	private static String getPathFromLocation(PackType packType, ResourceLocation resourceLocation) {
		return String.format(Locale.ROOT, "%s/%s/%s", packType.getDirectory(), resourceLocation.getNamespace(), resourceLocation.getPath());
	}

	@Shadow
	private ZipFile zipFile;

	@Inject(method = "getResource*", at = @At("HEAD"), cancellable = true)
	private void onGetResource(PackType packType, ResourceLocation resourceLocation, CallbackInfoReturnable<IoSupplier<InputStream>> cir) {
		if (!ResourceBlockingUtils.isBlockingEnabled()) return;
		if (((FilePackResources)(Object)this).packId().equals("server") && packType == PackType.CLIENT_RESOURCES) {
//			long start = System.currentTimeMillis();
			ResourceAction action = ResourceBlockingUtils.getConfiguredResourceAction(resourceLocation);
			if (action != ResourceAction.PASS) {
				if (action == ResourceAction.MERGE) {
					ZipFile zipFile = this.zipFile;
					if (zipFile == null) {
						cir.setReturnValue(null);
						return;
					}
					ZipEntry zipEntry = zipFile.getEntry(getPathFromLocation(packType, resourceLocation));
					if (zipEntry == null) {
						cir.setReturnValue(null);
						return;
					}
					cir.setReturnValue(ResourceBlockingUtils.mergeData(resourceLocation, IoSupplier.create(zipFile, zipEntry)));
//					System.out.println(resourceLocation.toString() + " took " + (System.currentTimeMillis() - start) + "ms");
					return;
				}
//				System.out.println(resourceLocation.toString() + " took " + (System.currentTimeMillis() - start) + "ms");
				cir.cancel();
				return;
			}
//			System.out.println(resourceLocation.toString() + " took " + (System.currentTimeMillis() - start) + "ms");
		}
	}

	@Inject(method = "listResources", at = @At("HEAD"), cancellable = true)
	private void onListResources(PackType packType, String namespace, String path, PackResources.ResourceOutput resourceOutput, CallbackInfo ci) {
		if (!ResourceBlockingUtils.isBlockingEnabled()) return;
		if (((FilePackResources)(Object)this).packId().equals("server") && packType == PackType.CLIENT_RESOURCES) {
//			long start = System.currentTimeMillis();
			try {
				ZipFile zipFile = this.zipFile;

				if (zipFile == null) {
					return;
				}
				Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
				String string3 = packType.getDirectory() + "/" + namespace + "/";
				String string4 = string3 + path + "/";
				while (enumeration.hasMoreElements()) {
					String string5;
					ZipEntry zipEntry = enumeration.nextElement();
					if (zipEntry.isDirectory() || !(string5 = zipEntry.getName()).startsWith(string4)) continue;
					String string6 = string5.substring(string3.length());
					ResourceLocation resourceLocation = ResourceLocation.tryBuild(namespace, string6);
					if (resourceLocation != null) {
						Optional<IoSupplier<InputStream>> resource = Optional.empty();
						ResourceAction action = ResourceBlockingUtils.getConfiguredResourceAction(resourceLocation);
						if (action != ResourceAction.PASS) {
							if (action == ResourceAction.MERGE)
								resource = Optional.of(ResourceBlockingUtils.mergeData(resourceLocation, IoSupplier.create(zipFile, zipEntry)));
							else
								continue;
						}
						resourceOutput.accept(resourceLocation, resource.orElse(IoSupplier.create(zipFile, zipEntry)));
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
//			System.out.println(namespace + "/" + path + " took " + (System.currentTimeMillis() - start) + "ms");
			ci.cancel();
        }
	}

	@Override
	public ResourceDirectory myResourcePack$createResourceTree(PackType packType, String namespace) {
		ZipFile zipFile = this.zipFile;
		ResourceDirectory root = new ResourceDirectory(new ResourceLocation(namespace, ""));
		if (zipFile == null) {
			return root;
		}
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		String prefixedNamespace = packType.getDirectory() + "/" + namespace;
		while (entries.hasMoreElements()) {
			String currName;
			ZipEntry zipEntry = entries.nextElement();
			if (zipEntry.isDirectory() || !(currName = zipEntry.getName()).startsWith(prefixedNamespace)) continue;

			String pathWithoutNamespace = currName.substring(prefixedNamespace.length());
			String[] parts = pathWithoutNamespace.split("/");
			if (pathWithoutNamespace.startsWith("/"))
				pathWithoutNamespace = pathWithoutNamespace.substring(1);
			ResourceDirectory currentDir = root;
			for (int i = 0; i < parts.length - 1; i++) {
				if (parts[i].isEmpty()) continue;
				currentDir = currentDir.getOrCreateDir(parts[i]);
			}

			ResourceLocation resourceLocation = ResourceLocation.tryBuild(namespace, pathWithoutNamespace);
			if (currentDir != null && resourceLocation != null) {
				VanillaResourceAction action = ResourceBlockingUtils.getDefaultResourceAction(resourceLocation);
				currentDir.children().add(new ResourceFile(resourceLocation, action.action(), action.overridesVanilla()));
			}
		}
		return root;
	}
}