package shit.zen.utils.render;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.imageio.ImageIO;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import shit.zen.ClientBase;
import shit.zen.render.GaussianBlur;
import shit.zen.render.ResourceLocationWrapper;
import shit.zen.render.shader.ShaderFormats;
import shit.zen.render.shader.ShaderProgram;
import shit.zen.utils.animation.Timer;
import shit.zen.utils.game.EntityUtil;
import shit.zen.utils.render.ColorUtil;
import shit.zen.utils.render.RenderHelper;
import shit.zen.utils.render.RenderUtil.ShadowTexture;

public final class RenderUtil
extends ClientBase {

    public static class ShadowTexture {
        public shit.zen.render.ResourceLocationWrapper resourceLocation =
                new shit.zen.render.ResourceLocationWrapper("texture/remote/"
                        + org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(16));

        public ShadowTexture(java.awt.image.BufferedImage image) {
            RenderUtil.registerTexture(this.resourceLocation, image);
        }

        public void bind() {
            com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, this.resourceLocation.get());
        }
    }

    private static final Stack<int[]> scissorStack = new Stack<>();
    private static final Map<Integer, RenderUtil.ShadowTexture> shadowCache = new HashMap<>();
    private static ShaderProgram blurShader;
    private static final Supplier<TextureTarget> textureTargetSupplier;
    private static RenderTarget mainRenderTarget;
    private static ShaderProgram roundedRectShader;
    private static boolean blurFailed;
    private static float zLevel;

    public static void drawGradientV(PoseStack poseStack, float f, float f2, float f3, float f4, int n, int n2) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderHelper.resetShaderColor();
        Matrix4f matrix4f = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2, zLevel).color(n).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2 + f4, zLevel).color(n2).endVertex();
        bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, zLevel).color(n2).endVertex();
        bufferBuilder.vertex(matrix4f, f + f3, f2, zLevel).color(n).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void drawGradientH(PoseStack poseStack, float f, float f2, float f3, float f4, int n, int n2) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderHelper.resetShaderColor();
        Matrix4f matrix4f = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2, zLevel).color(n).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2 + f4, zLevel).color(n).endVertex();
        bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, zLevel).color(n2).endVertex();
        bufferBuilder.vertex(matrix4f, f + f3, f2, zLevel).color(n2).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void drawDiamond(PoseStack poseStack, float f, float f2, float f3, float f4, float f5, int n) {
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glEnable(2848);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f matrix4f = poseStack.last().pose();
        float f6 = (float)(n >> 24 & 0xFF) / 255.0f;
        float f7 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f8 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f9 = (float)(n & 0xFF) / 255.0f;
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(f7, f8, f9, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f - f3 / f4, f2 + f3, 0.0f).color(f7, f8, f9, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2 + f3 / f5, 0.0f).color(f7, f8, f9, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f + f3 / f4, f2 + f3, 0.0f).color(f7, f8, f9, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(f7, f8, f9, f6).endVertex();
        Tesselator.getInstance().end();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(2848);
    }

    public static void drawRoundedRect(PoseStack poseStack, float f, float f2, float f3, float f4, float f5, int n) {
        RenderUtil.drawRoundedRect(poseStack, f, f2, f3, f4, f5, 1.0f, n);
    }

    public static void drawRoundedRect(PoseStack poseStack, float f, float f2, float f3, float f4, float f5, float f6, int n) {
        if (roundedRectShader == null) {
            roundedRectShader = new ShaderProgram("rounded_rect", "vertex_color", ShaderFormats.POSITION_UV_COLOR);
        }
        Matrix4f matrix4f = poseStack.last().pose();
        roundedRectShader.use();
        GL20.glUniform2f(roundedRectShader.getUniformLocation("Size"), f3, f4);
        GL20.glUniform1f(roundedRectShader.getUniformLocation("Radius"), f5);
        GL20.glUniform1f(roundedRectShader.getUniformLocation("Smoothness"), f6);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).uv(0.0f, 0.0f).color(n).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).uv(0.0f, 1.0f).color(n).endVertex();
        bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).uv(1.0f, 1.0f).color(n).endVertex();
        bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).uv(1.0f, 0.0f).color(n).endVertex();
        BufferUploader.draw(bufferBuilder.end());
        RenderSystem.disableBlend();
        roundedRectShader.stopUsing();
    }

    public static void drawRoundedRectCorners(PoseStack poseStack, float f, float f2, float f3, float f4, float f5, boolean bl, boolean bl2, boolean bl3, boolean bl4, int n) {
        if (f5 <= 0.0f || !bl && !bl2 && !bl3 && !bl4) {
            RenderUtil.drawFilledRect(poseStack, f, f2, f3, f4, n);
            return;
        }
        f5 = Math.min(f5, Math.min(f3, f4) / 2.0f);
        float f6 = f3 - 2.0f * f5;
        float f7 = f4 - 2.0f * f5;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (f6 > 0.0f && f7 > 0.0f) {
            RenderUtil.drawFilledRect(poseStack, f + f5, f2 + f5, f6, f7, n);
        }
        if (f6 > 0.0f) {
            RenderUtil.drawFilledRect(poseStack, f + f5, f2, f6, f5, n);
            RenderUtil.drawFilledRect(poseStack, f + f5, f2 + f4 - f5, f6, f5, n);
        }
        if (f7 > 0.0f) {
            RenderUtil.drawFilledRect(poseStack, f, f2 + f5, f5, f7, n);
            RenderUtil.drawFilledRect(poseStack, f + f3 - f5, f2 + f5, f5, f7, n);
        }
        if (bl) {
            RenderUtil.pushScissor((int)f, (int)f2, (int)f5, (int)f5);
            RenderUtil.drawRoundedRect(poseStack, f, f2, f5 * 2.0f, f5 * 2.0f, f5, n);
            RenderUtil.popScissor();
        } else {
            RenderUtil.drawFilledRect(poseStack, f, f2, f5, f5, n);
        }
        if (bl2) {
            RenderUtil.pushScissor((int)(f + f3 - f5), (int)f2, (int)f5, (int)f5);
            RenderUtil.drawRoundedRect(poseStack, f + f3 - f5 * 2.0f, f2, f5 * 2.0f, f5 * 2.0f, f5, n);
            RenderUtil.popScissor();
        } else {
            RenderUtil.drawFilledRect(poseStack, f + f3 - f5, f2, f5, f5, n);
        }
        if (bl3) {
            RenderUtil.pushScissor((int)f, (int)(f2 + f4 - f5), (int)f5, (int)f5);
            RenderUtil.drawRoundedRect(poseStack, f, f2 + f4 - f5 * 2.0f, f5 * 2.0f, f5 * 2.0f, f5, n);
            RenderUtil.popScissor();
        } else {
            RenderUtil.drawFilledRect(poseStack, f, f2 + f4 - f5, f5, f5, n);
        }
        if (bl4) {
            RenderUtil.pushScissor((int)(f + f3 - f5), (int)(f2 + f4 - f5), (int)f5, (int)f5);
            RenderUtil.drawRoundedRect(poseStack, f + f3 - f5 * 2.0f, f2 + f4 - f5 * 2.0f, f5 * 2.0f, f5 * 2.0f, f5, n);
            RenderUtil.popScissor();
        } else {
            RenderUtil.drawFilledRect(poseStack, f + f3 - f5, f2 + f4 - f5, f5, f5, n);
        }
        RenderSystem.enableBlend();
    }

    public static void drawBlurredRect(PoseStack poseStack, float f, float f2, float f3, float f4, float f5, float f6, float f7, int n) {
        try {
            PoseStack poseStack2;
            boolean bl;
            if (blurFailed) {
                return;
            }
            if (blurShader == null) {
                blurShader = new ShaderProgram("blur", ShaderFormats.POSITION_UV_COLOR);
            }
            if (mainRenderTarget == null) {
                mainRenderTarget = mc.getMainRenderTarget();
            }
            TextureTarget textureTarget = textureTargetSupplier.get();
            if (textureTarget.width != RenderUtil.mainRenderTarget.width || textureTarget.height != RenderUtil.mainRenderTarget.height) {
                textureTarget.resize(RenderUtil.mainRenderTarget.width, RenderUtil.mainRenderTarget.height, Minecraft.ON_OSX);
            }
            Matrix4f matrix4f = poseStack.last().pose();
            int n2 = n == 0 ? ColorUtil.fromARGB(255, 255, 255, 255) : n;
            n2 = ColorUtil.withAlpha(n2, f7);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            float f8 = RenderUtil.mainRenderTarget.width * RenderUtil.mainRenderTarget.height;
            float f9 = f3 * f4;
            boolean bl2 = f9 / f8 >= 0.05f;
            long l = mc.level != null ? mc.level.getGameTime() : -1L;
            boolean bl3 = bl = !bl2 || l != -1L;
            if (bl) {
                textureTarget.bindWrite(false);
                poseStack2 = new PoseStack();
                RenderHelper.blitRenderTarget(mainRenderTarget, poseStack2, textureTarget.width, textureTarget.height);
                mainRenderTarget.bindWrite(false);
            }
            blurShader.use();
            GL20.glUniform1i(blurShader.getUniformLocation("Sampler0"), 0);
            GlStateManager._activeTexture(33984);
            GlStateManager._bindTexture(textureTarget.getColorTextureId());
            GL20.glUniform2f(blurShader.getUniformLocation("Size"), f3, f4);
            GL20.glUniform4f(blurShader.getUniformLocation("Radius"), f5, f5, f5, f5);
            GL20.glUniform1f(blurShader.getUniformLocation("Smoothness"), 1.0f);
            GL20.glUniform1f(blurShader.getUniformLocation("BlurRadius"), f6);
            GL20.glUniform1f(blurShader.getUniformLocation("Opacity"), f7);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.vertex(matrix4f, f, f2, 0.0f).uv(0.0f, 1.0f).color(n2).endVertex();
            bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).uv(0.0f, 0.0f).color(n2).endVertex();
            bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).uv(1.0f, 0.0f).color(n2).endVertex();
            bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).uv(1.0f, 1.0f).color(n2).endVertex();
            BufferUploader.draw(bufferBuilder.end());
            GlStateManager._bindTexture(0);
            blurShader.stopUsing();
            RenderSystem.disableBlend();
        } catch (Exception exception) {
            logger.error("Error while rendering blur", exception);
            blurFailed = true;
        }
    }

    public static void pushScissor(int n, int n2, int n3, int n4) {
        int n5 = (int)mc.getWindow().getGuiScale();
        int n6 = n * n5;
        int n7 = mc.getWindow().getHeight() - (n2 + n4) * n5;
        int n8 = n3 * n5;
        int n9 = n4 * n5;
        int[] nArray = new int[4];
        if (!scissorStack.isEmpty()) {
            nArray = scissorStack.peek();
        } else {
            nArray[0] = 0;
            nArray[1] = 0;
            nArray[2] = mc.getWindow().getWidth();
            nArray[3] = mc.getWindow().getHeight();
        }
        int n10 = Math.max(n6, nArray[0]);
        int n11 = Math.max(n7, nArray[1]);
        int n12 = Math.min(n6 + n8, nArray[0] + nArray[2]) - n10;
        int n13 = Math.min(n7 + n9, nArray[1] + nArray[3]) - n11;
        int[] nArray2 = new int[]{n10, n11, n12, n13};
        scissorStack.push(nArray2);
        RenderSystem.enableScissor(nArray2[0], nArray2[1], nArray2[2], nArray2[3]);
    }

    public static void popScissor() {
        if (scissorStack.isEmpty()) {
            RenderSystem.disableScissor();
            return;
        }
        scissorStack.pop();
        if (scissorStack.isEmpty()) {
            RenderSystem.disableScissor();
        } else {
            int[] nArray = scissorStack.peek();
            RenderSystem.enableScissor(nArray[0], nArray[1], nArray[2], nArray[3]);
        }
    }

    public static void drawTexturedRect(PoseStack poseStack, int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9, int n10) {
        float f = (float)n5 / (float)n9;
        float f2 = (float)(n5 + n7) / (float)n9;
        float f3 = (float)n6 / (float)n10;
        float f4 = (float)(n6 + n8) / (float)n10;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        Matrix4f matrix4f = poseStack.last().pose();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f, (float)n, (float)(n2 + n4), 0.0f).uv(f, f4).endVertex();
        bufferBuilder.vertex(matrix4f, (float)(n + n3), (float)(n2 + n4), 0.0f).uv(f2, f4).endVertex();
        bufferBuilder.vertex(matrix4f, (float)(n + n3), (float)n2, 0.0f).uv(f2, f3).endVertex();
        bufferBuilder.vertex(matrix4f, (float)n, (float)n2, 0.0f).uv(f, f3).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static void drawFilledRect(PoseStack poseStack, float f, float f2, float f3, float f4, int n) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderHelper.setShaderColor(n);
        RenderUtil.drawFilledRect(poseStack, f, f2, f3, f4);
        RenderSystem.disableBlend();
        RenderHelper.resetShaderColor();
    }

    public static void drawFilledRect(PoseStack poseStack, float f, float f2, float f3, float f4) {
        Matrix4f matrix4f = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(matrix4f, f, f2, zLevel).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2 + f4, zLevel).endVertex();
        bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, zLevel).endVertex();
        bufferBuilder.vertex(matrix4f, f + f3, f2, zLevel).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static void drawSolidBox(AABB aABB, PoseStack poseStack) {
        Matrix4f matrix4f = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.minZ).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static void drawOutlineBox(AABB aABB, PoseStack poseStack) {
        Matrix4f matrix4f = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.minZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.maxZ).endVertex();
        bufferBuilder.vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.minZ).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static boolean isHovered(float f, float f2, float f3, float f4, int n, int n2) {
        return (float)n >= f && (float)n2 >= f2 && (float)n < f + f3 && (float)n2 < f2 + f4;
    }

    public static void drawQuad(BufferBuilder bufferBuilder, Matrix4f matrix4f, float f, float f2, float f3, float f4, Color color) {
        float f5 = (float)color.getRed() / 255.0f;
        float f6 = (float)color.getGreen() / 255.0f;
        float f7 = (float)color.getBlue() / 255.0f;
        float f8 = (float)color.getAlpha() / 255.0f;
        bufferBuilder.vertex(matrix4f, f, f4, 0.0f).color(f5, f6, f7, f8).endVertex();
        bufferBuilder.vertex(matrix4f, f3, f4, 0.0f).color(f5, f6, f7, f8).endVertex();
        bufferBuilder.vertex(matrix4f, f3, f2, 0.0f).color(f5, f6, f7, f8).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(f5, f6, f7, f8).endVertex();
    }

    public static void drawSpiralEffect(PoseStack poseStack, Entity entity, float f) {
        double d;
        double d2;
        double d3;
        double d4;
        double d5;
        double d6;
        int n;
        if (mc == null || mc.gameRenderer == null || mc.gameRenderer.getMainCamera() == null) {
            return;
        }
        double d7 = 2000.0;
        double d8 = (double)System.currentTimeMillis() % d7;
        boolean bl = d8 > d7 / 2.0;
        double d9 = d8 / (d7 / 2.0);
        d9 = bl ? (d9 -= 1.0) : 1.0 - d9;
        d9 = d9 < 0.5 ? 2.0 * d9 * d9 : 1.0 - Math.pow(-2.0 * d9 + 2.0, 2.0) / 2.0;
        Vec3 vec3 = EntityUtil.getInterpolatedPos(entity, f);
        double d10 = vec3.x();
        double d11 = vec3.y();
        double d12 = vec3.z();
        float f2 = entity.getBbHeight();
        double d13 = (double)entity.getBbWidth() * 0.975;
        double d14 = (double)f2 * d9;
        double d15 = (double)f2 / 1.2 * (d9 > 0.5 ? 1.0 - d9 : d9) * (double)(bl ? -1 : 1);
        double d16 = d11 + d14;
        double d17 = d16 + d15;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        Matrix4f matrix4f = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        int n2 = 60;
        for (int i = 0; i <= n2; ++i) {
            Color color = ColorUtil.getRainbowColor(10, i * 5);
            int n3 = color.getRGB();
            n = ColorUtil.withAlphaColor(color, 0.004f).getRGB();
            d6 = (double)i / (double)n2 * Math.PI * 2.0;
            d5 = Math.cos(d6) * d13;
            d4 = Math.sin(d6) * d13;
            d3 = d10 + d5;
            d2 = d12 + d4;
            d = d16;
            double d18 = d17;
            bufferBuilder.vertex(matrix4f, (float)d3, (float)d, (float)d2).color(n3).endVertex();
            bufferBuilder.vertex(matrix4f, (float)d3, (float)d18, (float)d2).color(n).endVertex();
        }
        BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
        if (renderedBuffer != null) {
            BufferUploader.drawWithShader(renderedBuffer);
        }
        bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.lineWidth(1.0f);
        for (int i = 0; i <= n2; ++i) {
            Color color = ColorUtil.getRainbowColor(10, i * 5);
            n = ColorUtil.withAlphaColor(color, 0.004f).getRGB();
            d6 = (double)i / (double)n2 * Math.PI * 2.0;
            d5 = Math.cos(d6) * d13;
            d4 = Math.sin(d6) * d13;
            d3 = d10 + d5;
            d2 = d12 + d4;
            d = d16;
            bufferBuilder.vertex(matrix4f, (float)d3, (float)d, (float)d2).color(n).endVertex();
        }
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void drawColoredBox(AABB aABB, PoseStack poseStack, Color color, Color color2) {
        Matrix4f matrix4f = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(1.5f);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        float f = (float)aABB.minX;
        float f2 = (float)aABB.minY;
        float f3 = (float)aABB.minZ;
        float f4 = (float)aABB.maxX;
        float f5 = (float)aABB.maxY;
        float f6 = (float)aABB.maxZ;
        bufferBuilder.vertex(matrix4f, f, f2, f3).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f3).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f3).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f6).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f6).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, f6).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, f6).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, f3).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f3).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f3).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f3).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f6).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f6).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f6).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f6).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f3).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, f3).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f3).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f3).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f3).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f6).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f6).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, f6).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f6).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();
    }

    public static void drawFilledColoredBox(AABB aABB, PoseStack poseStack, Color color, Color color2) {
        Matrix4f matrix4f = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        float f = (float)aABB.minX;
        float f2 = (float)aABB.minY;
        float f3 = (float)aABB.minZ;
        float f4 = (float)aABB.maxX;
        float f5 = (float)aABB.maxY;
        float f6 = (float)aABB.maxZ;
        int n = color.getRed();
        int n2 = color.getGreen();
        int n3 = color.getBlue();
        int n4 = color.getAlpha();
        int n5 = color2.getRed();
        int n6 = color2.getGreen();
        int n7 = color2.getBlue();
        int n8 = color2.getAlpha();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2, f6).color(n5, n6, n7, n8).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f6).color(n5, n6, n7, n8).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f6).color(n, n2, n3, n4).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f6).color(n, n2, n3, n4).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, f3).color(n5, n6, n7, n8).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f3).color(n, n2, n3, n4).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f3).color(n, n2, n3, n4).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f3).color(n5, n6, n7, n8).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, f3).color(n5, n6, n7, n8).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, f6).color(n5, n6, n7, n8).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f6).color(n, n2, n3, n4).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f3).color(n, n2, n3, n4).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f3).color(n5, n6, n7, n8).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f3).color(n, n2, n3, n4).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f6).color(n, n2, n3, n4).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f6).color(n5, n6, n7, n8).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f3).color(n, n2, n3, n4).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f6).color(n, n2, n3, n4).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f6).color(n, n2, n3, n4).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f3).color(n, n2, n3, n4).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, f3).color(n5, n6, n7, n8).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f3).color(n5, n6, n7, n8).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f6).color(n5, n6, n7, n8).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, f6).color(n5, n6, n7, n8).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void drawBoxVerts(BufferBuilder bufferBuilder, Matrix4f matrix4f, AABB aABB) {
        float f = (float)(aABB.minX - mc.getEntityRenderDispatcher().camera.getPosition().x());
        float f2 = (float)(aABB.minY - mc.getEntityRenderDispatcher().camera.getPosition().y());
        float f3 = (float)(aABB.minZ - mc.getEntityRenderDispatcher().camera.getPosition().z());
        float f4 = (float)(aABB.maxX - mc.getEntityRenderDispatcher().camera.getPosition().x());
        float f5 = (float)(aABB.maxY - mc.getEntityRenderDispatcher().camera.getPosition().y());
        float f6 = (float)(aABB.maxZ - mc.getEntityRenderDispatcher().camera.getPosition().z());
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(matrix4f, f, f2, f3).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f3).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f3).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f3).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, f3).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f3).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f3).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f3).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f3).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f3).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f2, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f4, f5, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, f3).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f6).endVertex();
        bufferBuilder.vertex(matrix4f, f, f5, f3).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static void enableBlend() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    public static void disableBlend() {
        RenderSystem.disableBlend();
    }

    public static void drawTexture(ResourceLocation resourceLocation, PoseStack poseStack, float f, float f2, float f3, float f4, float f5, int n) {
        RenderUtil.drawTexture(mc.getTextureManager().getTexture(resourceLocation).getId(), poseStack, f, f2, f3, f4, f5, n);
    }

    public static void drawTexture(int n, PoseStack poseStack, float f, float f2, float f3, float f4, float f5, int n2) {
        Matrix4f matrix4f = poseStack.last().pose();
        RenderHelper.setShaderColor(ColorUtil.withAlpha(n2, f5));
        if (n != -1) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, n);
        }
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).uv(0.0f, 0.0f).endVertex();
        bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).uv(0.0f, 1.0f).endVertex();
        bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).uv(1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).uv(1.0f, 0.0f).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderHelper.resetShaderColor();
    }

    public static void drawShadow(PoseStack poseStack, float f, float f2, float f3, float f4, int n, int n2) {
        int n3;
        f -= (float)n;
        f2 -= (float)n;
        if (!shadowCache.containsKey(n3 = (int)((f3 += (float)(n * 2)) * (f4 += (float)(n * 2)) + f3 * (float)n))) {
            BufferedImage bufferedImage = new BufferedImage((int)f3, (int)f4, 2);
            Graphics graphics = bufferedImage.getGraphics();
            graphics.setColor(new Color(-1));
            graphics.fillRect(n, n, (int)(f3 - (float)(n * 2)), (int)(f4 - (float)(n * 2)));
            graphics.dispose();
            GaussianBlur gaussianBlur = new GaussianBlur(n);
            BufferedImage bufferedImage2 = gaussianBlur.filter(bufferedImage, null);
            shadowCache.put(n3, new RenderUtil.ShadowTexture(bufferedImage2));
            return;
        }
        shadowCache.get(n3).bind();
        RenderUtil.enableBlend();
        RenderUtil.drawTexture(-1, poseStack, f, f2, f3, f4, (float)ColorUtil.getAlpha(n2) / 255.0f, n2);
        RenderUtil.disableBlend();
    }

    public static void registerTexture(ResourceLocationWrapper resourceLocationWrapper, BufferedImage bufferedImage) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write((RenderedImage)bufferedImage, (String)"png", (OutputStream)byteArrayOutputStream);
            byte[] byArray = byteArrayOutputStream.toByteArray();
            RenderUtil.registerTextureBytes(resourceLocationWrapper, byArray);
        } catch (Exception exception) {
            // empty catch block
        }
    }

    private static void registerTextureBytes(ResourceLocationWrapper resourceLocationWrapper, byte[] byArray) {
        try {
            ByteBuffer byteBuffer = BufferUtils.createByteBuffer(byArray.length).put(byArray);
            byteBuffer.flip();
            DynamicTexture dynamicTexture = new DynamicTexture(NativeImage.read(byteBuffer));
            mc.execute(() -> mc.getTextureManager().register(resourceLocationWrapper.get(), dynamicTexture));
        } catch (Exception exception) {
            // empty catch block
        }
    }

    public static int lerpColorHSB(int n, int n2, float f) {
        float[] fArray = new float[3];
        float[] fArray2 = new float[3];
        Color.RGBtoHSB(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, fArray);
        Color.RGBtoHSB(n2 >> 16 & 0xFF, n2 >> 8 & 0xFF, n2 & 0xFF, fArray2);
        float f2 = fArray[0] + (fArray2[0] - fArray[0]) * f;
        float f3 = fArray[1] + (fArray2[1] - fArray[1]) * f;
        float f4 = fArray[2] + (fArray2[2] - fArray[2]) * f;
        int n3 = (int)((float)(n >> 24 & 0xFF) + (float)((n2 >> 24 & 0xFF) - (n >> 24 & 0xFF)) * f);
        return n3 << 24 | Color.HSBtoRGB(f2, f3, f4) & 0xFFFFFF;
    }

    @Generated
    private RenderUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static void setZLevel(float f) {
        zLevel = f;
    }

    static {
        textureTargetSupplier = Suppliers.memoize(() -> new TextureTarget(1920, 1024, false, Minecraft.ON_OSX));
        mainRenderTarget = mc.getMainRenderTarget();
        new Timer();
        blurFailed = false;
        zLevel = 0.0f;
    }
}