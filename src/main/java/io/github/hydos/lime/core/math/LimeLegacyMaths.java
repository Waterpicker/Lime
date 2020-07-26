package io.github.hydos.lime.core.math;

//import com.github.hydos.ginger.engine.common.cameras.Camera;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class LimeLegacyMaths {
    private static final Vector3f XVEC = new Vector3f(1, 0, 0);
    private static final Vector3f YVEC = new Vector3f(0, 1, 0);
    private static final Vector3f ZVEC = new Vector3f(0, 0, 1);
    private static final Matrix4f matrix = new Matrix4f();

    public static float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {
        float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
        float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
        float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
        float l3 = 1.0f - l1 - l2;
        return l1 * p1.y + l2 * p2.y + l3 * p3.y;
    }

    public static Matrix4f createTransformationMatrix(Vector2f translation, Vector2f scale) {
        Matrix4f matrix = new Matrix4f();
        matrix.identity();
        matrix.translate(new org.joml.Vector3f(translation.x, translation.y, 0), matrix);
        matrix.scale(new Vector3f(scale.x, scale.y, 1f), matrix);
        return matrix;
    }

    public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, float scale) {
        Matrix4f matrix = new Matrix4f();
        matrix.identity();
        matrix.translate(translation, matrix);
        matrix.rotate((float) Math.toRadians(rx), new Vector3f(1, 0, 0), matrix);
        matrix.rotate((float) Math.toRadians(ry), new Vector3f(0, 1, 0), matrix);
        matrix.rotate((float) Math.toRadians(rz), new Vector3f(0, 0, 1), matrix);
        Vector3f newScale = new Vector3f(scale, scale, scale);
        matrix.scale(newScale, matrix);
        return matrix;
    }

    public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, Vector3f scale) {
        Matrix4f mat4 = new Matrix4f();
        mat4.zero();
        mat4.identity();
        mat4.translate(translation);
        mat4.rotate((float) Math.toRadians(rx), XVEC);
        mat4.rotate((float) Math.toRadians(ry), YVEC);
        mat4.rotate((float) Math.toRadians(rz), ZVEC);
        mat4.scale(scale);
        return mat4;
    }

    public static Matrix4f createViewMatrix(Vector3f cameraPos, Vector3f rotation) {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.identity();
        viewMatrix.rotate((float) Math.toRadians(rotation.x), new Vector3f(1, 0, 0), viewMatrix);
        viewMatrix.rotate((float) Math.toRadians(rotation.y), new Vector3f(0, 1, 0), viewMatrix);
        viewMatrix.rotate((float) Math.toRadians(rotation.z), new Vector3f(0, 0, 1), viewMatrix);
        Vector3f negativeCameraPos = new Vector3f(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        viewMatrix.translate(negativeCameraPos, viewMatrix);
        return viewMatrix;
    }

    public static Matrix4f calcView(Vector3f pos, Vector3f lookAt) {
        return new Matrix4f().lookAt(pos.x, pos.y, pos.z, lookAt.x, lookAt.y, lookAt.z, 0f, 1f, 0f);
    }
}
