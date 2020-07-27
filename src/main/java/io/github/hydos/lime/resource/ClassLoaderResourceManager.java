package io.github.hydos.lime.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;

public class ClassLoaderResourceManager implements ResourceManager {

    private final List<UrlResource> loadedResources = new ArrayList<>();
    private final ClassLoader classLoader;
    private final String prefix;

    public ClassLoaderResourceManager(ClassLoader classLoader, String prefix) {
        this.classLoader = classLoader;
        this.prefix = prefix;
    }

    @Override
    public Optional<Resource> getResource(Identifier identifier) {
        return Optional.ofNullable(classLoader.getResource(prefix + identifier.getNamespace() + "/" + identifier.getPath()))
                .map(url -> {
                    UrlResource resource = new UrlResource(identifier, url);
                    loadedResources.add(resource);
                    return resource;
                });
    }

    @Override
    public Collection<Identifier> findResources(Predicate<Identifier> predicate) {
        return Collections.emptyList();
    }

    @Override
    public void invalidate() {
        loadedResources.removeIf(resource -> resource.invalid = true);
    }

    private static class UrlResource implements Resource {

        private final Identifier identifier;
        private final URL url;
        private boolean invalid;

        private UrlResource(Identifier identifier, URL url) {
            this.identifier = identifier;
            this.url = url;
        }

        @Override
        public Identifier getIdentifier() {
            return identifier;
        }

        @Override
        public InputStream openStream() throws IOException {
            if (invalid) {
                throw new RuntimeException("Invalid URL resource was trying to be read");
            }

            return url.openStream();
        }
    }
}
