package io.github.hydos.lime;

import io.github.hydos.lime.resource.ClassLoaderResourceManager;
import io.github.hydos.lime.resource.ResourceManager;

public class IDoNotKnow {

    public static final ResourceManager GLOBAL_RESOURCE_MANAGER = new ClassLoaderResourceManager(ClassLoader.getSystemClassLoader(), "assets/");
}
