package io.github.hydos.example;

import io.github.hydos.lime.core.PlayerController;
import io.github.hydos.lime.core.io.Window;
import io.github.hydos.lime.impl.vulkan.Variables;
import io.github.hydos.lime.impl.vulkan.VulkanManager;
import io.github.hydos.lime.impl.vulkan.VulkanReg;
import io.github.hydos.lime.impl.vulkan.device.DeviceManager;
import io.github.hydos.lime.impl.vulkan.elements.TexturedVulkanRenderObject;
import io.github.hydos.lime.impl.vulkan.elements.VulkanRenderObject;
import io.github.hydos.lime.impl.vulkan.io.VKWindow;
import io.github.hydos.lime.impl.vulkan.model.VKModelLoader;
import io.github.hydos.lime.impl.vulkan.render.Frame;
import io.github.hydos.lime.impl.vulkan.render.VKTextureManager;
import io.github.hydos.lime.impl.vulkan.swapchain.SwapchainManager;
import io.github.hydos.lime.impl.vulkan.ubo.DescriptorManager;
import io.github.hydos.lime.impl.vulkan.util.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.io.File;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.lwjgl.assimp.Assimp.aiProcess_DropNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_FlipUVs;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;

public class VulkanExample {

    public static final Set<String> DEVICE_EXTENSIONS = Stream.of(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME).collect(Collectors.toSet());

    TexturedVulkanRenderObject chalet;
    TexturedVulkanRenderObject dragon;

    public static void main(String[] args) {
        VulkanExample app = new VulkanExample();

        app.run();
    }

    public void run() {
        initWindow();
        initVulkan();
        mainLoop();
        VulkanManager.getInstance().cleanup();
    }

    private void loadModels() {

        File chaletModelFile = new File(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("models/chalet.obj")).getFile());
        File dragonModelFile = new File(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("models/dragon.obj")).getFile());

        VKModelLoader.VKMesh chaletModel = VKModelLoader.loadModel(chaletModelFile, aiProcess_FlipUVs | aiProcess_DropNormals);
        VKModelLoader.VKMesh dragonModel = VKModelLoader.loadModel(dragonModelFile, aiProcess_FlipUVs | aiProcess_DropNormals);

        dragon = VKTextureManager.textureModel("textures/skybox/back.png", new VulkanRenderObject(dragonModel, new Vector3f(-2, -1, 0), 0, 0, 0, new Vector3f(0.3f, 0.3f, 0.3f)));

        chalet = VKTextureManager.textureModel("textures/chalet.jpg", new VulkanRenderObject(chaletModel, new Vector3f(-2, -0.4f, 0.5f), -90, 0, 0, new Vector3f(1f, 1f, 1f)));

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
        Utils.createCommandPool();
        DescriptorManager.createUBODescriptorSetLayout();
        loadModels();
        SwapchainManager.createSwapChainObjects();
        Utils.createSyncObjects();
    }

    private void mainLoop() {
        Window.lockMouse();
        while (!Window.closed()) {
            doStuff();
            PlayerController.onInput();
        }

        // Wait for the device to complete all operations before release resources
        vkDeviceWaitIdle(Variables.device);
    }

    private void doStuff() {
        if (Window.shouldRender()) {
            chalet.increaseRotation(0, 0, 5);
            Frame.drawFrame();
        }
        glfwPollEvents();
    }

    public static class QueueFamilyIndices {
        public Integer graphicsFamily;
        public Integer presentFamily;

        public boolean isComplete() {
            return graphicsFamily != null && presentFamily != null;
        }

        public int[] unique() {
            return IntStream.of(graphicsFamily, presentFamily).distinct().toArray();
        }
    }

    public static class SwapChainSupportDetails {
        public VkSurfaceCapabilitiesKHR capabilities;
        public VkSurfaceFormatKHR.Buffer formats;
        public IntBuffer presentModes;
    }
}
