package io.github.hydos.lime.impl.vulkan.swapchain;

import io.github.hydos.example.VulkanExample;
import io.github.hydos.lime.core.io.Window;
import io.github.hydos.lime.impl.vulkan.VKVariables;
import io.github.hydos.lime.impl.vulkan.VulkanManager;
import io.github.hydos.lime.impl.vulkan.model.CommandBufferManager;
import io.github.hydos.lime.impl.vulkan.render.VKRenderManager;
import io.github.hydos.lime.impl.vulkan.render.pipelines.VKPipelineManager;
import io.github.hydos.lime.impl.vulkan.render.renderers.EntityRenderer;
import io.github.hydos.lime.impl.vulkan.utils.VKUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class VKSwapchainManager {

    public static void cleanupSwapChain() {
        vkDestroyImageView(VKVariables.device, VKVariables.colorImageView, null);
        vkDestroyImage(VKVariables.device, VKVariables.colorImage, null);
        vkFreeMemory(VKVariables.device, VKVariables.colorImageMemory, null);

        vkDestroyImageView(VKVariables.device, VKVariables.depthImageView, null);
        vkDestroyImage(VKVariables.device, VKVariables.depthImage, null);
        vkFreeMemory(VKVariables.device, VKVariables.depthImageMemory, null);
        VKVariables.uniformBuffers.forEach(ubo -> vkDestroyBuffer(VKVariables.device, ubo, null));
        VKVariables.uniformBuffersMemory.forEach(uboMemory -> vkFreeMemory(VKVariables.device, uboMemory, null));

        vkDestroyDescriptorPool(VKVariables.device, VKVariables.descriptorPool, null);

        VKVariables.swapChainFramebuffers.forEach(framebuffer -> vkDestroyFramebuffer(VKVariables.device, framebuffer, null));

        vkFreeCommandBuffers(VKVariables.device, VKVariables.commandPool, VKUtils.asPointerBuffer(VKVariables.commandBuffers));

        vkDestroyPipeline(VKVariables.device, VKVariables.graphicsPipeline, null);

        vkDestroyPipelineLayout(VKVariables.device, VKVariables.pipelineLayout, null);

        vkDestroyRenderPass(VKVariables.device, VKVariables.renderPass, null);

        VKVariables.swapChainImageViews.forEach(imageView -> vkDestroyImageView(VKVariables.device, imageView, null));

        vkDestroySwapchainKHR(VKVariables.device, VKVariables.swapChain, null);
    }

    public static void recreateSwapChain() {

        try (MemoryStack stack = stackPush()) {

            IntBuffer width = stack.ints(0);
            IntBuffer height = stack.ints(0);

            while (width.get(0) == 0 && height.get(0) == 0) {
                GLFW.glfwGetFramebufferSize(Window.getWindow(), width, height);
                glfwWaitEvents();
            }
        }

        vkDeviceWaitIdle(VKVariables.device);

        VKSwapchainManager.cleanupSwapChain();

        createSwapChainObjects(VulkanManager.getInstance().entityRenderer);
    }

    public static void createSwapChain() {

        try (MemoryStack stack = stackPush()) {

            VulkanExample.SwapChainSupportDetails swapChainSupport = VKUtils.querySwapChainSupport(VKVariables.physicalDevice, stack);

            VkSurfaceFormatKHR surfaceFormat = VKUtils.chooseSwapSurfaceFormat(swapChainSupport.formats);
            int presentMode = VKUtils.chooseSwapPresentMode(swapChainSupport.presentModes);
            VkExtent2D extent = VKUtils.chooseSwapExtent(swapChainSupport.capabilities);

            IntBuffer imageCount = stack.ints(swapChainSupport.capabilities.minImageCount() + 1);

            if (swapChainSupport.capabilities.maxImageCount() > 0 && imageCount.get(0) > swapChainSupport.capabilities.maxImageCount()) {
                imageCount.put(0, swapChainSupport.capabilities.maxImageCount());
            }

            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.callocStack(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
            createInfo.surface(VKVariables.surface);

            // Image settings
            createInfo.minImageCount(imageCount.get(0));
            createInfo.imageFormat(surfaceFormat.format());
            createInfo.imageColorSpace(surfaceFormat.colorSpace());
            createInfo.imageExtent(extent);
            createInfo.imageArrayLayers(1);
            createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

            VulkanExample.QueueFamilyIndices indices = VKUtils.findQueueFamilies(VKVariables.physicalDevice);

            if (!indices.graphicsFamily.equals(indices.presentFamily)) {
                createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                createInfo.pQueueFamilyIndices(stack.ints(indices.graphicsFamily, indices.presentFamily));
            } else {
                createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
            }

            createInfo.preTransform(swapChainSupport.capabilities.currentTransform());
            createInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
            createInfo.presentMode(presentMode);
            createInfo.clipped(true);

            createInfo.oldSwapchain(VK_NULL_HANDLE);

            LongBuffer pSwapChain = stack.longs(VK_NULL_HANDLE);

            if (vkCreateSwapchainKHR(VKVariables.device, createInfo, null, pSwapChain) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create swap chain");
            }

            VKVariables.swapChain = pSwapChain.get(0);

            vkGetSwapchainImagesKHR(VKVariables.device, VKVariables.swapChain, imageCount, null);

            LongBuffer pSwapchainImages = stack.mallocLong(imageCount.get(0));

            vkGetSwapchainImagesKHR(VKVariables.device, VKVariables.swapChain, imageCount, pSwapchainImages);

            VKVariables.swapChainImages = new ArrayList<>(imageCount.get(0));

            for (int i = 0; i < pSwapchainImages.capacity(); i++) {
                VKVariables.swapChainImages.add(pSwapchainImages.get(i));
            }

            VKVariables.swapChainImageFormat = surfaceFormat.format();
            VKVariables.swapChainExtent = VkExtent2D.create().set(extent);
        }
    }


    /**
     * i tried organising it but if i change the order everything breaks
     */
    public static void createSwapChainObjects(EntityRenderer entityRenderer) {
        createSwapChain();
        VKUtils.createImageViews();
        VKRenderManager.createRenderPass();
        VKPipelineManager.createGraphicsPipeline();
        VKUtils.createColorResources();
        VKUtils.createDepthResources();
        VKUtils.createFramebuffers();
        VKUtils.createUniformBuffers(entityRenderer.entities);
        VKUtils.createDescriptorPool();
        VKUtils.createDescriptorSets();
        CommandBufferManager.createCommandBuffers();
    }

}
