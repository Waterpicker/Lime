package io.github.hydos.example;

import io.github.hydos.lime.core.core.ModelManager;
import io.github.hydos.lime.core.io.Window;
import io.github.hydos.lime.core.player.PlayerController;
import io.github.hydos.lime.core.render.RenderObject;
import io.github.hydos.lime.impl.vulkan.Variables;
import io.github.hydos.lime.impl.vulkan.VulkanManager;
import io.github.hydos.lime.impl.vulkan.VulkanReg;
import io.github.hydos.lime.impl.vulkan.device.DeviceManager;
import io.github.hydos.lime.impl.vulkan.io.VKWindow;
import io.github.hydos.lime.impl.vulkan.model.CommandBufferManager;
import io.github.hydos.lime.impl.vulkan.render.Frame;
import io.github.hydos.lime.impl.vulkan.swapchain.SwapchainManager;
import io.github.hydos.lime.impl.vulkan.ubo.DescriptorManager;
import io.github.hydos.lime.impl.vulkan.util.Utils;
import org.joml.Vector3f;

import java.io.File;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;

public class VulkanExample {

    RenderObject chalet;
    RenderObject dragon;

    public VulkanExample() {
        initWindow();
        initVulkan();
        runEngine();
        VulkanManager.getInstance().cleanup();
    }

    public static void main(String[] args) {
        new VulkanExample();
    }

    private void loadModels() {

        File chaletModelFile = new File(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("models/chalet.obj")).getFile());
        File dragonModelFile = new File(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("models/dragon.obj")).getFile());
        dragon = ModelManager.createObject("textures/skybox/back.png", dragonModelFile, new Vector3f(-2, -1, 0), new Vector3f(), new Vector3f(0.3f, 0.3f, 0.3f));

        chalet = ModelManager.createObject("textures/chalet.jpg", chaletModelFile, new Vector3f(-2, -0.4f, 0.5f), new Vector3f(-90, 0, 0), new Vector3f(1f, 1f, 1f));

        VulkanManager.getInstance().entityRenderer.processEntity(chalet);
        VulkanManager.getInstance().entityRenderer.processEntity(dragon);
    }

    private void initWindow() {
        Window.create(1200, 800, "Lime Render Engine", 60);
        glfwSetFramebufferSizeCallback(Window.getWindow(), this::framebufferResizeCallback);
    }

    private void framebufferResizeCallback(long window, int width, int height) {
        Variables.framebufferResize = true;
    }

    private void initVulkan() {
        VulkanReg.createInstance();
        VKWindow.createSurface();
        VulkanManager.init();
        VulkanManager.getInstance().createRenderers();
        DeviceManager.pickPhysicalDevice();
        DeviceManager.createLogicalDevice();
        CommandBufferManager.createCommandPool();
        DescriptorManager.createUBODescriptorSetLayout();
        loadModels();
        SwapchainManager.createSwapChainObjects();
        Utils.createSyncObjects();
    }

    private void runEngine() {
        Window.lockMouse();
        while (!Window.closed()) {
            loop();
            PlayerController.onInput();
        }
        Window.stop();
        vkDeviceWaitIdle(Variables.device);
    }

    private void loop() {
        if (Window.shouldRender()) {
            chalet.increaseRotation(0, 0, 5);
            Frame.drawFrame();
        }
        glfwPollEvents();
    }
}
