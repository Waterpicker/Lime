package io.github.hydos.lime.core;

import io.github.hydos.lime.core.io.Window;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

/**
 * this class is only for moving around the scene and testing
 */
@Deprecated
public class PlayerController {

    public static Vector3f position = new Vector3f(2, 0 , 0);
    public static Vector3f rotation = new Vector3f();

    public static void onInput(){
        if(Window.isKeyDown(GLFW.GLFW_KEY_A)){
            position.add(0, 0, 0.1f);
            rotation.add(0,0,0.1f);
        }
        if(Window.isKeyDown(GLFW.GLFW_KEY_D)){
            rotation.add(0,0,-0.1f);
            position.add(0, 0, -0.1f);
        }
        if(Window.isKeyDown(GLFW.GLFW_KEY_W)){
            rotation.add(-0.1f,0,0);
            position.add(-0.1f, 0, 0);
        }
        if(Window.isKeyDown(GLFW.GLFW_KEY_S)){
            rotation.add(0.1f,0,0);
            position.add(0.1f, 0, 0);
        }
    }

}
