package dev.kinau.myresourcepack.config.resource;

import dev.kinau.myresourcepack.MyResourcePack;
import dev.kinau.myresourcepack.config.ResourceAction;
import dev.kinau.myresourcepack.config.ResourceRule;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
@Setter
@Accessors(fluent = true)
@EqualsAndHashCode(of = "children", callSuper = true)
public class ResourceDirectory extends ResourceObject implements Cloneable {

    private final List<ResourceObject> children = new ArrayList<>();
    private ResourceAction ruleAction;

    public ResourceDirectory(ResourceLocation location) {
        super(location);
    }

    public ResourceDirectory getOrCreateDir(String part) {
        Optional<ResourceDirectory> optDir = children.stream()
                .filter(resourceObject -> resourceObject instanceof ResourceDirectory)
                .map(resourceObject -> (ResourceDirectory) resourceObject)
                .filter(resourceDirectory -> resourceDirectory.location().getPath().endsWith("/" + part) || resourceDirectory.location().getPath().equals(part))
                .findAny();
        if (optDir.isPresent())
            return optDir.get();
        String addedPath = location().getPath() + "/" + part;
        if (addedPath.startsWith("/"))
            addedPath = addedPath.substring(1);
        try {
            ResourceLocation resourceLocation = new ResourceLocation(location().getNamespace(), addedPath);
            ResourceDirectory addedDir = new ResourceDirectory(resourceLocation);
            children.add(addedDir);
            return addedDir;
        } catch (ResourceLocationException ex) {
            MyResourcePack.LOGGER.error("Could not create resource directory for {}", location().getNamespace() + ":" + addedPath);
        }
        return null;
    }

    public ResourceDirectory merge(ResourceObject object) {
        if (object instanceof ResourceFile) {
            if (children().stream().anyMatch(object1 -> object1.location().toString().equals(object.location().toString())))
                return this;
            addChild(object);
            return this;
        } else if (object instanceof ResourceDirectory dirToAdd) {
            Optional<ResourceObject> optChild = children().stream().filter(object1 -> object1.location().toString().equals(dirToAdd.location().toString())).findAny();
            if (optChild.isPresent()) {
                ResourceObject existing = optChild.get();
                if (existing instanceof ResourceFile) {
                    addChild(object);
                } else if (existing instanceof ResourceDirectory existingDir) {
                    dirToAdd.children().forEach(existingDir::merge);
                }
            } else {
                addChild(object);
                return this;
            }
        }
        return this;
    }

    public Optional<ResourceFile> findFile(ResourceLocation location) {
        for (ResourceObject child : children()) {
            if (child instanceof ResourceFile file && file.location().toString().equals(location.toString()))
                return Optional.of(file);
            else if (child instanceof ResourceDirectory dir) {
                Optional<ResourceFile> optFile = dir.findFile(location);
                if (optFile.isPresent())
                    return optFile;
            }
        }
        return Optional.empty();
    }

    public Optional<ResourceDirectory> findDirectory(ResourceLocation location) {
        if (location().toString().equals(location.toString()))
            return Optional.of(this);
        for (ResourceObject child : children()) {
            if (child instanceof ResourceDirectory dir) {
                if (dir.location().toString().equals(location.toString()))
                    return Optional.of(dir);
                Optional<ResourceDirectory> optDir = dir.findDirectory(location);
                if (optDir.isPresent())
                    return optDir;
            }
        }
        return Optional.empty();
    }

    public void applyRule(ResourceRule rule) {
        applyRule(rule, false);
    }

    public void applyRule(ResourceRule rule, boolean appliedAtParent) {
        if (!appliedAtParent) {
            if (rule.matches(location().toString())) {
                ruleAction(rule.action());
                appliedAtParent = true;
            }
        }
        boolean finalAppliedAtParent = appliedAtParent;
        children().forEach(object -> {
            if (rule.matches(object.location().toString())) {
                object.action(rule.action());
            }
            if (object instanceof ResourceDirectory dir) {
                dir.applyRule(rule, finalAppliedAtParent);
            }
        });
    }

    public List<ResourceRule> createRules(ResourceDirectory compareRoot) {
        return createRules(compareRoot, null);
    }

    private List<ResourceRule> createRules(ResourceDirectory compareRoot, ResourceAction ignoreRulesWith) {
        List<ResourceRule> rules = new ArrayList<>();
        Optional<ResourceDirectory> compDir = compareRoot.findDirectory(location());
        ResourceAction ruleAction = actionForRule();
        if (compDir.isPresent()) {
            ResourceAction compActionForRule = compDir.get().ruleAction();
            if (compActionForRule != null && compActionForRule != ignoreRulesWith && (ignoreRulesWith != null || !Objects.equals(compActionForRule, ruleAction))) {
                rules.add(new ResourceRule(compDir.get()));
                ignoreRulesWith = compActionForRule;
            }
        }
        ResourceAction finalIgnoreRulesWith = ignoreRulesWith;
        children().forEach(object -> {
            if (object instanceof ResourceFile file) {
                Optional<ResourceFile> compFile = compareRoot.findFile(file.location());
                if (compFile.isPresent()) {
                    ResourceAction compFileActionForRule = compFile.get().actionForRule();
                    if (compFileActionForRule != finalIgnoreRulesWith && (finalIgnoreRulesWith != null || !compFileActionForRule.equals(file.actionForRule()))) {
                        rules.add(new ResourceRule(compFile.get()));
                    }
                }
            } else if (object instanceof ResourceDirectory dir) {
                rules.addAll(dir.createRules(compareRoot, finalIgnoreRulesWith));
            }
        });
        return rules;
    }

    @Override
    protected void printTree(int tabIndex) {
        System.out.println("\t".repeat(tabIndex) + location().toString());
        children().forEach(resourceObject -> resourceObject.printTree(tabIndex + 1));
    }

    public void addChild(ResourceObject object) {
        children().add(object);
    }

    private void removeEmptyDirectories() {
        children().removeIf(resourceObject -> {
            return resourceObject instanceof ResourceDirectory resourceDirectory && !resourceDirectory.containsFiles();
        });
    }

    private void flatten() {
        removeEmptyDirectories();
        if (children().size() == 1 && children.get(0) instanceof ResourceDirectory childDir) {
            location(childDir.location());
            children().clear();
            children().addAll(childDir.children());
        }
        removeEmptyDirectories();
    }

    private void sort() {
        children().sort((o1, o2) -> {
            if (o1.getClass().equals(o2.getClass()))
                return o1.location().toString().compareTo(o2.location().toString());
            if (o1 instanceof ResourceDirectory && !(o2 instanceof ResourceDirectory))
                return -1;
            else
                return 1;
        });
    }

    public void flattenSort() {
        flatten();
        for (ResourceObject child : children()) {
            if (child instanceof ResourceDirectory childDir)
                childDir.flattenSort();
        }
        sort();
    }

    public ResourceDirectory filter(Predicate<ResourceObject> predicate) {
        ResourceDirectory clone = clone();
        clone.children().removeIf(predicate.negate());
        List<ResourceObject> newObjects = clone.children().stream().map(resourceObject -> {
            if (resourceObject instanceof ResourceDirectory resourceDirectory)
                return resourceDirectory.filter(predicate);
            return resourceObject;
        }).toList();
        clone.children().clear();
        clone.children().addAll(newObjects);
        return clone;
    }

    @Override
    public ResourceAction action() {
        if (children().isEmpty()) return null;
        ResourceAction action = children().get(0).action();
        if (action == null) return null;
        for (int i = 1; i < children().size(); i++) {
            if (children().get(i).action() != action)
                return null;
        }
        return action;
    }

    @Override
    public ResourceAction actionForRule() {
        if (ruleAction() != null)
            return ruleAction();
        return super.actionForRule();
    }

    @Override
    public void action(ResourceAction action) {
        children().forEach(object -> object.action(action));
    }

    @Override
    public boolean supportsMerging() {
        return children().stream().allMatch(ResourceObject::supportsMerging);
    }

    public boolean containsFiles() {
        if (children().isEmpty()) return false;
        return children().stream().anyMatch(resourceObject -> {
            if (resourceObject instanceof ResourceDirectory resourceDirectory)
                return resourceDirectory.containsFiles();
            return true;
        });
    }

    @Override
    public ResourceDirectory clone() {
        ResourceDirectory clone = new ResourceDirectory(location());
        clone.children().addAll(children().stream().map(ResourceObject::clone).toList());
        return clone;
    }
}