package dev.kinau.myresourcepack.mixin;

import dev.kinau.myresourcepack.utils.ResourceBlockingUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.spongepowered.asm.mixin.Final;
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
public class FilePackResourcesMixin {

	private static String getPathFromLocation(PackType packType, ResourceLocation resourceLocation) {
		return String.format(Locale.ROOT, "%s/%s/%s", packType.getDirectory(), resourceLocation.getNamespace(), resourceLocation.getPath());
	}

	@Shadow
	@Final
	private FilePackResources.SharedZipFileAccess zipFileAccess;

	@Shadow
	private String addPrefix(String string) {
		return "";
	}

	@Inject(method = "getResource*", at = @At("HEAD"), cancellable = true)
	private void onGetResource(PackType packType, ResourceLocation resourceLocation, CallbackInfoReturnable<IoSupplier<InputStream>> cir) {
		if (!ResourceBlockingUtils.isBlockingEnabled()) return;
		if (((FilePackResources)(Object)this).packId().equals("server") && packType == PackType.CLIENT_RESOURCES) {
			if (ResourceBlockingUtils.shouldBlockOrModify(resourceLocation)) {
				if (ResourceBlockingUtils.shouldModify(resourceLocation)) {
					ZipFile zipFile = this.zipFileAccess.getOrCreateZipFile();
					if (zipFile == null) {
						cir.setReturnValue(null);
						return;
					}
					ZipEntry zipEntry = zipFile.getEntry(this.addPrefix(getPathFromLocation(packType, resourceLocation)));
					if (zipEntry == null) {
						cir.setReturnValue(null);
						return;
					}
					cir.setReturnValue(ResourceBlockingUtils.modifyData(resourceLocation, IoSupplier.create(zipFile, zipEntry)));
					return;
				}
				cir.cancel();
				return;
			}
		}
	}

	@Inject(method = "listResources", at = @At("HEAD"), cancellable = true)
	private void onListResources(PackType packType, String namespace, String path, PackResources.ResourceOutput resourceOutput, CallbackInfo ci) {
		if (!ResourceBlockingUtils.isBlockingEnabled()) return;
		if (((FilePackResources)(Object)this).packId().equals("server") && packType == PackType.CLIENT_RESOURCES) {
			try {
				ZipFile zipFile = zipFileAccess.getOrCreateZipFile();

				if (zipFile == null) {
					return;
				}
				Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
				String string3 = this.addPrefix(packType.getDirectory() + "/" + namespace + "/");
				String string4 = string3 + path + "/";
				while (enumeration.hasMoreElements()) {
					String string5;
					ZipEntry zipEntry = enumeration.nextElement();
					if (zipEntry.isDirectory() || !(string5 = zipEntry.getName()).startsWith(string4)) continue;
					String string6 = string5.substring(string3.length());
					ResourceLocation resourceLocation = ResourceLocation.tryBuild(namespace, string6);
					if (resourceLocation != null) {
						Optional<IoSupplier<InputStream>> resource = Optional.empty();
						if (ResourceBlockingUtils.shouldBlockOrModify(resourceLocation)) {
							if (ResourceBlockingUtils.shouldModify(resourceLocation))
								resource = Optional.of(ResourceBlockingUtils.modifyData(resourceLocation, IoSupplier.create(zipFile, zipEntry)));
							else
								continue;
						}
						resourceOutput.accept(resourceLocation, resource.orElse(IoSupplier.create(zipFile, zipEntry)));
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			ci.cancel();
        }
	}
}