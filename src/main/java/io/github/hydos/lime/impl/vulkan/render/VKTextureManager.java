package io.github.hydos.lime.impl.vulkan.render;

import io.github.hydos.lime.core.math.CitrusMath;
import io.github.hydos.lime.impl.vulkan.Variables;
import io.github.hydos.lime.impl.vulkan.VulkanError;
import io.github.hydos.lime.impl.vulkan.elements.TexturedVulkanRenderObject;
import io.github.hydos.lime.impl.vulkan.elements.VulkanRenderObject;
import io.github.hydos.lime.impl.vulkan.lowlevel.VKBufferUtils;
import io.github.hydos.lime.impl.vulkan.lowlevel.VKMemoryUtils;
import io.github.hydos.lime.impl.vulkan.texture.CompiledTexture;
import io.github.hydos.lime.impl.vulkan.util.ImageUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VKTextureManager {

    public static List<CompiledTexture> compiledTextures = new ArrayList<>();

    public static TexturedVulkanRenderObject textureModel(String path, VulkanRenderObject object) {
        if (checkForExistingCompiledTexture(path)) {
            return new TexturedVulkanRenderObject(object, getExistingTexture(path));
        }
        try (MemoryStack stack = stackPush()) {
            CompiledTexture compiledTexture = new CompiledTexture();
            compiledTexture.path = path;
            String filename = Paths.get(new URI(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(path)).toExternalForm())).toString();

            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            IntBuffer pChannels = stack.mallocInt(1);

            ByteBuffer pixels = stbi_load(filename, pWidth, pHeight, pChannels, STBI_rgb_alpha);

            long imageSize = pWidth.get(0) * pHeight.get(0) * 4; // pChannels.get(0);

            compiledTexture.mipLevels = (int) Math.floor(CitrusMath.log2(Math.max(pWidth.get(0), pHeight.get(0)))) + 1;

            if (pixels == null) {
                throw new RuntimeException("Failed to load texture image " + filename);
            }

            LongBuffer pStagingBuffer = stack.mallocLong(1);
            LongBuffer pStagingBufferMemory = stack.mallocLong(1);
            VKBufferUtils.createBuffer(imageSize,
                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    pStagingBuffer,
                    pStagingBufferMemory);


            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(Variables.device, pStagingBufferMemory.get(0), 0, imageSize, 0, data);
            {
                VKMemoryUtils.memcpy(data.getByteBuffer(0, (int) imageSize), pixels, imageSize);
            }
            vkUnmapMemory(Variables.device, pStagingBufferMemory.get(0));

            stbi_image_free(pixels);

            LongBuffer pTextureImage = stack.mallocLong(1);
            LongBuffer pTextureImageMemory = stack.mallocLong(1);
            ImageUtils.createImage(pWidth.get(0), pHeight.get(0),
                    compiledTexture.mipLevels,
                    VK_SAMPLE_COUNT_1_BIT, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pTextureImage,
                    pTextureImageMemory);

            compiledTexture.textureImage = pTextureImage.get(0);
            compiledTexture.textureImageMemory = pTextureImageMemory.get(0);

            ImageUtils.transitionImageLayout(compiledTexture.textureImage,
                    VK_FORMAT_R8G8B8A8_SRGB,
                    VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    compiledTexture.mipLevels);

            ImageUtils.copyBufferToImage(pStagingBuffer.get(0), compiledTexture.textureImage, pWidth.get(0), pHeight.get(0));

            // Transitioned to VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL while generating mipmaps
            ImageUtils.generateMipmaps(compiledTexture.textureImage, VK_FORMAT_R8G8B8A8_SRGB, pWidth.get(0), pHeight.get(0), compiledTexture.mipLevels);

            vkDestroyBuffer(Variables.device, pStagingBuffer.get(0), null);
            vkFreeMemory(Variables.device, pStagingBufferMemory.get(0), null);

            compiledTextures.add(compiledTexture);
            VKTextureManager.createTextureImageView(compiledTexture);
            VKTextureManager.createTextureSampler(compiledTexture);
            return new TexturedVulkanRenderObject(object, compiledTexture);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static CompiledTexture getExistingTexture(String path) {
        for (CompiledTexture texture : compiledTextures) {
            if (texture.path.equals(path)) {
                return texture;
            }
        }
        return null;
    }

    private static boolean checkForExistingCompiledTexture(String path) {
        for (CompiledTexture texture : compiledTextures) {
            if (texture.path.equals(path)) {
                return true;
            }
        }
        return false;
    }

    public static void createTextureImageView(CompiledTexture texture) {
        texture.textureImageView = ImageUtils.createImageView(texture.textureImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_ASPECT_COLOR_BIT, texture.mipLevels);
    }

    public static void createTextureSampler(CompiledTexture texture) {
        try (MemoryStack stack = stackPush()) {
            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.callocStack(stack);
            samplerInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
            samplerInfo.magFilter(VK_FILTER_LINEAR);
            samplerInfo.minFilter(VK_FILTER_LINEAR);
            samplerInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.anisotropyEnable(true);
            samplerInfo.maxAnisotropy(16.0f);
            samplerInfo.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK);
            samplerInfo.unnormalizedCoordinates(false);
            samplerInfo.compareEnable(false);
            samplerInfo.compareOp(VK_COMPARE_OP_ALWAYS);
            samplerInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
            samplerInfo.minLod(0); // Optional
            samplerInfo.maxLod((float) texture.mipLevels);
            samplerInfo.mipLodBias(0); // Optional

            LongBuffer pTextureSampler = stack.mallocLong(1);

            VulkanError.failIfError(vkCreateSampler(Variables.device, samplerInfo, null, pTextureSampler));

            texture.textureSampler = pTextureSampler.get(0);
        }
    }
}
