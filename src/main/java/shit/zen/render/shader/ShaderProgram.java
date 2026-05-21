package shit.zen.render.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import lombok.Getter;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import shit.zen.render.shader.Matrix4Uniform;
import shit.zen.render.shader.ShaderFormats;
import shit.zen.render.shader.ShaderSource;

public class ShaderProgram {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Getter
    private final int programId;
    @Getter
    private final Map<String, Integer> uniformCache = new HashMap<>();
    @Getter
    private final Matrix4Uniform modelViewUniform;
    @Getter
    private final Matrix4Uniform projectionUniform;
    @Getter
    private Matrix4f cachedModelView;
    @Getter
    private Matrix4f cachedProjection;
    private static int prevProgram;

    public ShaderProgram(String string) {
        this(string, "vertex", ShaderFormats.POSITION_UV, "ModelViewMat", "ProjMat");
    }

    public ShaderProgram(String string, String string2, Supplier<Map<Integer, String>> supplier) {
        this(string, string2, supplier, "ModelViewMat", "ProjMat");
    }

    public ShaderProgram(String string, Supplier<Map<Integer, String>> supplier) {
        this(string, string, supplier, "ModelViewMat", "ProjMat");
    }

    public ShaderProgram(String string, Supplier<Map<Integer, String>> supplier, String string2, String string3) {
        this(string, string, supplier, string2, string3);
    }

    public ShaderProgram(String string, String string2, Supplier<Map<Integer, String>> supplier, String string3, String string4) {
        this.programId = GL20.glCreateProgram();
        int n = ShaderProgram.compileShader(ShaderSource.getByFileName(string + ".fsh").getSource(), 35632);
        int n2 = ShaderProgram.compileShader(ShaderSource.getByFileName(string2 + ".vsh").getSource(), 35633);
        GL20.glAttachShader(this.programId, n);
        GL20.glAttachShader(this.programId, n2);
        for (Map.Entry<Integer, String> entry : supplier.get().entrySet()) {
            GL20.glEnableVertexAttribArray(entry.getKey());
            GL20.glBindAttribLocation(this.programId, entry.getKey(), entry.getValue());
        }
        GL20.glLinkProgram(this.programId);
        if (GL20.glGetProgrami(this.programId, 35714) == 0) {
            LOGGER.error(GL20.glGetProgramInfoLog(this.programId, Short.MAX_VALUE));
            throw new IllegalStateException("Failed to link shader program!");
        }
        GL20.glDeleteShader(n);
        GL20.glDeleteShader(n2);
        this.modelViewUniform = new Matrix4Uniform(string3).bindToProgram(this.programId);
        this.projectionUniform = new Matrix4Uniform(string4).bindToProgram(this.programId);
    }

    public void use() {
        prevProgram = GL20.glGetInteger(35725);
        GL20.glUseProgram(this.programId);
        this.setModelView(RenderSystem.getModelViewMatrix());
        this.setProjection(RenderSystem.getProjectionMatrix());
    }

    public void setModelView(Matrix4f matrix4f) {
        if (this.cachedModelView != matrix4f) {
            this.modelViewUniform.upload(matrix4f);
            this.cachedModelView = matrix4f;
        }
    }

    public void setProjection(Matrix4f matrix4f) {
        if (this.cachedProjection != matrix4f) {
            this.projectionUniform.upload(matrix4f);
            this.cachedProjection = matrix4f;
        }
    }

    public void stopUsing() {
        GL20.glUseProgram(prevProgram);
    }

    public int getUniformLocation(String string) {
        if (!this.uniformCache.containsKey(string)) {
            this.uniformCache.put(string, GL20.glGetUniformLocation(this.programId, string));
        }
        return this.uniformCache.get(string);
    }

    private static int compileShader(String string, int n) {
        int n2 = GL20.glCreateShader(n);
        Matcher matcher = ShaderFormats.IMPORT_PATTERN.matcher(string);
        while (matcher.find()) {
            boolean bl = matcher.group(2) == null;
            if (!bl) continue;
            String string2 = matcher.group(3);
            String string3 = ShaderSource.getByFileName(string2).getSource();
            string = string.replaceAll(ShaderFormats.IMPORT_PATTERN.pattern(), string3);
        }
        GL20.glShaderSource(n2, string);
        GL20.glCompileShader(n2);
        if (GL20.glGetShaderi(n2, 35713) == 0) {
            LOGGER.error(GL20.glGetShaderInfoLog(n2, Short.MAX_VALUE));
            throw new IllegalStateException(String.format("Failed to compile shader! (Type: %s)", new Object[]{n}));
        }
        return n2;
    }

    }