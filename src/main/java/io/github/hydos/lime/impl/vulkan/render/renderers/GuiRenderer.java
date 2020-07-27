package io.github.hydos.lime.impl.vulkan.render.renderers;

import io.github.hydos.lime.core.render.Renderer;
import io.github.hydos.lime.core.ui.GuiElement;
import io.github.hydos.lime.core.ui.GuiManager;
import io.github.hydos.lime.impl.vulkan.Variables;
import io.github.hydos.lime.impl.vulkan.elements.TexturedVulkanRenderObject;
import io.github.hydos.lime.impl.vulkan.model.VKBufferMesh;
import io.github.hydos.lime.impl.vulkan.render.pipelines.VKPipelineManager;
import io.github.hydos.lime.impl.vulkan.ubo.DescriptorManager;
import io.github.hydos.lime.impl.vulkan.ui.VulkanGuiElement;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

public class GuiRenderer extends Renderer {
    public List<TexturedVulkanRenderObject> elements;//TODO: batch rendering for ui's with the same texture

    public long graphicsPipeline;

    public GuiRenderer() {
        elements = new ArrayList<>();
    }

    @Override
    public void VKRender(MemoryStack stack, VkCommandBuffer commandBuffer, int index) {
        for (GuiElement element : GuiManager.elements) {
            System.out.println("TEST");
            VulkanGuiElement vkElement = (VulkanGuiElement) element;
            VKBufferMesh mesh = element.getQuad();

            LongBuffer vertexBuffers = stack.longs(mesh.vertexBuffer);
            LongBuffer offsets = stack.longs(0);
            vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);
            vkCmdBindIndexBuffer(commandBuffer, mesh.indexBuffer, 0, VK_INDEX_TYPE_UINT32);

            int[] pushConstants = new int[2];
            pushConstants[0] = element.id;

            VK10.vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                    Variables.pipelineLayout,
                    0, stack.longs(
                            DescriptorManager.descriptorSetsMap.get(vkElement.texture).get(index)
                    ),
                    null);
            vkCmdPushConstants(commandBuffer, Variables.pipelineLayout, VK_SHADER_STAGE_VERTEX_BIT, 0, pushConstants);
            vkCmdDrawIndexed(commandBuffer, mesh.vkMesh.indices.size(), 1, 0, 0, 0);
        }

    }

    @Override
    public void createShader() {
        graphicsPipeline = VKPipelineManager.createGraphicsPipeline("shaders/gui");
    }


}
