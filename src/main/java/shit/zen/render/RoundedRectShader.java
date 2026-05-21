package shit.zen.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

public final class RoundedRectShader {
    private int programId = 0;
    private int uModelViewMat = -1;
    private int uProjMat = -1;
    private int uHalfSize = -1;
    private int uRadii = -1;
    private int uColor1 = -1;
    private int uColor2 = -1;
    private int uUseGradient = -1;
    private int uUseTexture = -1;
    private int uSampler0 = -1;
    private int uStrokeWidth = -1;
    private int vboId = 0;
    private int vaoId = 0;

    public void init() {
        if (this.programId != 0) {
            return;
        }
        int n = RoundedRectShader.compileShader(35633, "#version 150\nin vec2 Position;\nin vec2 LocalPos;\nin vec2 UV;\nuniform mat4 ModelViewMat;\nuniform mat4 ProjMat;\nout vec2 localPos;\nout vec2 uvCoord;\nvoid main() {\n    gl_Position = ProjMat * ModelViewMat * vec4(Position, 0.0, 1.0);\n    localPos = LocalPos;\n    uvCoord = UV;\n}\n");
        int n2 = RoundedRectShader.compileShader(35632, "#version 150\nuniform vec2 HalfSize;\nuniform vec4 Radii;\nuniform vec4 Color1;\nuniform vec4 Color2;\nuniform int UseGradient;\nuniform int UseTexture;\nuniform sampler2D Sampler0;\nuniform float StrokeWidth;\nin vec2 localPos;\nin vec2 uvCoord;\nout vec4 fragColor;\nvoid main() {\n    vec2 p = localPos;\n    float r;\n    if (p.x < 0.0) {\n        r = (p.y < 0.0) ? Radii.x : Radii.w;\n    } else {\n        r = (p.y < 0.0) ? Radii.y : Radii.z;\n    }\n    vec2 q = abs(p) - HalfSize + r;\n    float d = length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0) - r;\n    float alpha;\n    if (StrokeWidth > 0.0) {\n        float halfStroke = StrokeWidth * 0.5;\n        alpha = 1.0 - smoothstep(halfStroke - 0.5, halfStroke + 0.5, abs(d));\n    } else {\n        alpha = 1.0 - smoothstep(-0.5, 0.5, d);\n    }\n    vec4 col;\n    if (UseTexture == 1) {\n        col = texture(Sampler0, uvCoord) * Color1;\n    } else if (UseGradient == 1) {\n        float t = clamp((p.y + HalfSize.y) / max(2.0 * HalfSize.y, 0.0001), 0.0, 1.0);\n        col = mix(Color1, Color2, t);\n    } else {\n        col = Color1;\n    }\n    fragColor = vec4(col.rgb, col.a * alpha);\n}\n");
        int n3 = GL20.glCreateProgram();
        GL20.glAttachShader(n3, n);
        GL20.glAttachShader(n3, n2);
        GL20.glBindAttribLocation(n3, 0, "Position");
        GL20.glBindAttribLocation(n3, 1, "LocalPos");
        GL20.glBindAttribLocation(n3, 2, "UV");
        GL20.glLinkProgram(n3);
        if (GL20.glGetProgrami(n3, 35714) == 0) {
            String string = GL20.glGetProgramInfoLog(n3);
            GL20.glDeleteProgram(n3);
            GL20.glDeleteShader(n);
            GL20.glDeleteShader(n2);
            throw new IllegalStateException("RoundedRectShader link failed: " + string);
        }
        GL20.glDeleteShader(n);
        GL20.glDeleteShader(n2);
        this.programId = n3;
        this.uModelViewMat = GL20.glGetUniformLocation(this.programId, "ModelViewMat");
        this.uProjMat = GL20.glGetUniformLocation(this.programId, "ProjMat");
        this.uHalfSize = GL20.glGetUniformLocation(this.programId, "HalfSize");
        this.uRadii = GL20.glGetUniformLocation(this.programId, "Radii");
        this.uColor1 = GL20.glGetUniformLocation(this.programId, "Color1");
        this.uColor2 = GL20.glGetUniformLocation(this.programId, "Color2");
        this.uUseGradient = GL20.glGetUniformLocation(this.programId, "UseGradient");
        this.uUseTexture = GL20.glGetUniformLocation(this.programId, "UseTexture");
        this.uSampler0 = GL20.glGetUniformLocation(this.programId, "Sampler0");
        this.uStrokeWidth = GL20.glGetUniformLocation(this.programId, "StrokeWidth");
        this.vaoId = GL30.glGenVertexArrays();
        this.vboId = GL15.glGenBuffers();
        int n4 = GL11.glGetInteger(34229);
        int n5 = GL11.glGetInteger(34964);
        GL30.glBindVertexArray(this.vaoId);
        GL15.glBindBuffer(34962, this.vboId);
        GL15.glBufferData(34962, 144L, 35048);
        int n6 = 24;
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, 5126, false, n6, 0L);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, 5126, false, n6, 8L);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 2, 5126, false, n6, 16L);
        GL30.glBindVertexArray(n4);
        GL15.glBindBuffer(34962, n5);
    }

    public void draw(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int n, int n2, boolean bl, float f9) {
        this.drawInternal(matrix4f, f, f2, f3, f4, f5, f6, f7, f8, n, n2, bl, f9, -1, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    public void drawTextured(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int n, int n2, float f9, float f10, float f11, float f12) {
        this.drawInternal(matrix4f, f, f2, f3, f4, f5, f6, f7, f8, n, n, false, 0.0f, n2, f9, f10, f11, f12);
    }

    private void drawInternal(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int n, int n2, boolean bl, float f9, int n3, float f10, float f11, float f12, float f13) {
        int n4;
        this.init();
        float f14 = (f + f3) * 0.5f;
        float f15 = (f2 + f4) * 0.5f;
        float f16 = (f3 - f) * 0.5f;
        float f17 = (f4 - f2) * 0.5f;
        if (f16 <= 0.0f || f17 <= 0.0f) {
            return;
        }
        float f18 = Math.min(f16, f17);
        f5 = Math.min(Math.max(f5, 0.0f), f18);
        f6 = Math.min(Math.max(f6, 0.0f), f18);
        f7 = Math.min(Math.max(f7, 0.0f), f18);
        f8 = Math.min(Math.max(f8, 0.0f), f18);
        float f19 = f16 + 1.0f;
        float f20 = f17 + 1.0f;
        Vector4f vector4f = new Vector4f();
        float[] fArray = new float[36];
        float[][] fArrayArray = new float[][]{{-f19, -f20}, {f19, -f20}, {f19, f20}, {-f19, -f20}, {f19, f20}, {-f19, f20}};
        for (n4 = 0; n4 < 6; ++n4) {
            vector4f.set(f14 + fArrayArray[n4][0], f15 + fArrayArray[n4][1], 0.0f, 1.0f).mul(matrix4f);
            float f21 = fArrayArray[n4][0];
            float f22 = fArrayArray[n4][1];
            float f23 = f10 + (f21 + f16) / (2.0f * f16) * (f12 - f10);
            float f24 = f11 + (f22 + f17) / (2.0f * f17) * (f13 - f11);
            int n5 = n4 * 6;
            fArray[n5] = vector4f.x;
            fArray[n5 + 1] = vector4f.y;
            fArray[n5 + 2] = f21;
            fArray[n5 + 3] = f22;
            fArray[n5 + 4] = f23;
            fArray[n5 + 5] = f24;
        }
        n4 = GL11.glGetInteger(35725);
        int n6 = GL11.glGetInteger(34229);
        int n7 = GL11.glGetInteger(34964);
        int n8 = GL11.glGetInteger(34016);
        GL13.glActiveTexture(33984);
        int n9 = GL11.glGetInteger(32873);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        GL20.glUseProgram(this.programId);
        GL30.glBindVertexArray(this.vaoId);
        GL15.glBindBuffer(34962, this.vboId);
        try (MemoryStack memoryStack = MemoryStack.stackPush()){
            FloatBuffer floatBuffer = memoryStack.mallocFloat(fArray.length);
            floatBuffer.put(fArray).flip();
            GL15.glBufferSubData(34962, 0L, floatBuffer);
            FloatBuffer floatBuffer2 = memoryStack.mallocFloat(16);
            RenderSystem.getModelViewMatrix().get(floatBuffer2);
            GL20.glUniformMatrix4fv(this.uModelViewMat, false, floatBuffer2);
            FloatBuffer floatBuffer3 = memoryStack.mallocFloat(16);
            RenderSystem.getProjectionMatrix().get(floatBuffer3);
            GL20.glUniformMatrix4fv(this.uProjMat, false, floatBuffer3);
        }
        GL20.glUniform2f(this.uHalfSize, f16, f17);
        GL20.glUniform4f(this.uRadii, f5, f6, f7, f8);
        GL20.glUniform4f(this.uColor1, (float)(n >> 16 & 0xFF) / 255.0f, (float)(n >> 8 & 0xFF) / 255.0f, (float)(n & 0xFF) / 255.0f, (float)(n >>> 24 & 0xFF) / 255.0f);
        GL20.glUniform4f(this.uColor2, (float)(n2 >> 16 & 0xFF) / 255.0f, (float)(n2 >> 8 & 0xFF) / 255.0f, (float)(n2 & 0xFF) / 255.0f, (float)(n2 >>> 24 & 0xFF) / 255.0f);
        GL20.glUniform1i(this.uUseGradient, bl ? 1 : 0);
        GL20.glUniform1f(this.uStrokeWidth, Math.max(0.0f, f9));
        if (n3 > 0) {
            GL20.glUniform1i(this.uUseTexture, 1);
            GL20.glUniform1i(this.uSampler0, 0);
            GL11.glBindTexture(3553, n3);
            GL11.glTexParameteri(3553, 10241, 9728);
            GL11.glTexParameteri(3553, 10240, 9728);
        } else {
            GL20.glUniform1i(this.uUseTexture, 0);
        }
        GL11.glDrawArrays(4, 0, 6);
        GL11.glBindTexture(3553, n9);
        GL13.glActiveTexture(n8);
        GL15.glBindBuffer(34962, n7);
        GL30.glBindVertexArray(n6);
        GL20.glUseProgram(n4);
    }

    public void dispose() {
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
            throw new IllegalStateException("RoundedRectShader shader compile failed: " + string2);
        }
        return n2;
    }
}