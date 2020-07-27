package io.github.hydos.lime.resource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class CascadingResourceManager implements ResourceManager {

    private final List<ResourceManager> managers;

    public CascadingResourceManager(List<ResourceManager> managers) {
        this.managers = managers;
    }

    public CascadingResourceManager install(ResourceManager manager) {
        managers.add(manager);
        return this;
    }

    @Override
    public Optional<Resource> getResource(Identifier identifier) {
        for (ResourceManager manager : managers) {
            Optional<Resource> resource = manager.getResource(identifier);

            if (resource.isPresent()) {
                return resource;
            }
        }

        return Optional.empty();
    }

    @Override
    public Collection<Identifier> findResources(Predicate<Identifier> predicate) {
        Collection<Identifier> identifiers = new HashSet<>();

        for (ResourceManager manager : managers) {
            identifiers.addAll(manager.findResources(predicate));
        }

        return identifiers;
    }

    @Override
    public Collection<Resource> getResources(Identifier identifier) {
        Collection<Resource> resources = new HashSet<>();

        for (ResourceManager manager : managers) {
            resources.addAll(manager.getResources(identifier));
        }

        return resources;
    }

    @Override
    public void invalidate() {
        for (ResourceManager manager : managers) {
            manager.invalidate();
        }
    }
}
