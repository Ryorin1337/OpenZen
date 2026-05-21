package shit.zen.utils.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import java.awt.image.BufferedImage;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.joml.Matrix4f;
import shit.zen.ClientBase;
import shit.zen.utils.render.ColorUtil;

public final class RenderHelper {
    public static void blitRenderTarget(RenderTarget renderTarget, PoseStack poseStack, int n, int n2) {
        Matrix4f matrix4f = poseStack.last().pose();
        ShaderInstance shaderInstance = ClientBase.mc.gameRenderer.blitShader;
        shaderInstance.setSampler("DiffuseSampler", renderTarget.getColorTextureId());
        shaderInstance.apply();
        float f = (float)renderTarget.viewWidth / (float)renderTarget.width;
        float f2 = (float)renderTarget.viewHeight / (float)renderTarget.height;
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(matrix4f, 0.0f, (float)n2, 0.0f).uv(0.0f, 0.0f).color(255, 255, 255, 255).endVertex();
        bufferBuilder.vertex(matrix4f, (float)n, (float)n2, 0.0f).uv(f, 0.0f).color(255, 255, 255, 255).endVertex();
        bufferBuilder.vertex(matrix4f, (float)n, 0.0f, 0.0f).uv(f, f2).color(255, 255, 255, 255).endVertex();
        bufferBuilder.vertex(matrix4f, 0.0f, 0.0f, 0.0f).uv(0.0f, f2).color(255, 255, 255, 255).endVertex();
        BufferUploader.draw(bufferBuilder.end());
        shaderInstance.clear();
    }

    public static void blitRenderTargetSafe(RenderTarget renderTarget, PoseStack poseStack, int n, int n2) {
        RenderSystem.assertOnRenderThread();
        Matrix4f matrix4f = poseStack.last().pose();
        Minecraft minecraft = ClientBase.mc;
        ShaderInstance shaderInstance = minecraft.gameRenderer.blitShader;
        shaderInstance.setSampler("DiffuseSampler", renderTarget.getColorTextureId());
        shaderInstance.apply();
        float f = n;
        float f2 = n2;
        float f3 = (float)renderTarget.viewWidth / (float)renderTarget.width;
        float f4 = (float)renderTarget.viewHeight / (float)renderTarget.height;
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(matrix4f, 0.0f, f2, 0.0f).uv(0.0f, 0.0f).color(255, 255, 255, 255).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).uv(f3, 0.0f).color(255, 255, 255, 255).endVertex();
        bufferBuilder.vertex(matrix4f, f, 0.0f, 0.0f).uv(f3, f4).color(255, 255, 255, 255).endVertex();
        bufferBuilder.vertex(matrix4f, 0.0f, 0.0f, 0.0f).uv(0.0f, f4).color(255, 255, 255, 255).endVertex();
        BufferUploader.draw(bufferBuilder.end());
        shaderInstance.clear();
    }

    public static void setTexFilter(int n, int n2) {
        RenderSystem.texParameter(3553, 10241, n);
        RenderSystem.texParameter(3553, 10240, n2);
    }

    public static void pushScaleAround(PoseStack poseStack, float f, float f2, float f3) {
        poseStack.pushPose();
        poseStack.translate(f, f2, 0.0f);
        poseStack.scale(f3, f3, 1.0f);
        poseStack.translate(-f, -f2, 0.0f);
    }

    public static void popPose(PoseStack poseStack) {
        poseStack.popPose();
    }

    public static void pushRotateAround(PoseStack poseStack, float f, float f2, float f3) {
        poseStack.pushPose();
        poseStack.translate(f, f2, 0.0f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(f3));
        poseStack.translate(-f, -f2, 0.0f);
    }

    public static void resetShaderColor() {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void setShaderColorRGBA(int n, int n2, int n3, int n4) {
        RenderSystem.setShaderColor((float)n / 255.0f, (float)n2 / 255.0f, (float)n3 / 255.0f, (float)n4 / 255.0f);
    }

    public static void setShaderColorWithAlpha(int n, int n2) {
        RenderSystem.setShaderColor((float)ColorUtil.getRed(n) / 255.0f, (float)ColorUtil.getGreen(n) / 255.0f, (float)ColorUtil.getBlue(n) / 255.0f, (float)n2 / 255.0f);
    }

    public static void setShaderColor(int n) {
        RenderSystem.setShaderColor((float)ColorUtil.getRed(n) / 255.0f, (float)ColorUtil.getGreen(n) / 255.0f, (float)ColorUtil.getBlue(n) / 255.0f, (float)ColorUtil.getAlpha(n) / 255.0f);
    }

    public static void withBlend(Runnable runnable) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        runnable.run();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public static void setShaderColorComponents(int n) {
        RenderHelper.setShaderColorRGBA(ColorUtil.getRed(n), ColorUtil.getGreen(n), ColorUtil.getBlue(n), ColorUtil.getAlpha(n));
    }

    public static DynamicTexture uploadTexture(NativeImage nativeImage, BufferedImage bufferedImage) {
        for (int i = 0; i < bufferedImage.getWidth(); ++i) {
            for (int j = 0; j < bufferedImage.getHeight(); ++j) {
                nativeImage.setPixelRGBA(i, j, bufferedImage.getRGB(i, j));
            }
        }
        return new DynamicTexture(nativeImage);
    }

    @Generated
    private RenderHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}