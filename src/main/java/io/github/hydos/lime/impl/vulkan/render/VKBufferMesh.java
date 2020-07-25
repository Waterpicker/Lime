package io.github.hydos.lime.impl.vulkan.render;

import io.github.hydos.lime.impl.vulkan.VKVariables;
import io.github.hydos.lime.impl.vulkan.model.VKModelLoader;
import io.github.hydos.lime.impl.vulkan.model.VKVertex;

import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;

public class VKBufferMesh {

    public long vertexBuffer;
    public long indexBuffer;
    public VKModelLoader.VKMesh vkMesh;
    public int[] indices;
    public VKVertex[] vertices;
    public long vertexBufferMemory;
    public long indexBufferMemory;

    public void cleanup() {
        vkDestroyBuffer(VKVariables.device, indexBuffer, null);
        vkFreeMemory(VKVariables.device, indexBufferMemory, null);

        vkDestroyBuffer(VKVariables.device, vertexBuffer, null);
        vkFreeMemory(VKVariables.device, vertexBufferMemory, null);
    }

}
