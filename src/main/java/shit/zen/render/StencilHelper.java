package shit.zen.render;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import shit.zen.ClientBase;
import shit.zen.render.shader.ShaderProgram;
import shit.zen.utils.misc.ReflectionUtil;

public class StencilHelper {
    private static final ShaderProgram stencilShader = new ShaderProgram("stencil");
    private static final Supplier<TextureTarget> maskTarget = Suppliers.memoize(() -> new TextureTarget(360, 360, false, Minecraft.ON_OSX));
    private static final Supplier<TextureTarget> contentTarget = Suppliers.memoize(() -> new TextureTarget(360, 360, false, Minecraft.ON_OSX));
    private static final RenderTarget mainRenderTarget = ClientBase.mc.getMainRenderTarget();

    public static void applyStencil(PoseStack poseStack, Runnable runnable, Runnable runnable2, float f) {
        Matrix4f matrix4f = poseStack.last().pose();
        RenderTarget renderTarget = maskTarget.get();
        if (renderTarget.width != StencilHelper.mainRenderTarget.width || renderTarget.height != StencilHelper.mainRenderTarget.height) {
            renderTarget.resize(StencilHelper.mainRenderTarget.width, StencilHelper.mainRenderTarget.height, Minecraft.ON_OSX);
        }
        renderTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        renderTarget.clear(Minecraft.ON_OSX);
        renderTarget.bindWrite(false);
        runnable.run();
        RenderTarget renderTarget2 = contentTarget.get();
        if (renderTarget2.width != StencilHelper.mainRenderTarget.width || renderTarget2.height != StencilHelper.mainRenderTarget.height) {
            renderTarget2.resize(StencilHelper.mainRenderTarget.width, StencilHelper.mainRenderTarget.height, Minecraft.ON_OSX);
        }
        renderTarget2.clear(Minecraft.ON_OSX);
        renderTarget2.bindWrite(false);
        runnable2.run();
        mainRenderTarget.bindWrite(false);
        RenderSystem.disableBlend();
        stencilShader.use();
        GL20.glUniform1f(stencilShader.getUniformLocation("Opacity"), f);
        GL20.glUniform1i(stencilShader.getUniformLocation("StencilTexture"), 2);
        GL20.glUniform1i(stencilShader.getUniformLocation("TargetTexture"), 0);
        GL13.glActiveTexture(33986);
        GlStateManager._bindTexture(renderTarget.getColorTextureId());
        GL13.glActiveTexture(33984);
        GlStateManager._bindTexture(renderTarget2.getColorTextureId());
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f, 0.0f, 0.0f, 0.0f).uv(0.0f, 1.0f).endVertex();
        bufferBuilder.vertex(matrix4f, 0.0f, (float)ClientBase.mc.getWindow().getGuiScaledHeight(), 0.0f).uv(0.0f, 0.0f).endVertex();
        bufferBuilder.vertex(matrix4f, (float)ClientBase.mc.getWindow().getGuiScaledWidth(), (float)ClientBase.mc.getWindow().getGuiScaledHeight(), 0.0f).uv(1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(matrix4f, (float)ClientBase.mc.getWindow().getGuiScaledWidth(), 0.0f, 0.0f).uv(1.0f, 1.0f).endVertex();
        BufferUploader.draw(bufferBuilder.end());
        stencilShader.stopUsing();
        RenderSystem.enableBlend();
    }

    public static void beginWrite(boolean bl) {
        StencilHelper.beginWriteFull(bl, ClientBase.mc.getMainRenderTarget(), true, false);
    }

    public static void beginWriteFull(boolean bl, RenderTarget renderTarget, boolean bl2, boolean bl3) {
        renderTarget.bindWrite(false);
        StencilHelper.setupFBO(renderTarget);
        GL11.glEnable(2960);
        if (bl2) {
            GL11.glClearStencil(0);
            GL11.glClear(1024);
        }
        GL11.glStencilFunc(519, bl3 ? 0 : 1, 255);
        GL11.glStencilOp(7680, 7680, 7681);
        if (!bl) {
            GlStateManager._colorMask(false, false, false, false);
        }
    }

    public static void beginRead(boolean bl) {
        GL11.glStencilFunc(bl ? 514 : 517, 1, 255);
        GL11.glStencilOp(7680, 7680, 7681);
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._enableBlend();
    }

    public static void end() {
        GL11.glDisable(2960);
    }

    public static void setupFBO(RenderTarget renderTarget) {
        if (renderTarget != null && renderTarget.getDepthTextureId() > -1) {
            StencilHelper.attachStencilBuffer(renderTarget);
            ReflectionUtil.setDepthBufferId(renderTarget, -1);
        }
    }

    public static void attachStencilBuffer(RenderTarget renderTarget) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(renderTarget.getDepthTextureId());
        int n = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(36161, n);
        EXTFramebufferObject.glRenderbufferStorageEXT(36161, 34041, ClientBase.mc.getMainRenderTarget().width, ClientBase.mc.getMainRenderTarget().height);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, n);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, n);
    }
}