package shit.zen.render.shader;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;

public class BufferUtil {
    public static void fill(FloatBuffer floatBuffer, float f) {
        floatBuffer.clear();
        for (int i = 0; i < floatBuffer.capacity(); ++i) {
            floatBuffer.put(i, f);
        }
        floatBuffer.clear();
    }

    public static FloatBuffer storeMatrix(FloatBuffer floatBuffer, Matrix4f matrix4f) {
        return matrix4f.get(floatBuffer);
    }
}