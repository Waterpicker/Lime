package io.github.hydos.lime.core.ui;

import io.github.hydos.lime.impl.vulkan.texture.VKTextureManager;
import io.github.hydos.lime.impl.vulkan.ui.VulkanGuiElement;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class GuiManager {

    public static final List<GuiElement> elements = new ArrayList<>();

    public static void addElement(GuiElement element) {
        elements.add(element);
    }

    public static void removeElement(GuiElement element) {
        elements.remove(element);
    }

    public static GuiElement createUiElement(Vector2f position, Vector2f scale, String imagePath) {
        return new VulkanGuiElement(position, scale, VKTextureManager.getCompiledTexture(imagePath));
    }

}