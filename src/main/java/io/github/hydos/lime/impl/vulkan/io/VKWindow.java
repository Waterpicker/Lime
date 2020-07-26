package io.github.hydos.lime.impl.vulkan.io;

import io.github.hydos.citrus.io.Window;
import io.github.hydos.lime.impl.vulkan.Variables;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class VKWindow {
    public static void createSurface() {

        try (MemoryStack stack = MemoryStack.stackPush()) {

            LongBuffer pSurface = stack.longs(VK_NULL_HANDLE);

            if (GLFWVulkan.glfwCreateWindowSurface(Variables.instance, Window.getWindow(), null, pSurface) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create window surface");
            }

            Variables.surface = pSurface.get(0);
        }
    }

}
