package io.github.hydos.lime.impl.vulkan;

import io.github.hydos.lime.impl.vulkan.render.Frame;
import io.github.hydos.lime.impl.vulkan.render.VKRenderManager;
import org.lwjgl.vulkan.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;

/**
 * the place where Vulkan variables are stored
 *
 * @author hydos
 */
public class VKVariables {

    public static VkInstance instance;
    public static long surface;

    public static VkPhysicalDevice physicalDevice;
    public static VkDevice device;

    public static int msaaSamples = VK_SAMPLE_COUNT_1_BIT;

    public static VkQueue graphicsQueue;
    public static VkQueue presentQueue;

    public static long swapChain;
    public static List<Long> swapChainImages;
    public static int swapChainImageFormat;
    public static VkExtent2D swapChainExtent;
    public static List<Long> swapChainImageViews;
    public static List<Long> swapChainFramebuffers;

    public static long renderPass;

    public static long descriptorPool;
    public static long descriptorSetLayout;

    public static long pipelineLayout;

    public static long graphicsPipeline;

    public static long commandPool;

    public static long colorImage;
    public static long colorImageMemory;
    public static long colorImageView;

    public static long depthImage;
    public static long depthImageMemory;
    public static long depthImageView;

    public static int mipLevels;
    public static long textureImage;
    public static long textureImageMemory;
    public static long textureImageView;
    public static long textureSampler;

    public static List<VkCommandBuffer> commandBuffers;

    public static List<Frame> inFlightFrames;
    public static Map<Integer, Frame> imagesInFlight;
    public static int currentFrame;

    public static boolean framebufferResize;
    public static VKRenderManager renderManager;
    public static int currentImageIndex;

    public static ArrayList<Long> uniformBuffers;
    public static ArrayList<Long> uniformBuffersMemory;
    public static ArrayList<Long> descriptorSets;
}
