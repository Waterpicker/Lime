package io.github.hydos.lime.impl.vulkan.elements;

import io.github.hydos.lime.core.render.RenderObject;
import io.github.hydos.lime.impl.vulkan.model.VKModelLoader;
import io.github.hydos.lime.impl.vulkan.render.VKBufferMesh;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class VulkanRenderObject extends RenderObject {

    public ArrayList<Long> descriptorSets;
    private VKBufferMesh model = null;
    private final VKModelLoader.VKMesh rawModel;
    public List<Long> uniformBuffers;
    public List<Long> uniformBuffersMemory;

    public VulkanRenderObject(VKModelLoader.VKMesh rawModel, Vector3f position, float rotX, float rotY, float rotZ, Vector3f scale) {
        this.uniformBuffers = new ArrayList<>();
        this.uniformBuffersMemory = new ArrayList<>();
        this.rawModel = rawModel;
        this.position = position;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.scale = scale;
    }

    public VKBufferMesh getModel() {
        return model;
    }

    public void setModel(VKBufferMesh model) {
        this.model = model;
    }

    public VKModelLoader.VKMesh getRawModel() {
        return rawModel;
    }

}
