package dev.kinau.myresourcepack.expander;

import lombok.Getter;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.Arrays;
import java.util.LinkedHashSet;

public interface ServerDataExpander {

    MappedPackStatus myResourcePack$getPackStatus();
    void myResourcePack$setPackStatus(MappedPackStatus packStatus);
    void myResourcePack$setPackStatusWithoutDirtyMark(MappedPackStatus packStatus);

    boolean myResourcePack$isDirty();

    @Getter
    class MappedPackStatus {

        private final String internalName;
        private final Component name;
        private final ServerData.ServerPackStatus mappedStatus;

        public MappedPackStatus(String name, ServerData.ServerPackStatus mappedStatus) {
            this.internalName = name;
            this.name = Component.translatable("manageServer.resourcePack." + name);
            this.mappedStatus = mappedStatus;
        }

        public MappedPackStatus(ServerData.ServerPackStatus serverPackStatus) {
            this.name = serverPackStatus.getName();
            this.mappedStatus = serverPackStatus;
            if (serverPackStatus.getName().getContents() instanceof TranslatableContents translatableContents) {
                String[] parts = translatableContents.getKey().split("\\.");
                this.internalName = parts[parts.length - 1];
            } else {
                this.internalName = serverPackStatus.getName().getString();
            }
        }

        public static MappedPackStatus getByMappedStatus(ServerData.ServerPackStatus packStatus) {
            return VALUES.stream().filter(status -> status.getMappedStatus().equals(packStatus)).findFirst().orElse(IGNORED);
        }

        public static MappedPackStatus getByInternalName(String internalName) {
            return VALUES.stream().filter(status -> status.getInternalName().equals(internalName)).findFirst().orElse(null);
        }

        public static MappedPackStatus IGNORED = new MappedPackStatus("ignored", ServerData.ServerPackStatus.ENABLED);
        public static LinkedHashSet<MappedPackStatus> VALUES = new LinkedHashSet<>();
        static {
            VALUES.addAll(Arrays.stream(ServerData.ServerPackStatus.values()).map(MappedPackStatus::new).toList());
            VALUES.add(IGNORED);
        }
    }
}
