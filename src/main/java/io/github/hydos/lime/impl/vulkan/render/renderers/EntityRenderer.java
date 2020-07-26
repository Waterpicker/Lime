package io.github.hydos.lime.impl.vulkan.render.renderers;

import io.github.hydos.lime.core.render.Renderer;
import io.github.hydos.lime.impl.vulkan.VKVariables;
import io.github.hydos.lime.impl.vulkan.elements.VulkanRenderObject;
import io.github.hydos.lime.impl.vulkan.model.VKModelLoader;
import io.github.hydos.lime.impl.vulkan.model.VKVertex;
import io.github.hydos.lime.impl.vulkan.render.VKBufferMesh;
import io.github.hydos.lime.impl.vulkan.utils.VKUtils;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

public class EntityRenderer extends Renderer {
    public List<VulkanRenderObject> entities;//TODO: batch rendering

    public EntityRenderer() {
        entities = new ArrayList<>();
    }

    public void processEntity(VulkanRenderObject entity) {
        VKModelLoader.VKMesh mesh = entity.getRawModel();
        VKBufferMesh processedMesh = new VKBufferMesh();
        processedMesh.vkMesh = mesh;
        int vertexCount = mesh.positions.size();

        processedMesh.vertices = new VKVertex[vertexCount];

        Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);

        for (int i = 0; i < vertexCount; i++) {
            processedMesh.vertices[i] = new VKVertex(
                    mesh.positions.get(i),
                    color,
                    mesh.texCoords.get(i));
        }

        processedMesh.indices = new int[mesh.indices.size()];

        for (int i = 0; i < processedMesh.indices.length; i++) {
            processedMesh.indices[i] = mesh.indices.get(i);
        }

        VKUtils.createVertexBuffer(processedMesh);
        VKUtils.createIndexBuffer(processedMesh);
        entity.setModel(processedMesh);
        entities.add(entity);
    }

    @Override
    public void VKRender(MemoryStack stack, VkCommandBuffer commandBuffer, int index) {

        for (VulkanRenderObject entity : entities) {
            VKBufferMesh mesh = entity.getModel();

            LongBuffer vertexBuffers = stack.longs(mesh.vertexBuffer);
            LongBuffer offsets = stack.longs(0);
            vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);

            vkCmdBindIndexBuffer(commandBuffer, mesh.indexBuffer, 0, VK_INDEX_TYPE_UINT32);

            int[] pushConstants = new int[2];
            pushConstants[0] = entity.id;

            VK10.vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                    VKVariables.pipelineLayout,
                    0, stack.longs(
                            VKVariables.descriptorSets.get(index)
                    ),
                    null);
            vkCmdPushConstants(commandBuffer, VKVariables.pipelineLayout, VK_SHADER_STAGE_VERTEX_BIT, 0, pushConstants);
            vkCmdDrawIndexed(commandBuffer, mesh.vkMesh.indices.size(), 1, 0, 0, 0);
        }

    }


}
