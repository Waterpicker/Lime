package io.github.hydos.lime.resource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;

public interface ResourceManager {

    Optional<Resource> getResource(Identifier identifier);

    Collection<Identifier> findResources(Predicate<Identifier> predicate);

    default Collection<Resource> getResources(Identifier identifier) {
        Collection<Identifier> identifiers = findResources(identifier::equals);
        Collection<Resource> resources = new HashSet<>();

        for (Identifier i : identifiers) {
            getResource(i).ifPresent(resources::add);
        }

        return resources;
    }

    default void invalidate() {
    }
}
