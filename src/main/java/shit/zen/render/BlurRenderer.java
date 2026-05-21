package shit.zen.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import shit.zen.render.BlurFbo;
import shit.zen.render.BlurShader;
import shit.zen.render.DrawContext;

public final class BlurRenderer {
    private static final BlurFbo fboA = new BlurFbo();
    private static final BlurFbo fboB = new BlurFbo();
    private static final BlurShader blurShader = new BlurShader();
    private static boolean initialized = false;

    public static void ensureInitialized() {
        if (initialized) {
            return;
        }
        blurShader.init();
        initialized = true;
    }

    public static void renderBlur(DrawContext drawContext, float f, float f2, float f3, float f4, float f5, Runnable runnable) {
        if (f3 <= 0.0f || f4 <= 0.0f || f5 <= 0.001f) {
            runnable.run();
            return;
        }
        BlurRenderer.ensureInitialized();
        float f6 = Math.max(4.0f, f5 * 3.0f);
        float f7 = f - f6;
        float f8 = f2 - f6;
        float f9 = f3 + 2.0f * f6;
        float f10 = f4 + 2.0f * f6;
        float f11 = (float)Minecraft.getInstance().getWindow().getGuiScale();
        int n = Math.max(4, Math.min(2048, (int)Math.ceil(f9 * f11)));
        int n2 = Math.max(4, Math.min(2048, (int)Math.ceil(f10 * f11)));
        fboA.resize(n, n2);
        fboB.resize(n, n2);
        int n3 = GL11.glGetInteger(36006);
        int[] nArray = new int[4];
        GL11.glGetIntegerv(2978, nArray);
        Matrix4f matrix4f = RenderSystem.getProjectionMatrix();
        VertexSorting vertexSorting = RenderSystem.getVertexSorting();
        Matrix4f matrix4f2 = new Matrix4f().setOrtho(f7, f7 + f9, f8 + f10, f8, 1000.0f, 3000.0f);
        fboA.bind();
        fboA.clear();
        RenderSystem.setProjectionMatrix(matrix4f2, VertexSorting.ORTHOGRAPHIC_Z);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        try {
            runnable.run();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        float f12 = Math.max(1.0f, f5 * f11);
        GlStateManager._disableBlend();
        fboB.bind();
        fboB.clear();
        blurShader.render(fboA.getTextureId(), 1.0f, 0.0f, n, n2, f12);
        fboA.bind();
        fboA.clear();
        blurShader.render(fboB.getTextureId(), 0.0f, 1.0f, n, n2, f12);
        GL30.glBindFramebuffer(36160, n3);
        GL11.glViewport(nArray[0], nArray[1], nArray[2], nArray[3]);
        RenderSystem.setProjectionMatrix(matrix4f, vertexSorting);
        BlurRenderer.blitTexture(drawContext, fboA.getTextureId(), f7, f8, f9, f10);
    }

    private static void blitTexture(DrawContext drawContext, int n, float f, float f2, float f3, float f4) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, n);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        Matrix4f matrix4f = drawContext.getPoseStack().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).uv(0.0f, 1.0f).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).uv(0.0f, 0.0f).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).uv(1.0f, 0.0f).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).uv(1.0f, 1.0f).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        tesselator.end();
        RenderSystem.defaultBlendFunc();
    }

    public static void cleanup() {
        fboA.delete();
        fboB.delete();
        blurShader.delete();
        initialized = false;
    }
}