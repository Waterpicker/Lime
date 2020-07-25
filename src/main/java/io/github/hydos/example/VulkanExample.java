package io.github.hydos.example;

import io.github.hydos.lime.core.io.Window;
import io.github.hydos.lime.impl.vulkan.VKRegister;
import io.github.hydos.lime.impl.vulkan.VKVariables;
import io.github.hydos.lime.impl.vulkan.elements.VulkanRenderObject;
import io.github.hydos.lime.impl.vulkan.io.VKWindow;
import io.github.hydos.lime.impl.vulkan.render.VKTextureManager;
import io.github.hydos.lime.impl.vulkan.model.VKModelLoader;
import io.github.hydos.lime.impl.vulkan.render.Frame;
import io.github.hydos.lime.impl.vulkan.swapchain.VKSwapchainManager;
import io.github.hydos.lime.impl.vulkan.utils.VKDeviceManager;
import io.github.hydos.lime.impl.vulkan.utils.VKUtils;
import io.github.hydos.lime.impl.vulkan.VulkanManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.io.File;
import java.nio.IntBuffer;
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

    public static void main(String[] args) {
        VulkanExample app = new VulkanExample();

        app.run();
    }

    public void run() {
        initWindow();
        initVulkan();
        mainLoop();
        VulkanManager.getInstance().cleanup();
        VKUtils.cleanup();
    }

    private void loadModels() {

        File modelFile = new File(ClassLoader.getSystemClassLoader().getResource("models/chalet.obj").getFile());

        VKModelLoader.VKMesh model = VKModelLoader.loadModel(modelFile, aiProcess_FlipUVs | aiProcess_DropNormals);
        VulkanRenderObject object = new VulkanRenderObject(model, new Vector3f(), 1, 1, 1, new Vector3f());
        VulkanManager.getInstance().entityRenderer.processEntity(object);
    }

    private void initWindow() {
        Window.create(1200, 800, "Vulkan Ginger2", 60);
        glfwSetFramebufferSizeCallback(Window.getWindow(), this::framebufferResizeCallback);
    }

    private void framebufferResizeCallback(long window, int width, int height) {
        VKVariables.framebufferResize = true;
    }

    private void initVulkan() {
        VKRegister.createInstance();
        VKWindow.createSurface();
        VulkanManager.init();
        VulkanManager.getInstance().createRenderers();
        VKDeviceManager.pickPhysicalDevice();
        VKDeviceManager.createLogicalDevice();
        VKUtils.createCommandPool();
        VKTextureManager.createTextureImage();
        VKTextureManager.createTextureImageView();
        VKTextureManager.createTextureSampler();
        VKUtils.createUBODescriptorSetLayout();
        loadModels();
        VKSwapchainManager.createSwapChainObjects(VulkanManager.getInstance().entityRenderer);
        VKUtils.createSyncObjects();
    }

    private void mainLoop() {

        while (!Window.closed()) {
            if (Window.shouldRender()) {
                Frame.drawFrame();
            }
            glfwPollEvents();
        }

        // Wait for the device to complete all operations before release resources
        vkDeviceWaitIdle(VKVariables.device);
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

    public static class UniformBufferObject {

        public static final int SIZEOF = 3 * 16 * Float.BYTES;

        public Matrix4f model;
        public Matrix4f view;
        public Matrix4f proj;

        public UniformBufferObject() {
            model = new Matrix4f();
            view = new Matrix4f();
            proj = new Matrix4f();
        }

        public UniformBufferObject(VulkanRenderObject renderObject) {
            model = new Matrix4f();
            view = new Matrix4f();
            proj = new Matrix4f();
            if(renderObject != null){
                model.translate(renderObject.position);
            }
        }
    }

}
