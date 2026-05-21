package shit.zen.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class DrawContext {

    public static final class StrokeState {
        private final Paint.LinearGradient gradient;
        private float dashOffset;
        private boolean inDash;

        StrokeState(Paint.LinearGradient gradient) {
            this.gradient = gradient;
            this.inDash = true;
            this.dashOffset = 0.0f;
            if (gradient != null) {
                this.dashOffset = gradient.angle;
            }
        }

        boolean isDrawing() {
            return this.gradient == null || this.inDash;
        }

        void advance(float length) {
            if (this.gradient == null || this.gradient.colors == null || this.gradient.colors.length == 0) {
                return;
            }
            this.dashOffset += length;
            while (this.dashOffset >= this.currentDashLength()) {
                this.dashOffset -= this.currentDashLength();
                this.inDash = !this.inDash;
            }
        }

        private float currentDashLength() {
            int index = this.inDash ? 0 : (this.gradient.colors.length > 1 ? 1 : 0);
            return Math.max(0.001f, this.gradient.colors[index]);
        }
    }

    private static final RoundedRectShader ROUNDED_RECT_SHADER = new RoundedRectShader();
    private final PoseStack poseStack;
    private final GuiGraphics guiGraphics;
    private final Deque<Boolean> clipStack = new ArrayDeque<>();

    public static RoundedRectShader getRoundedRectShader() {
        return ROUNDED_RECT_SHADER;
    }

    public DrawContext(GuiGraphics guiGraphics) {
        this.guiGraphics = guiGraphics;
        this.poseStack = new PoseStack();
    }

    public DrawContext(GuiGraphics guiGraphics, PoseStack poseStack) {
        this.guiGraphics = guiGraphics;
        this.poseStack = poseStack != null ? poseStack : new PoseStack();
    }

    public PoseStack getPoseStack() {
        return this.poseStack;
    }

    public GuiGraphics getGuiGraphics() {
        return this.guiGraphics;
    }

    public void save() {
        this.poseStack.pushPose();
        this.clipStack.push(Boolean.FALSE);
    }

    public void restore() {
        boolean bl;
        if (!this.clipStack.isEmpty() && (bl = this.clipStack.pop()) && this.guiGraphics != null) {
            this.guiGraphics.disableScissor();
        }
        this.poseStack.popPose();
    }

    public void translate(float f, float f2) {
        this.poseStack.translate(f, f2, 0.0f);
    }

    public void scale(float f, float f2) {
        this.poseStack.scale(f, f2, 1.0f);
    }

    public void rotate(float f) {
        this.poseStack.mulPose(Axis.ZP.rotationDegrees(f));
    }

    public void flush() {
    }

    public void clip(Rectangle rectangle) {
        this.clipRect(rectangle, true);
    }

    public void clipRect(Rectangle rectangle, boolean bl) {
        if (this.guiGraphics == null) {
            return;
        }
        Matrix4f matrix4f = this.poseStack.last().pose();
        Vector4f vector4f = new Vector4f(rectangle.getX(), rectangle.getY(), 0.0f, 1.0f).mul(matrix4f);
        Vector4f vector4f2 = new Vector4f(rectangle.getRight(), rectangle.getBottom(), 0.0f, 1.0f).mul(matrix4f);
        int n = (int)Math.floor(Math.min(vector4f.x, vector4f2.x));
        int n2 = (int)Math.floor(Math.min(vector4f.y, vector4f2.y));
        int n3 = (int)Math.ceil(Math.max(vector4f.x, vector4f2.x));
        int n4 = (int)Math.ceil(Math.max(vector4f.y, vector4f2.y));
        this.guiGraphics.enableScissor(n, n2, n3, n4);
        this.updateClipStack();
    }

    public void clipRoundedRect(RoundedRectangle roundedRectangle, boolean bl) {
        if (this.guiGraphics == null) {
            return;
        }
        Matrix4f matrix4f = this.poseStack.last().pose();
        Vector4f vector4f = new Vector4f(roundedRectangle.x1, roundedRectangle.y1, 0.0f, 1.0f).mul(matrix4f);
        Vector4f vector4f2 = new Vector4f(roundedRectangle.x2, roundedRectangle.y2, 0.0f, 1.0f).mul(matrix4f);
        int n = (int)Math.floor(Math.min(vector4f.x, vector4f2.x));
        int n2 = (int)Math.floor(Math.min(vector4f.y, vector4f2.y));
        int n3 = (int)Math.ceil(Math.max(vector4f.x, vector4f2.x));
        int n4 = (int)Math.ceil(Math.max(vector4f.y, vector4f2.y));
        this.guiGraphics.enableScissor(n, n2, n3, n4);
        this.updateClipStack();
    }

    private void updateClipStack() {
        if (!this.clipStack.isEmpty()) {
            this.clipStack.pop();
            this.clipStack.push(Boolean.TRUE);
        }
    }

    public void drawRect(Rectangle rectangle, Paint paint) {
        this.drawRectXYWH(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight(), paint);
    }

    public void drawRectXYWH(float f, float f2, float f3, float f4, Paint paint) {
        if (paint.getCapStyle() == Paint.StrokeCap.STROKE) {
            this.drawRectStroke(f, f2, f3, f4, paint);
            return;
        }
        this.setupColorShader();
        Matrix4f matrix4f = this.poseStack.last().pose();
        float[] fArray = DrawContext.colorToFloats(paint.getColor());
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        if (paint.getGradCoords() != null) {
            boolean bl;
            Paint.GradientCoords paint$GradientCoords = paint.getGradCoords();
            float[] fArray2 = DrawContext.colorToFloats(paint$GradientCoords.color1);
            float[] fArray3 = DrawContext.colorToFloats(paint$GradientCoords.color2);
            boolean bl2 = bl = Math.abs(paint$GradientCoords.y2 - paint$GradientCoords.y1) >= Math.abs(paint$GradientCoords.x2 - paint$GradientCoords.x1);
            if (bl) {
                bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(fArray2[0], fArray2[1], fArray2[2], fArray2[3]).endVertex();
                bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).color(fArray3[0], fArray3[1], fArray3[2], fArray3[3]).endVertex();
                bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).color(fArray3[0], fArray3[1], fArray3[2], fArray3[3]).endVertex();
                bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).color(fArray2[0], fArray2[1], fArray2[2], fArray2[3]).endVertex();
            } else {
                bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(fArray2[0], fArray2[1], fArray2[2], fArray2[3]).endVertex();
                bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).color(fArray2[0], fArray2[1], fArray2[2], fArray2[3]).endVertex();
                bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).color(fArray3[0], fArray3[1], fArray3[2], fArray3[3]).endVertex();
                bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).color(fArray3[0], fArray3[1], fArray3[2], fArray3[3]).endVertex();
            }
        } else {
            bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
            bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
            bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
            bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
        }
        tesselator.end();
    }

    public void drawRoundedRect(RoundedRectangle roundedRectangle, Paint paint) {
        int n;
        int n2 = n = paint.getColor();
        boolean bl = false;
        Paint.GradientCoords paint$GradientCoords = paint.getGradCoords();
        if (paint$GradientCoords != null) {
            n = paint$GradientCoords.color1;
            n2 = paint$GradientCoords.color2;
            bl = true;
        }
        float f = paint.getCapStyle() == Paint.StrokeCap.STROKE ? Math.max(0.5f, paint.getStrokeWidth()) : 0.0f;
        ROUNDED_RECT_SHADER.draw(this.poseStack.last().pose(), roundedRectangle.x1, roundedRectangle.y1, roundedRectangle.x2, roundedRectangle.y2, roundedRectangle.topLeftRadius, roundedRectangle.topRightRadius, roundedRectangle.bottomRightRadius, roundedRectangle.bottomLeftRadius, n, n2, bl, f);
    }

    private void drawCornerArc(BufferBuilder bufferBuilder, Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, int n, Paint paint, RoundedRectangle roundedRectangle) {
        int n2 = paint.getColor();
        Paint.GradientCoords paint$GradientCoords = paint.getGradCoords();
        if (f3 <= 0.0f) {
            float[] fArray = DrawContext.interpolateGradientColor(paint$GradientCoords, f, f2, roundedRectangle, n2);
            bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
            return;
        }
        for (int i = 0; i <= n; ++i) {
            float f6 = (float)i / (float)n;
            double d = Math.toRadians(f4 + (f5 - f4) * f6);
            float f7 = f + (float)Math.cos(d) * f3;
            float f8 = f2 + (float)Math.sin(d) * f3;
            float[] fArray = DrawContext.interpolateGradientColor(paint$GradientCoords, f7, f8, roundedRectangle, n2);
            bufferBuilder.vertex(matrix4f, f7, f8, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
        }
    }

    private static float[] interpolateGradientColor(Paint.GradientCoords paint$GradientCoords, float f, float f2, RoundedRectangle roundedRectangle, int n) {
        if (paint$GradientCoords == null) {
            return DrawContext.colorToFloats(n);
        }
        float f3 = roundedRectangle.getHeight();
        if (f3 <= 0.0f) {
            return DrawContext.colorToFloats(paint$GradientCoords.color1);
        }
        float f4 = Math.max(0.0f, Math.min(1.0f, (f2 - roundedRectangle.y1) / f3));
        float[] fArray = DrawContext.colorToFloats(paint$GradientCoords.color1);
        float[] fArray2 = DrawContext.colorToFloats(paint$GradientCoords.color2);
        return new float[]{fArray[0] + (fArray2[0] - fArray[0]) * f4, fArray[1] + (fArray2[1] - fArray[1]) * f4, fArray[2] + (fArray2[2] - fArray[2]) * f4, fArray[3] + (fArray2[3] - fArray[3]) * f4};
    }

    private void drawRectStroke(float f, float f2, float f3, float f4, Paint paint) {
        float f5 = Math.max(0.5f, paint.getStrokeWidth());
        float f6 = f5 * 0.5f;
        this.drawRectXYWH(f - f6, f2 - f6, f3 + f5, f5, paint.copy());
        this.drawRectXYWH(f - f6, f2 + f4 - f6, f3 + f5, f5, paint.copy());
        this.drawRectXYWH(f - f6, f2 - f6, f5, f4 + f5, paint.copy());
        this.drawRectXYWH(f + f3 - f6, f2 - f6, f5, f4 + f5, paint.copy());
    }

    private void drawRoundedRectStroke(RoundedRectangle roundedRectangle, Paint paint) {
        Paint paint2 = paint.copy();
        this.drawRectStroke(roundedRectangle.x1, roundedRectangle.y1, roundedRectangle.getWidth(), roundedRectangle.getHeight(), paint);
    }

    public void drawLine(float f, float f2, float f3, float f4, Paint paint) {
        float f5 = Math.max(0.5f, paint.getStrokeWidth());
        float f6 = f3 - f;
        float f7 = f4 - f2;
        float f8 = (float)Math.hypot(f6, f7);
        if (f8 < 1.0E-4f) {
            return;
        }
        float f9 = -f7 / f8 * f5 * 0.5f;
        float f10 = f6 / f8 * f5 * 0.5f;
        this.setupColorShader();
        Matrix4f matrix4f = this.poseStack.last().pose();
        float[] fArray = DrawContext.colorToFloats(paint.getColor());
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, f + f9, f2 + f10, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
        bufferBuilder.vertex(matrix4f, f - f9, f2 - f10, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
        bufferBuilder.vertex(matrix4f, f3 - f9, f4 - f10, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
        bufferBuilder.vertex(matrix4f, f3 + f9, f4 + f10, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
        tesselator.end();
    }

    public void drawString(String string, float f, float f2, FontRenderer fontRenderer, Paint paint) {
        if (string == null || string.isEmpty()) {
            return;
        }
        CustomFont customFont = fontRenderer.getFont();
        if (customFont == null) {
            return;
        }
        GlyphMetrics glyphMetrics = fontRenderer.getMetrics();
        float f3 = f2 + glyphMetrics.ascent();
        customFont.drawString(this.poseStack, string, f, f3, paint.getColor());
    }

    public void drawArc(float f, float f2, float f3, float f4, float f5, float f6, boolean bl, Paint paint) {
        this.setupColorShader();
        Matrix4f matrix4f = this.poseStack.last().pose();
        float f7 = (f + f3) * 0.5f;
        float f8 = (f2 + f4) * 0.5f;
        float f9 = Math.min(f3 - f, f4 - f2) * 0.5f;
        float[] fArray = DrawContext.colorToFloats(paint.getColor());
        int n = 32;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        if (paint.getCapStyle() == Paint.StrokeCap.STROKE) {
            float f10 = Math.max(0.5f, paint.getStrokeWidth());
            float f11 = f9 - f10 * 0.5f;
            float f12 = f9 + f10 * 0.5f;
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i <= n; ++i) {
                float f13 = (float)i / (float)n;
                double d = Math.toRadians(f5 + f6 * f13);
                float f14 = (float)Math.cos(d);
                float f15 = (float)Math.sin(d);
                bufferBuilder.vertex(matrix4f, f7 + f14 * f12, f8 + f15 * f12, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
                bufferBuilder.vertex(matrix4f, f7 + f14 * f11, f8 + f15 * f11, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
            }
        } else {
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.vertex(matrix4f, f7, f8, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
            for (int i = 0; i <= n; ++i) {
                float f16 = (float)i / (float)n;
                double d = Math.toRadians(f5 + f6 * f16);
                float f17 = f7 + (float)Math.cos(d) * f9;
                float f18 = f8 + (float)Math.sin(d) * f9;
                bufferBuilder.vertex(matrix4f, f17, f18, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
            }
        }
        tesselator.end();
    }

    public void drawPath(Path path, Paint paint) {
        if (path == null) {
            return;
        }
        float f = Math.max(0.5f, paint.getStrokeWidth());
        float[] fArray = DrawContext.colorToFloats(paint.getColor());
        DrawContext.StrokeState drawContext$StrokeState = new DrawContext.StrokeState(paint.getLinGradient());
        Matrix4f matrix4f = this.poseStack.last().pose();
        float f2 = 0.0f;
        float f3 = 0.0f;
        float f4 = 0.0f;
        float f5 = 0.0f;
        boolean bl = false;
        if (paint.getCapStyle() == Paint.StrokeCap.STROKE || paint.getCapStyle() == Paint.StrokeCap.STROKE_AND_FILL) {
            for (Path.PathSegment path$PathSegment : path.getSegments()) {
                switch (path$PathSegment.type) {
                    case MOVE_TO: {
                        f2 = path$PathSegment.coords[0];
                        f3 = path$PathSegment.coords[1];
                        f4 = f2;
                        f5 = f3;
                        bl = true;
                        break;
                    }
                    case LINE_TO: {
                        if (bl) {
                            this.drawStrokedLineSegment(matrix4f, f2, f3, path$PathSegment.coords[0], path$PathSegment.coords[1], f, fArray, drawContext$StrokeState);
                        }
                        f2 = path$PathSegment.coords[0];
                        f3 = path$PathSegment.coords[1];
                        bl = true;
                        break;
                    }
                    case QUAD_TO: {
                        if (bl) {
                            this.drawStrokedCubicBezier(matrix4f, f2, f3, (f2 + 2.0f * path$PathSegment.coords[0]) / 3.0f, (f3 + 2.0f * path$PathSegment.coords[1]) / 3.0f, (path$PathSegment.coords[2] + 2.0f * path$PathSegment.coords[0]) / 3.0f, (path$PathSegment.coords[3] + 2.0f * path$PathSegment.coords[1]) / 3.0f, path$PathSegment.coords[2], path$PathSegment.coords[3], f, fArray, drawContext$StrokeState);
                        }
                        f2 = path$PathSegment.coords[2];
                        f3 = path$PathSegment.coords[3];
                        break;
                    }
                    case CUBIC_TO: {
                        if (bl) {
                            this.drawStrokedCubicBezier(matrix4f, f2, f3, path$PathSegment.coords[0], path$PathSegment.coords[1], path$PathSegment.coords[2], path$PathSegment.coords[3], path$PathSegment.coords[4], path$PathSegment.coords[5], f, fArray, drawContext$StrokeState);
                        }
                        f2 = path$PathSegment.coords[4];
                        f3 = path$PathSegment.coords[5];
                        break;
                    }
                    case CLOSE: {
                        if (bl) {
                            this.drawStrokedLineSegment(matrix4f, f2, f3, f4, f5, f, fArray, drawContext$StrokeState);
                        }
                        f2 = f4;
                        f3 = f5;
                        break;
                    }
                    case RRECT: {
                        this.drawRoundedRectStroke(path$PathSegment.roundedRect, paint);
                        break;
                    }
                    case RECT: {
                        this.drawRectStroke(path$PathSegment.rect.getX(), path$PathSegment.rect.getY(), path$PathSegment.rect.getWidth(), path$PathSegment.rect.getHeight(), paint);
                    }
                }
            }
        }
        if (paint.getCapStyle() == Paint.StrokeCap.FILL || paint.getCapStyle() == Paint.StrokeCap.STROKE_AND_FILL) {
            this.fillPath(path, paint);
        }
    }

    private void fillPath(Path path, Paint paint) {
        ArrayList<float[]> arrayList = new ArrayList<>();
        float f = 0.0f;
        float f2 = 0.0f;
        float f3 = 0.0f;
        float f4 = 0.0f;
        block7: for (Path.PathSegment path$PathSegment : path.getSegments()) {
            switch (path$PathSegment.type) {
                case MOVE_TO: {
                    if (!arrayList.isEmpty()) {
                        this.fillPolygon((List<float[]>)arrayList, paint);
                        arrayList.clear();
                    }
                    f = path$PathSegment.coords[0];
                    f2 = path$PathSegment.coords[1];
                    f3 = f;
                    f4 = f2;
                    arrayList.add(new float[]{f, f2});
                    continue block7;
                }
                case LINE_TO: {
                    f = path$PathSegment.coords[0];
                    f2 = path$PathSegment.coords[1];
                    arrayList.add(new float[]{f, f2});
                    continue block7;
                }
                case CLOSE: {
                    if (!arrayList.isEmpty()) {
                        this.fillPolygon((List<float[]>)arrayList, paint);
                        arrayList.clear();
                    }
                    f = f3;
                    f2 = f4;
                    continue block7;
                }
                case RRECT: {
                    this.drawRoundedRect(path$PathSegment.roundedRect, paint);
                    continue block7;
                }
                case RECT: {
                    this.drawRect(path$PathSegment.rect, paint);
                    continue block7;
                }
            }
        }
        if (!arrayList.isEmpty()) {
            this.fillPolygon((List<float[]>)arrayList, paint);
        }
    }

    private void fillPolygon(List<float[]> list, Paint paint) {
        if (list.size() < 3) {
            return;
        }
        this.setupColorShader();
        Matrix4f matrix4f = this.poseStack.last().pose();
        float[] fArray = DrawContext.colorToFloats(paint.getColor());
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        for (float[] fArray2 : list) {
            bufferBuilder.vertex(matrix4f, fArray2[0], fArray2[1], 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
        }
        tesselator.end();
    }

    private void drawStrokedCubicBezier(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float[] fArray, DrawContext.StrokeState drawContext$StrokeState) {
        int n = 24;
        float f10 = f;
        float f11 = f2;
        for (int i = 1; i <= n; ++i) {
            float f12 = (float)i / (float)n;
            float f13 = 1.0f - f12;
            float f14 = f13 * f13 * f13 * f + 3.0f * f13 * f13 * f12 * f3 + 3.0f * f13 * f12 * f12 * f5 + f12 * f12 * f12 * f7;
            float f15 = f13 * f13 * f13 * f2 + 3.0f * f13 * f13 * f12 * f4 + 3.0f * f13 * f12 * f12 * f6 + f12 * f12 * f12 * f8;
            this.drawStrokedLineSegment(matrix4f, f10, f11, f14, f15, f9, fArray, drawContext$StrokeState);
            f10 = f14;
            f11 = f15;
        }
    }

    private void drawStrokedLineSegment(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float[] fArray, DrawContext.StrokeState drawContext$StrokeState) {
        if (drawContext$StrokeState.isDrawing()) {
            this.drawLineSegment(matrix4f, f, f2, f3, f4, f5, fArray);
        }
        drawContext$StrokeState.advance((float)Math.hypot(f3 - f, f4 - f2));
    }

    private void drawLineSegment(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float[] fArray) {
        float f6 = f3 - f;
        float f7 = f4 - f2;
        float f8 = (float)Math.hypot(f6, f7);
        if (f8 < 1.0E-4f) {
            return;
        }
        float f9 = -f7 / f8 * f5 * 0.5f;
        float f10 = f6 / f8 * f5 * 0.5f;
        this.setupColorShader();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, f + f9, f2 + f10, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
        bufferBuilder.vertex(matrix4f, f - f9, f2 - f10, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
        bufferBuilder.vertex(matrix4f, f3 - f9, f4 - f10, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
        bufferBuilder.vertex(matrix4f, f3 + f9, f4 + f10, 0.0f).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
        tesselator.end();
    }

    public void drawTexture(Texture texture, Rectangle rectangle, Rectangle rectangle2, Paint paint) {
        if (texture == null) {
            return;
        }
        this.setupTexShader();
        if (texture.getResourceLocation() != null) {
            RenderSystem.setShaderTexture(0, texture.getResourceLocation());
        } else {
            RenderSystem.setShaderTexture(0, texture.getGlId());
        }
        Matrix4f matrix4f = this.poseStack.last().pose();
        float[] fArray = DrawContext.colorToFloats(paint.getColor());
        float f = rectangle.getX() / (float)texture.getWidth();
        float f2 = rectangle.getY() / (float)texture.getHeight();
        float f3 = rectangle.getRight() / (float)texture.getWidth();
        float f4 = rectangle.getBottom() / (float)texture.getHeight();
        float f5 = rectangle2.getX();
        float f6 = rectangle2.getY();
        float f7 = rectangle2.getWidth();
        float f8 = rectangle2.getHeight();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(matrix4f, f5, f6, 0.0f).uv(f, f2).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
        bufferBuilder.vertex(matrix4f, f5, f6 + f8, 0.0f).uv(f, f4).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
        bufferBuilder.vertex(matrix4f, f5 + f7, f6 + f8, 0.0f).uv(f3, f4).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
        bufferBuilder.vertex(matrix4f, f5 + f7, f6, 0.0f).uv(f3, f2).color(fArray[0], fArray[1], fArray[2], fArray[3]).endVertex();
        tesselator.end();
    }

    public void drawBlurredRoundedRect(RoundedRectangle roundedRectangle, float f, float f2, float f3, float f4, int n) {
        float f5 = roundedRectangle.x1 + f - f4;
        float f6 = roundedRectangle.y1 + f2 - f4;
        float f7 = roundedRectangle.getWidth() + f4 * 2.0f;
        float f8 = roundedRectangle.getHeight() + f4 * 2.0f;
        float f9 = Math.max(0.0f, roundedRectangle.topLeftRadius + f4);
        float f10 = Math.max(0.0f, roundedRectangle.topRightRadius + f4);
        float f11 = Math.max(0.0f, roundedRectangle.bottomRightRadius + f4);
        float f12 = Math.max(0.0f, roundedRectangle.bottomLeftRadius + f4);
        RoundedRectangle roundedRectangle2 = RoundedRectangle.ofXYWHRadii(f5, f6, f7, f8, new float[]{f9, f10, f11, f12});
        Paint paint = new Paint().setColor(n);
        float f13 = Math.max(0.01f, f3 * 0.5f);
        BlurRenderer.renderBlur(this, f5, f6, f7, f8, f13, () -> this.drawRoundedRect(roundedRectangle2, paint));
    }

    public void drawBlur(float f, float f2, float f3, float f4, float f5, Runnable runnable) {
        BlurRenderer.renderBlur(this, f, f2, f3, f4, f5, runnable);
    }

    void clearClipStack() {
        while (!this.clipStack.isEmpty()) {
            boolean bl = this.clipStack.pop();
            if (!bl || this.guiGraphics == null) continue;
            this.guiGraphics.disableScissor();
        }
    }

    private void setupColorShader() {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
    }

    private void setupTexShader() {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
    }

    static float[] colorToFloats(int n) {
        return new float[]{(float)(n >> 16 & 0xFF) / 255.0f, (float)(n >> 8 & 0xFF) / 255.0f, (float)(n & 0xFF) / 255.0f, (float)(n >>> 24 & 0xFF) / 255.0f};
    }

    public static AbstractTexture getTexture(ResourceLocation resourceLocation) {
        return Minecraft.getInstance().getTextureManager().getTexture(resourceLocation);
    }
}