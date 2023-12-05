package dev.kinau.myresourcepack.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import dev.kinau.myresourcepack.MyResourcePack;
import dev.kinau.myresourcepack.config.ResourceAction;
import dev.kinau.myresourcepack.config.ResourceRule;
import dev.kinau.myresourcepack.config.ServerSetting;
import dev.kinau.myresourcepack.config.VanillaResourceAction;
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
import java.util.List;
import java.util.Optional;

public class ResourceBlockingUtils {

    public static boolean isBlockingEnabled() {
        String currentServer = MyResourcePack.getInstance().getCurrentServer();
        if (currentServer == null) return false;
        return !MyResourcePack.getInstance().getPackSettings().getConfigData().getSettings(currentServer).overrideTextures();
    }

    public static boolean supportsMerging(ResourceLocation resourceLocation) {
        if (resourceLocation == null) return false;
        return resourceLocation.getPath().endsWith(".json");
    }

    public static Optional<ServerSetting> getServerSetting() {
        String currentServer = MyResourcePack.getInstance().getCurrentServer();
        if (currentServer == null) return Optional.empty();
        return Optional.of(MyResourcePack.getInstance().getPackSettings().getConfigData().getSettings(currentServer));
    }

    public static ResourceAction getConfiguredResourceAction(ResourceLocation resourceLocation) {
        Optional<ServerSetting> optSettings = getServerSetting();
        if (optSettings.isEmpty()) return ResourceAction.PASS;
        ServerSetting setting = optSettings.get();
        if (setting.overrideTextures()) return ResourceAction.PASS;

        String path = resourceLocation.toString();

        VanillaResourceAction resourceAction = getDefaultResourceAction(resourceLocation);
        List<ResourceRule> rules = resourceAction.overridesVanilla()
                ? setting.overrideRules()
                : setting.additionRules();

        for (int i = rules.size() - 1; i >= 0; i--) {
            ResourceRule rule = rules.get(i);
            if (rule.matches(path))
                return rule.action();
        }

        return resourceAction.action();
    }

    public static VanillaResourceAction getDefaultResourceAction(ResourceLocation resourceLocation) {
        if (!resourceLocation.getNamespace().equals("minecraft"))
            return new VanillaResourceAction(ResourceAction.PASS, false);

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

        if (resourceLocation.getPath().startsWith("shaders/"))
            return new VanillaResourceAction(ResourceAction.PASS, overridesVanilla);
        if (resourceLocation.getPath().startsWith("atlases/"))
            return new VanillaResourceAction(ResourceAction.PASS, overridesVanilla);
        if (resourceLocation.getPath().startsWith("blockstates/"))
            return new VanillaResourceAction(ResourceAction.PASS, overridesVanilla);
        if (resourceLocation.getPath().startsWith("textures/gui/sprites/boss_bar/"))
            return new VanillaResourceAction(ResourceAction.PASS, overridesVanilla);
        if (resourceLocation.getPath().startsWith("textures/misc/pumpkinblur.png"))
            return new VanillaResourceAction(ResourceAction.PASS, overridesVanilla);

        if (overridesVanilla) {
            // only allow overriding unifont (e.g. to add characters into the default font)
            // Although it is bad practice to add characters to the default font instead of
            // adding them to a new font, too many servers do this and blocking all characters
            // might end in UIs not showing up. This does not override the default minecraft font
            // (at least not the ascii, nonlatin_european and accented glyph providers).
            if (resourceLocation.getPath().startsWith("font/"))
                return resourceLocation.getPath().equals("include/unifont.zip")
                        ? new VanillaResourceAction(ResourceAction.BLOCK, true)
                        : new VanillaResourceAction(ResourceAction.PASS, true);
            if (resourceLocation.getPath().startsWith("models/"))
                return new VanillaResourceAction(ResourceAction.MERGE, true);
            if (resourceLocation.getPath().startsWith("lang/"))
                return new VanillaResourceAction(ResourceAction.MERGE, true);
            if (resourceLocation.getPath().equals("sounds.json"))
                return new VanillaResourceAction(ResourceAction.MERGE, true);
        }

        return overridesVanilla ? new VanillaResourceAction(ResourceAction.BLOCK, true) : new VanillaResourceAction(ResourceAction.PASS, false);
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

    public static IoSupplier<InputStream> mergeModelData(ResourceLocation resourceLocation, IoSupplier<InputStream> original) {
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

    private static IoSupplier<InputStream> mergeJsonData(ResourceLocation resourceLocation, IoSupplier<InputStream> original) {
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

    public static IoSupplier<InputStream> mergeData(ResourceLocation resourceLocation, IoSupplier<InputStream> original) {
        if (resourceLocation.getPath().startsWith("models/") && resourceLocation.getPath().endsWith(".json")) {
            return mergeModelData(resourceLocation, original);
        } else if (resourceLocation.getPath().startsWith("lang/") && resourceLocation.getPath().endsWith(".json")) {
            return mergeJsonData(resourceLocation, original);
        } else if (resourceLocation.getPath().equals("sounds.json")) {
            return mergeJsonData(resourceLocation, original);
        }
        return original;
    }

    record ModelTextureData(JsonObject root, JsonObject textures) {
    }
}
