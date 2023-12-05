package dev.kinau.myresourcepack.config.resource;

import dev.kinau.myresourcepack.config.ResourceAction;
import dev.kinau.myresourcepack.utils.ResourceBlockingUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

@Getter
@Setter
@Accessors(fluent = true)
@EqualsAndHashCode(of = "action", callSuper = true)
public class ResourceFile extends ResourceObject implements Cloneable {

    private ResourceAction action;
    private final boolean overridesVanilla;

    public ResourceFile(ResourceLocation location, ResourceAction action, boolean overridesVanilla) {
        super(location);
        this.action = action;
        this.overridesVanilla = overridesVanilla;
    }

    public void action(ResourceAction action) {
        this.action = action;
    }

    @Override
    protected void printTree(int tabIndex) {
        System.out.println("\t".repeat(tabIndex) + location().toString());
    }

    @Override
    public boolean supportsMerging() {
        return ResourceBlockingUtils.supportsMerging(location());
    }

    @Override
    public ResourceFile clone() {
        return new ResourceFile(location(), action(), overridesVanilla());
    }
}