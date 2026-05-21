package shit.zen.render.shader;

import java.nio.FloatBuffer;
import lombok.Getter;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;
import shit.zen.render.shader.BufferUtil;
import shit.zen.render.shader.Uniform;

public class Matrix4Uniform
extends Uniform<Matrix4Uniform> {
    @Getter
    private boolean transpose;
    @Getter
    private final FloatBuffer dataBuffer = MemoryUtil.memAllocFloat(16);
    @Getter
    private final FloatBuffer stagingBuffer = MemoryUtil.memAllocFloat(16);

    public Matrix4Uniform(String string) {
        super(string);
    }

    public void upload(Matrix4f matrix4f) {
        this.stagingBuffer.clear();
        BufferUtil.storeMatrix(this.stagingBuffer, matrix4f);
        this.uploadRaw(false, this.stagingBuffer);
    }

    public void uploadRaw(boolean bl, FloatBuffer floatBuffer) {
        this.transpose = bl;
        floatBuffer.mark();
        this.dataBuffer.clear();
        this.dataBuffer.put(floatBuffer);
        this.dataBuffer.rewind();
        floatBuffer.reset();
        int n = this.getLocation();
        if (n >= 0) {
            GL20.glUniformMatrix4fv(n, bl, this.dataBuffer);
        }
    }

    public float getElement(int n, int n2) {
        int n3 = this.transpose ? n2 * 4 + n : n * 4 + n2;
        return this.dataBuffer.get(n3);
    }

    protected void clear() {
        BufferUtil.fill(this.dataBuffer, 0.0f);
    }

    }