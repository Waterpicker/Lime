package io.github.hydos.lime.impl.vulkan.util;

import io.github.hydos.example.VulkanExample;
import io.github.hydos.lime.core.math.LimeLegacyMaths;
import io.github.hydos.lime.impl.vulkan.Variables;
import io.github.hydos.lime.impl.vulkan.VulkanManager;
import io.github.hydos.lime.impl.vulkan.device.DeviceManager;
import io.github.hydos.lime.impl.vulkan.elements.VulkanRenderObject;
import io.github.hydos.lime.impl.vulkan.lowlevel.AlignmentUtils;
import io.github.hydos.lime.impl.vulkan.lowlevel.VKBufferUtils;
import io.github.hydos.lime.impl.vulkan.model.CommandBufferManager;
import io.github.hydos.lime.impl.vulkan.render.Frame;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Utils {
    static final int mat4Size = 16 * Float.BYTES;
    static final int modelArraySize = mat4Size * 10; //FIXME: also hardcoded

    public static int findDepthFormat() {
        return Utils.findSupportedFormat(
                stackGet().ints(VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT),
                VK_IMAGE_TILING_OPTIMAL,
                VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT);
    }

    public static int getMaxUsableSampleCount() {

        try (MemoryStack stack = stackPush()) {

            VkPhysicalDeviceProperties physicalDeviceProperties = VkPhysicalDeviceProperties.mallocStack(stack);
            vkGetPhysicalDeviceProperties(Variables.physicalDevice, physicalDeviceProperties);

            System.out.println(physicalDeviceProperties.limits().minUniformBufferOffsetAlignment());

            int sampleCountFlags = physicalDeviceProperties.limits().framebufferColorSampleCounts()
                    & physicalDeviceProperties.limits().framebufferDepthSampleCounts();

            if ((sampleCountFlags & VK_SAMPLE_COUNT_64_BIT) != 0) {
                return VK_SAMPLE_COUNT_64_BIT;
            }
            if ((sampleCountFlags & VK_SAMPLE_COUNT_32_BIT) != 0) {
                return VK_SAMPLE_COUNT_32_BIT;
            }
            if ((sampleCountFlags & VK_SAMPLE_COUNT_16_BIT) != 0) {
                return VK_SAMPLE_COUNT_16_BIT;
            }
            if ((sampleCountFlags & VK_SAMPLE_COUNT_8_BIT) != 0) {
                return VK_SAMPLE_COUNT_8_BIT;
            }
            if ((sampleCountFlags & VK_SAMPLE_COUNT_4_BIT) != 0) {
                return VK_SAMPLE_COUNT_4_BIT;
            }
            if ((sampleCountFlags & VK_SAMPLE_COUNT_2_BIT) != 0) {
                return VK_SAMPLE_COUNT_2_BIT;
            }

            return VK_SAMPLE_COUNT_1_BIT;
        }
    }

    public static void createFramebuffers() {

        Variables.swapChainFramebuffers = new ArrayList<>(Variables.swapChainImageViews.size());

        try (MemoryStack stack = stackPush()) {

            LongBuffer attachments = stack.longs(Variables.colorImageView, Variables.depthImageView, VK_NULL_HANDLE);
            LongBuffer pFramebuffer = stack.mallocLong(1);

            // Lets allocate the create info struct once and just update the pAttachments field each iteration
            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack);
            framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferInfo.renderPass(Variables.renderPass);
            framebufferInfo.width(Variables.swapChainExtent.width());
            framebufferInfo.height(Variables.swapChainExtent.height());
            framebufferInfo.layers(1);

            for (long imageView : Variables.swapChainImageViews) {

                attachments.put(2, imageView);

                framebufferInfo.pAttachments(attachments);

                if (vkCreateFramebuffer(Variables.device, framebufferInfo, null, pFramebuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create framebuffer");
                }

                Variables.swapChainFramebuffers.add(pFramebuffer.get(0));
            }
        }
    }

    public static void createColorResources() {

        try (MemoryStack stack = stackPush()) {

            LongBuffer pColorImage = stack.mallocLong(1);
            LongBuffer pColorImageMemory = stack.mallocLong(1);

            ImageUtils.createImage(Variables.swapChainExtent.width(), Variables.swapChainExtent.height(),
                    1,
                    Variables.msaaSamples,
                    Variables.swapChainImageFormat,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT | VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pColorImage,
                    pColorImageMemory);

            Variables.colorImage = pColorImage.get(0);
            Variables.colorImageMemory = pColorImageMemory.get(0);

            Variables.colorImageView = ImageUtils.createImageView(Variables.colorImage, Variables.swapChainImageFormat, VK_IMAGE_ASPECT_COLOR_BIT, 1);

            Utils.transitionImageLayout(Variables.colorImage, Variables.swapChainImageFormat, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, 1);
        }
    }

    public static void createDepthResources() {

        try (MemoryStack stack = stackPush()) {

            int depthFormat = findDepthFormat();

            LongBuffer pDepthImage = stack.mallocLong(1);
            LongBuffer pDepthImageMemory = stack.mallocLong(1);

            ImageUtils.createImage(
                    Variables.swapChainExtent.width(), Variables.swapChainExtent.height(),
                    1,
                    Variables.msaaSamples,
                    depthFormat,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pDepthImage,
                    pDepthImageMemory);

            Variables.depthImage = pDepthImage.get(0);
            Variables.depthImageMemory = pDepthImageMemory.get(0);

            Variables.depthImageView = ImageUtils.createImageView(Variables.depthImage, depthFormat, VK_IMAGE_ASPECT_DEPTH_BIT, 1);

            // Explicitly transitioning the depth image
            Utils.transitionImageLayout(Variables.depthImage, depthFormat,
                    VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                    1);

        }
    }

    public static int findSupportedFormat(IntBuffer formatCandidates, int tiling, int features) {

        try (MemoryStack stack = stackPush()) {

            VkFormatProperties props = VkFormatProperties.callocStack(stack);

            for (int i = 0; i < formatCandidates.capacity(); ++i) {

                int format = formatCandidates.get(i);

                vkGetPhysicalDeviceFormatProperties(Variables.physicalDevice, format, props);

                if (tiling == VK_IMAGE_TILING_LINEAR && (props.linearTilingFeatures() & features) == features) {
                    return format;
                } else if (tiling == VK_IMAGE_TILING_OPTIMAL && (props.optimalTilingFeatures() & features) == features) {
                    return format;
                }

            }
        }

        throw new RuntimeException("Failed to find supported format");
    }

    public static void createCommandPool() {

        try (MemoryStack stack = stackPush()) {

            VulkanExample.QueueFamilyIndices queueFamilyIndices = DeviceManager.findQueueFamilies(Variables.physicalDevice);

            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack);
            poolInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
            poolInfo.queueFamilyIndex(queueFamilyIndices.graphicsFamily);

            LongBuffer pCommandPool = stack.mallocLong(1);

            if (vkCreateCommandPool(Variables.device, poolInfo, null, pCommandPool) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create command pool");
            }

            Variables.commandPool = pCommandPool.get(0);
        }
    }

    public static void transitionImageLayout(long image, int format, int oldLayout, int newLayout, int mipLevels) {

        try (MemoryStack stack = stackPush()) {

            VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.callocStack(1, stack);
            barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
            barrier.oldLayout(oldLayout);
            barrier.newLayout(newLayout);
            barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
            barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
            barrier.image(image);

            barrier.subresourceRange().baseMipLevel(0);
            barrier.subresourceRange().levelCount(mipLevels);
            barrier.subresourceRange().baseArrayLayer(0);
            barrier.subresourceRange().layerCount(1);

            if (newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {

                barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);

                if (Variables.hasStencilComponent(format)) {
                    barrier.subresourceRange().aspectMask(
                            barrier.subresourceRange().aspectMask() | VK_IMAGE_ASPECT_STENCIL_BIT);
                }

            } else {
                barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            }

            int sourceStage;
            int destinationStage;

            if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {

                barrier.srcAccessMask(0);
                barrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);

                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;

            } else if (oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {

                barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

                sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
                destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;

            } else if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {

                barrier.srcAccessMask(0);
                barrier.dstAccessMask(VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);

                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;

            } else if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL) {

                barrier.srcAccessMask(0);
                barrier.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;

            } else {
                throw new IllegalArgumentException("Unsupported layout transition");
            }

            VkCommandBuffer commandBuffer = CommandBufferManager.beginSingleTimeCommands();

            vkCmdPipelineBarrier(commandBuffer,
                    sourceStage, destinationStage,
                    0,
                    null,
                    null,
                    barrier);

            CommandBufferManager.endSingleTimeCommands(commandBuffer);
        }
    }

    public static void copyBufferToImage(long buffer, long image, int width, int height) {

        try (MemoryStack stack = stackPush()) {

            VkCommandBuffer commandBuffer = CommandBufferManager.beginSingleTimeCommands();

            VkBufferImageCopy.Buffer region = VkBufferImageCopy.callocStack(1, stack);
            region.bufferOffset(0);
            region.bufferRowLength(0);   // Tightly packed
            region.bufferImageHeight(0);  // Tightly packed
            region.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            region.imageSubresource().mipLevel(0);
            region.imageSubresource().baseArrayLayer(0);
            region.imageSubresource().layerCount(1);
            region.imageOffset().set(0, 0, 0);
            region.imageExtent(VkExtent3D.callocStack(stack).set(width, height, 1));

            vkCmdCopyBufferToImage(commandBuffer, buffer, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);

            CommandBufferManager.endSingleTimeCommands(commandBuffer);
        }
    }

    public static void createUniformBuffers() {
        try (MemoryStack stack = stackPush()) {
            Variables.uniformBuffers = new ArrayList<>(Variables.swapChainImages.size());
            Variables.uniformBuffersMemory = new ArrayList<>(Variables.swapChainImages.size());

            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);

            for (int i = 0; i < Variables.swapChainImages.size(); i++) {
                VKBufferUtils.createBuffer(VulkanExample.Ubo.SIZEOF, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, pBuffer, pBufferMemory);

                Variables.uniformBuffers.add(pBuffer.get(0));
                Variables.uniformBuffersMemory.add(pBufferMemory.get(0));
            }
        }
    }

    private static void putUBOInMemory(ByteBuffer buffer, VulkanExample.Ubo ubo) {
        putArrayInUboMemory(buffer, ubo.model);
        ubo.view.get(AlignmentUtils.alignas(modelArraySize, AlignmentUtils.alignof(ubo.view)), buffer);
        ubo.proj.get(AlignmentUtils.alignas(modelArraySize + mat4Size, AlignmentUtils.alignof(ubo.view)), buffer);
    }

    private static void putArrayInUboMemory(ByteBuffer buffer, Matrix4f[] models) {
        int i = 0;
        for (Matrix4f mat : models) {
            if (mat == null)
                mat = new Matrix4f();
            mat.get(mat4Size * i, buffer);
            i++;
        }
    }

    public static int findMemoryType(int typeFilter, int properties) {

        VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.mallocStack();
        vkGetPhysicalDeviceMemoryProperties(Variables.physicalDevice, memProperties);

        for (int i = 0; i < memProperties.memoryTypeCount(); i++) {
            if ((typeFilter & (1 << i)) != 0 && (memProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                return i;
            }
        }

        throw new RuntimeException("Failed to find suitable memory type");
    }


    public static void createSyncObjects() {

        Variables.inFlightFrames = new ArrayList<>(Frame.MAX_FRAMES_IN_FLIGHT);
        Variables.imagesInFlight = new HashMap<>(Variables.swapChainImages.size());

        try (MemoryStack stack = stackPush()) {

            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.callocStack(stack);
            semaphoreInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.callocStack(stack);
            fenceInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

            LongBuffer pImageAvailableSemaphore = stack.mallocLong(1);
            LongBuffer pRenderFinishedSemaphore = stack.mallocLong(1);
            LongBuffer pFence = stack.mallocLong(1);

            for (int i = 0; i < Frame.MAX_FRAMES_IN_FLIGHT; i++) {

                if (vkCreateSemaphore(Variables.device, semaphoreInfo, null, pImageAvailableSemaphore) != VK_SUCCESS
                        || vkCreateSemaphore(Variables.device, semaphoreInfo, null, pRenderFinishedSemaphore) != VK_SUCCESS
                        || vkCreateFence(Variables.device, fenceInfo, null, pFence) != VK_SUCCESS) {

                    throw new RuntimeException("Failed to create synchronization objects for the frame " + i);
                }

                Variables.inFlightFrames.add(new Frame(pImageAvailableSemaphore.get(0), pRenderFinishedSemaphore.get(0), pFence.get(0)));
            }

        }
    }

    public static void updateUniformBuffer(int currentImage) {
        try (MemoryStack stack = stackPush()) {

            VulkanExample.Ubo ubo = new VulkanExample.Ubo();

            for (VulkanRenderObject object : VulkanManager.getInstance().entityRenderer.entities) {
                ubo.model[object.id] = LimeLegacyMaths.createTransformationMatrix(object.getPosition(), object.getRotX(), object.getRotY(), object.getRotZ(), object.getScale());
            }

            //Constant stuff generally goes here. later on view matrix will use the Math class one.
            ubo.view = LimeLegacyMaths.calcView(new Vector3f(2, 0, 0), new Vector3f(0, 0, 0));
            ubo.proj.perspective((float) java.lang.Math.toRadians(45), (float) Variables.swapChainExtent.width() / (float) Variables.swapChainExtent.height(), 0.1f, 10.0f);
            ubo.proj.m11(ubo.proj.m11() * -1);

            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(Variables.device, Variables.uniformBuffersMemory.get(currentImage), 0, VulkanExample.Ubo.SIZEOF * VulkanManager.getInstance().entityRenderer.entities.size(), 0, data);
            {
                putUBOInMemory(data.getByteBuffer(0, VulkanExample.Ubo.SIZEOF * VulkanManager.getInstance().entityRenderer.entities.size()), ubo);
            }
            vkUnmapMemory(Variables.device, Variables.uniformBuffersMemory.get(currentImage));
        }
    }

    public static PointerBuffer asPointerBuffer(Collection<String> collection) {

        MemoryStack stack = stackGet();

        PointerBuffer buffer = stack.mallocPointer(collection.size());

        collection.stream()
                .map(stack::UTF8)
                .forEach(buffer::put);

        return buffer.rewind();
    }

    public static PointerBuffer asPointerBuffer(List<? extends Pointer> list) {
        MemoryStack stack = stackGet();

        PointerBuffer buffer = stack.mallocPointer(list.size());

        list.forEach(buffer::put);

        return buffer.rewind();
    }
}
