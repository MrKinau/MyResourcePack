package dev.kinau.myresourcepack.config;

import dev.kinau.myresourcepack.config.resource.ResourceDirectory;
import dev.kinau.myresourcepack.config.resource.ResourceFile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.regex.Pattern;

@NoArgsConstructor
@Getter
@Accessors(fluent = true)
public class ResourceRule {

    private String rule;
    private ResourceAction action;
    private transient Pattern pattern;

    public ResourceRule(ResourceFile resourceFile) {
        this("^" + Pattern.quote(resourceFile.location().toString()) + "$", resourceFile.actionForRule());
    }

    public ResourceRule(ResourceDirectory resourceDirectory) {
        this("^(" + Pattern.quote(resourceDirectory.location().toString() + "/") + ".*|" + Pattern.quote(resourceDirectory.location().toString()) + "$)", resourceDirectory.actionForRule());
    }

    public ResourceRule(String rule, ResourceAction action) {
        this.rule = rule;
        this.action = action;
        this.pattern = Pattern.compile(rule);
    }

    public boolean matches(String path) {
        if (pattern == null)
            this.pattern = Pattern.compile(rule);
        return pattern.matcher(path).matches();
    }

    @Override
    public String toString() {
        return rule + " » " + action;
    }
}
