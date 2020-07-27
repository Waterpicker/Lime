package io.github.hydos.lime.core.core;

import io.github.hydos.lime.core.render.RenderObject;
import io.github.hydos.lime.impl.vulkan.elements.VulkanRenderObject;
import io.github.hydos.lime.impl.vulkan.model.VKModelLoader;
import io.github.hydos.lime.impl.vulkan.render.VKTextureManager;
import org.joml.Vector3f;

import java.io.File;

import static org.lwjgl.assimp.Assimp.aiProcess_DropNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_FlipUVs;

public class ModelManager {

    public static int lastModelIdRegistered = 0;

    public static RenderObject createObject(String texturePath, File model, Vector3f position, Vector3f rotation, Vector3f scale) {
        VulkanRenderObject object = new VulkanRenderObject();
        object.id = lastModelIdRegistered;
        lastModelIdRegistered++;
        object.rawModel = VKModelLoader.loadModel(model, aiProcess_FlipUVs | aiProcess_DropNormals);
        object.position = position;
        object.rotX = rotation.x();
        object.rotY = rotation.y();
        object.rotZ = rotation.z();
        object.scale = scale;
        return VKTextureManager.textureModel(texturePath, object);
    }
}
