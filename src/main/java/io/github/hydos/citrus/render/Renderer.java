package io.github.hydos.citrus.render;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

public abstract class Renderer {
    public int priority = 2;//default is 2. 1 is the highest and 10 is the lowest

    public void VKRender(MemoryStack stack, VkCommandBuffer commandBuffer, int index) {
    }

    public void VKBindShaders(MemoryStack stack) {
    }

    public void VKUnbindShaders(MemoryStack stack) {
    }
}