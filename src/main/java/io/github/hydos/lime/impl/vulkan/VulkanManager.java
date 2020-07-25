package io.github.hydos.lime.impl.vulkan;

import io.github.hydos.lime.impl.vulkan.elements.VulkanRenderObject;
import io.github.hydos.lime.impl.vulkan.render.VKBufferMesh;
import io.github.hydos.lime.impl.vulkan.render.VKRenderManager;
import io.github.hydos.lime.impl.vulkan.render.renderers.EntityRenderer;

public class VulkanManager {
    private static VulkanManager INSTANCE;

    public EntityRenderer entityRenderer;

    public static void init() {
        INSTANCE = new VulkanManager();
        VKVariables.renderManager = new VKRenderManager();
    }

    public static VulkanManager getInstance() {
        return INSTANCE;
    }

    public void createRenderers() {
        entityRenderer = new EntityRenderer();
        VKVariables.renderManager.addRenderer(entityRenderer);
    }

    public void cleanup() {
        for (VulkanRenderObject entity : entityRenderer.entities) {
            VKBufferMesh bufferMesh = entity.getModel();
            bufferMesh.cleanup();
        }
    }

}
