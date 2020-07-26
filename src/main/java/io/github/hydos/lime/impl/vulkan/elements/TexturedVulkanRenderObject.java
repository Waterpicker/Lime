package io.github.hydos.lime.impl.vulkan.elements;

import io.github.hydos.lime.core.render.RenderObject;
import io.github.hydos.lime.impl.vulkan.model.VKModelLoader;
import io.github.hydos.lime.impl.vulkan.render.VKBufferMesh;
import io.github.hydos.lime.impl.vulkan.texture.CompiledTexture;
import org.joml.Vector3f;

public class TexturedVulkanRenderObject extends VulkanRenderObject {

    public CompiledTexture texture;

    public TexturedVulkanRenderObject(VulkanRenderObject object) {
        super(object.getRawModel(), object.position, object.rotX, object.rotY, object.rotZ, object.scale);
    }

    public TexturedVulkanRenderObject(VulkanRenderObject object, CompiledTexture compiledTexture) {
        this(object);
        this.texture = compiledTexture;
    }

}
