package io.github.hydos.lime.core.info;

/**
 * The rendering backend API which is being used
 */
public enum RenderBackend {

    /**
     * OpenGL rendering backend. Currently unsupported
     */
    OpenGL,

    /**
     * Vulkan rendering backend
     */
    Vulkan
}
