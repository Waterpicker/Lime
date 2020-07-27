package io.github.hydos.lime.core.ui;

import io.github.hydos.lime.impl.vulkan.model.VKBufferMesh;
import org.joml.Vector2f;

public abstract class GuiElement {
    public Vector2f position;
    public Vector2f scale;
    public boolean hidden;

    public abstract VKBufferMesh getQuad();
}
