package dev.kinau.myresourcepack.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import dev.kinau.myresourcepack.MyResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ResourceBlockingUtils {

    public static boolean isBlockingEnabled() {
        return !MyResourcePack.getInstance().getPackSettings().getConfigData().getSettings(MyResourcePack.getInstance().getCurrentServer()).overrideTextures();
    }

    public static boolean shouldModify(ResourceLocation resourceLocation) {
        if (!resourceLocation.getNamespace().equals("minecraft"))
            return false;
        if (resourceLocation.getPath().startsWith("models/"))
            return true;
        if (resourceLocation.getPath().startsWith("lang/"))
            return true;
        return false;
    }

    public static boolean shouldBlockOrModify(ResourceLocation resourceLocation) {
        if (!resourceLocation.getNamespace().equals("minecraft"))
            return false;
        if (resourceLocation.getPath().startsWith("shaders/"))
            return false;
        if (resourceLocation.getPath().startsWith("atlases/"))
            return false;
        if (resourceLocation.getPath().startsWith("blockstates/"))
            return false;
        //TODO: Make acceptable changes configurable
        if (resourceLocation.getPath().startsWith("textures/gui/sprites/boss_bar/"))
            return false;

        boolean overridesVanilla = true;
        try (PackResources vanillaResource = Minecraft.getInstance().getResourcePackRepository().getPack(BuiltInPackSource.VANILLA_ID).open()) {
            if (vanillaResource.getResource(PackType.CLIENT_RESOURCES, resourceLocation) == null) {
                if (resourceLocation.getPath().endsWith(PackResources.METADATA_EXTENSION)) {
                    ResourceLocation baseLoc = ResourceLocation.tryBuild(resourceLocation.getNamespace(), resourceLocation.getPath().substring(0, resourceLocation.getPath().length() - PackResources.METADATA_EXTENSION.length()));
                    if (vanillaResource.getResource(PackType.CLIENT_RESOURCES, baseLoc) == null)
                        overridesVanilla = false;
                } else {
                    overridesVanilla = false;
                }
            }
        }

        if (overridesVanilla && resourceLocation.getPath().startsWith("font/")) {
            // only allow overriding unifont (e.g. to add characters into the default font)
            // Although it is bad practice to add characters to the default font instead of
            // adding them to a new font, too many servers do this and blocking all characters
            // might end in UIs not showing up. This does not override the default minecraft font
            // (at least not the ascii, nonlatin_european and accented glyph providers).
            return resourceLocation.getPath().equals("include/unifont.zip");
        }

        return overridesVanilla;
    }

    private static ModelTextureData getModelTextures(InputStream inputStream) {
        if (inputStream == null)
            return null;
        JsonElement element = JsonParser.parseReader(new JsonReader(new InputStreamReader(inputStream)));
        if (element.isJsonObject()) {
            JsonObject rootObj = element.getAsJsonObject();
            if (rootObj.has("textures") && rootObj.get("textures").isJsonObject()) {
                JsonObject textures = rootObj.getAsJsonObject("textures");
                return new ModelTextureData(rootObj, textures);
            }
        }
        return null;
    }

    public static IoSupplier<InputStream> modifyModelData(ResourceLocation resourceLocation, IoSupplier<InputStream> original) {
        return () -> {
            try {
                ModelTextureData textures = getModelTextures(original.get());
                if (textures != null) {
                    try (PackResources vanillaResource = Minecraft.getInstance().getResourcePackRepository().getPack(BuiltInPackSource.VANILLA_ID).open()) {
                        ModelTextureData vanillaTextures = getModelTextures(vanillaResource.getResource(PackType.CLIENT_RESOURCES, resourceLocation).get());
                        if (!textures.textures().equals(vanillaTextures.textures())) {
                            JsonObject root = textures.root();
                            GsonTools.extendJsonObject(vanillaTextures.textures(), GsonTools.ConflictStrategy.PREFER_FIRST_OBJ, textures.textures());
                            root.add("textures", vanillaTextures.textures());
                            return new ByteArrayInputStream(root.toString().getBytes(StandardCharsets.UTF_8));
                        }
                    }
                }
                return original.get();
            } catch (Throwable e) {
                return original.get();
            }
        };
    }

    private static JsonObject getJsonData(InputStream inputStream) {
        if (inputStream == null)
            return null;
        JsonElement element = JsonParser.parseReader(new JsonReader(new InputStreamReader(inputStream)));
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        return null;
    }

    private static IoSupplier<InputStream> modifyJsonData(ResourceLocation resourceLocation, IoSupplier<InputStream> original) {
        return () -> {
            try {
                JsonObject jsonData = getJsonData(original.get());
                if (jsonData != null) {
                    try (PackResources vanillaResource = Minecraft.getInstance().getResourcePackRepository().getPack(BuiltInPackSource.VANILLA_ID).open()) {
                        JsonObject vanillaJsonData = getJsonData(vanillaResource.getResource(PackType.CLIENT_RESOURCES, resourceLocation).get());
                        if (vanillaJsonData != null) {
                            GsonTools.extendJsonObject(jsonData, GsonTools.ConflictStrategy.PREFER_SECOND_OBJ, vanillaJsonData);
                            return new ByteArrayInputStream(jsonData.toString().getBytes(StandardCharsets.UTF_8));
                        }
                    }
                }
                return original.get();
            } catch (Throwable e) {
                return original.get();
            }
        };
    }

    public static IoSupplier<InputStream> modifyData(ResourceLocation resourceLocation, IoSupplier<InputStream> original) {
        if (resourceLocation.getPath().startsWith("models/") && resourceLocation.getPath().endsWith(".json")) {
            return modifyModelData(resourceLocation, original);
        } else if (resourceLocation.getPath().startsWith("lang/") && resourceLocation.getPath().endsWith(".json")) {
            return modifyJsonData(resourceLocation, original);
        }
        return original;
    }

    record ModelTextureData(JsonObject root, JsonObject textures) {
    }
}
