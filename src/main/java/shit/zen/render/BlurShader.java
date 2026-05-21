package shit.zen.render;

import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

public final class BlurShader {
    private int programId = 0;
    private int samplerUniform = -1;
    private int blurDirUniform = -1;
    private int texelSizeUniform = -1;
    private int radiusUniform = -1;
    private int vboId = 0;
    private int vaoId = 0;

    public void init() {
        if (this.programId != 0) {
            return;
        }
        int n = BlurShader.compileShader(35633, "#version 150\nin vec3 Position;\nin vec2 UV0;\nout vec2 texCoord;\nvoid main() {\n    gl_Position = vec4(Position, 1.0);\n    texCoord = UV0;\n}\n");
        int n2 = BlurShader.compileShader(35632, "#version 150\nuniform sampler2D Sampler0;\nuniform vec2 BlurDir;\nuniform vec2 TexelSize;\nuniform float Radius;\nin vec2 texCoord;\nout vec4 fragColor;\nvoid main() {\n    float sigma = max(Radius, 1.0);\n    int halfK = int(ceil(sigma * 2.0));\n    if (halfK > 24) halfK = 24;\n    float twoSigmaSq = 2.0 * sigma * sigma;\n    vec4 sum = vec4(0.0);\n    float weightSum = 0.0;\n    for (int i = -halfK; i <= halfK; i++) {\n        float w = exp(-float(i * i) / twoSigmaSq);\n        vec2 offset = BlurDir * float(i) * TexelSize;\n        sum += texture(Sampler0, texCoord + offset) * w;\n        weightSum += w;\n    }\n    fragColor = sum / max(weightSum, 0.0001);\n}\n");
        int n3 = GL20.glCreateProgram();
        GL20.glAttachShader(n3, n);
        GL20.glAttachShader(n3, n2);
        GL20.glBindAttribLocation(n3, 0, "Position");
        GL20.glBindAttribLocation(n3, 1, "UV0");
        GL20.glLinkProgram(n3);
        if (GL20.glGetProgrami(n3, 35714) == 0) {
            String string = GL20.glGetProgramInfoLog(n3);
            GL20.glDeleteProgram(n3);
            GL20.glDeleteShader(n);
            GL20.glDeleteShader(n2);
            throw new IllegalStateException("Blur shader link failed: " + string);
        }
        GL20.glDeleteShader(n);
        GL20.glDeleteShader(n2);
        this.programId = n3;
        this.samplerUniform = GL20.glGetUniformLocation(this.programId, "Sampler0");
        this.blurDirUniform = GL20.glGetUniformLocation(this.programId, "BlurDir");
        this.texelSizeUniform = GL20.glGetUniformLocation(this.programId, "TexelSize");
        this.radiusUniform = GL20.glGetUniformLocation(this.programId, "Radius");
        this.vaoId = GL30.glGenVertexArrays();
        this.vboId = GL15.glGenBuffers();
        int n4 = GL11.glGetInteger(34229);
        int n5 = GL11.glGetInteger(34964);
        GL30.glBindVertexArray(this.vaoId);
        GL15.glBindBuffer(34962, this.vboId);
        try (MemoryStack memoryStack = MemoryStack.stackPush()){
            FloatBuffer floatBuffer = memoryStack.mallocFloat(30);
            BlurShader.putVertex(floatBuffer, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f);
            BlurShader.putVertex(floatBuffer, 1.0f, -1.0f, 0.0f, 1.0f, 0.0f);
            BlurShader.putVertex(floatBuffer, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f);
            BlurShader.putVertex(floatBuffer, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f);
            BlurShader.putVertex(floatBuffer, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f);
            BlurShader.putVertex(floatBuffer, -1.0f, 1.0f, 0.0f, 0.0f, 1.0f);
            floatBuffer.flip();
            GL15.glBufferData(34962, floatBuffer, 35044);
        }
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, 5126, false, 20, 0L);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, 5126, false, 20, 12L);
        GL30.glBindVertexArray(n4);
        GL15.glBindBuffer(34962, n5);
    }

    private static void putVertex(FloatBuffer floatBuffer, float f, float f2, float f3, float f4, float f5) {
        floatBuffer.put(f).put(f2).put(f3).put(f4).put(f5);
    }

    public void render(int n, float f, float f2, int n2, int n3, float f3) {
        int n4 = GL11.glGetInteger(35725);
        int n5 = GL11.glGetInteger(34229);
        int n6 = GL11.glGetInteger(34016);
        GL13.glActiveTexture(33984);
        int n7 = GL11.glGetInteger(32873);
        GL20.glUseProgram(this.programId);
        GL20.glUniform2f(this.blurDirUniform, f, f2);
        GL20.glUniform2f(this.texelSizeUniform, 1.0f / (float)n2, 1.0f / (float)n3);
        GL20.glUniform1f(this.radiusUniform, Math.max(f3, 1.0f));
        GL20.glUniform1i(this.samplerUniform, 0);
        GL11.glBindTexture(3553, n);
        GL30.glBindVertexArray(this.vaoId);
        GL11.glDrawArrays(4, 0, 6);
        GL11.glBindTexture(3553, n7);
        GL13.glActiveTexture(n6);
        GL30.glBindVertexArray(n5);
        GL20.glUseProgram(n4);
    }

    public void delete() {
        if (this.programId != 0) {
            GL20.glDeleteProgram(this.programId);
            this.programId = 0;
        }
        if (this.vboId != 0) {
            GL15.glDeleteBuffers(this.vboId);
            this.vboId = 0;
        }
        if (this.vaoId != 0) {
            GL30.glDeleteVertexArrays(this.vaoId);
            this.vaoId = 0;
        }
    }

    private static int compileShader(int n, String string) {
        int n2 = GL20.glCreateShader(n);
        GL20.glShaderSource(n2, string);
        GL20.glCompileShader(n2);
        if (GL20.glGetShaderi(n2, 35713) == 0) {
            String string2 = GL20.glGetShaderInfoLog(n2);
            GL20.glDeleteShader(n2);
            throw new IllegalStateException("Blur shader " + n + " compile failed: " + string2);
        }
        return n2;
    }
}