package io.github.hydos.lime.impl.vulkan.io;

import io.github.hydos.lime.core.io.Window;
import io.github.hydos.lime.impl.vulkan.Variables;
import io.github.hydos.lime.impl.vulkan.VulkanError;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;

public class VKWindow {
    public static void createSurface() {
        try (MemoryStack stack = MemoryStack.stackPush()) {

            LongBuffer pSurface = stack.longs(VK_NULL_HANDLE);

            VulkanError.failIfError(GLFWVulkan.glfwCreateWindowSurface(Variables.instance, Window.getWindow(), null, pSurface));
            Variables.surface = pSurface.get(0);
        }
    }
}
