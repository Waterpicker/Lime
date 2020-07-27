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
import io.github.hydos.lime.resource.ClassLoaderResourceManager;
import io.github.hydos.lime.resource.Identifier;
import io.github.hydos.lime.resource.Resource;
import io.github.hydos.lime.resource.ResourceManager;
import org.joml.Vector3f;

import java.io.IOException;

import static io.github.hydos.lime.IDoNotKnow.GLOBAL_RESOURCE_MANAGER;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;

public class VulkanExample {

    RenderObject chalet;
    RenderObject dragon;

    public VulkanExample() throws IOException {
        initWindow();
        initVulkan();
        runEngine();
        VulkanManager.getInstance().cleanup();
    }

    public static void main(String[] args) throws IOException {
        new VulkanExample();
    }

    private void loadModels() throws IOException {
        Resource charletModel = GLOBAL_RESOURCE_MANAGER.getResource(new Identifier("example", "models/chalet.obj")).get();
        Resource dragonModel = GLOBAL_RESOURCE_MANAGER.getResource(new Identifier("example", "models/dragon.obj")).get();

        Resource charletTexture = GLOBAL_RESOURCE_MANAGER.getResource(new Identifier("example", "textures/chalet.jpg")).get();
        Resource dragonTexture = GLOBAL_RESOURCE_MANAGER.getResource(new Identifier("example", "textures/skybox/back.png")).get();

        chalet = ModelManager.createObject(charletTexture, charletModel, new Vector3f(-2, -0.4f, 0.5f), new Vector3f(-90, 0, 0), new Vector3f(1f, 1f, 1f));
        dragon = ModelManager.createObject(dragonTexture, dragonModel, new Vector3f(-2, -1, 0), new Vector3f(), new Vector3f(0.3f, 0.3f, 0.3f));

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

    private void initVulkan() throws IOException {
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
