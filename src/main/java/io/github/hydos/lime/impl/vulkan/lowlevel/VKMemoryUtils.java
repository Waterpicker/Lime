package io.github.hydos.lime.impl.vulkan.lowlevel;

import io.github.hydos.lime.impl.vulkan.Variables;
import io.github.hydos.lime.impl.vulkan.model.VKVertex;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;

public class VKMemoryUtils {

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

    public static void memcpy(ByteBuffer dst, ByteBuffer src, long size) {
        src.limit((int) size);
        dst.put(src);
        src.limit(src.capacity()).rewind();
    }

    public static void memcpy(ByteBuffer buffer, VKVertex[] vertices) {
        for (VKVertex vertex : vertices) {
            buffer.putFloat(vertex.pos.x());
            buffer.putFloat(vertex.pos.y());
            buffer.putFloat(vertex.pos.z());

            buffer.putFloat(vertex.color.x());
            buffer.putFloat(vertex.color.y());
            buffer.putFloat(vertex.color.z());

            buffer.putFloat(vertex.texCoords.x());
            buffer.putFloat(vertex.texCoords.y());
        }
    }

    public static void memcpy(ByteBuffer buffer, int[] indices) {
        for (int index : indices) {
            buffer.putInt(index);
        }

        buffer.rewind();
    }

}
