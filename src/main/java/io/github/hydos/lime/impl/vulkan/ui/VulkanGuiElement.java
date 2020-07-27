package io.github.hydos.lime.impl.vulkan.ui;

import io.github.hydos.lime.core.ui.GuiElement;
import io.github.hydos.lime.impl.vulkan.elements.VulkanRenderObject;
import io.github.hydos.lime.impl.vulkan.model.VKBufferMesh;
import io.github.hydos.lime.impl.vulkan.texture.CompiledTexture;
import org.joml.Vector2f;

public class VulkanGuiElement extends GuiElement {

    public CompiledTexture texture;

    private static int lastRegistedId = 0;

    public VulkanGuiElement(Vector2f positon, Vector2f scale, CompiledTexture texture){
        this.position = positon;
        this.scale = scale;
        this.texture = texture;
        this.id = lastRegistedId;
        VulkanGuiElement.lastRegistedId++;
    }

    @Override
    public VKBufferMesh getQuad() {
        return null;
    }
}
