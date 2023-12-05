package dev.kinau.myresourcepack.config.resource;

import dev.kinau.myresourcepack.config.ResourceAction;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(of = "location")
public abstract class ResourceObject implements Cloneable {

    @Setter
    private ResourceLocation location;

    public void printTree() {
        printTree(0);
    }

    protected abstract void printTree(int tabIndex);

    public abstract ResourceAction action();
    public ResourceAction actionForRule() {
        return action();
    }
    public abstract void action(ResourceAction action);

    public abstract boolean supportsMerging();

    @Override
    public ResourceObject clone() {
        try {
            ResourceObject clone = (ResourceObject) super.clone();
            clone.location(location());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}