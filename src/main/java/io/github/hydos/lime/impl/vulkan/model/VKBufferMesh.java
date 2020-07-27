package io.github.hydos.lime.impl.vulkan.model;

import io.github.hydos.lime.impl.vulkan.Variables;
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
        vkDestroyBuffer(Variables.device, indexBuffer, null);
        vkFreeMemory(Variables.device, indexBufferMemory, null);

        vkDestroyBuffer(Variables.device, vertexBuffer, null);
        vkFreeMemory(Variables.device, vertexBufferMemory, null);
    }
}
