package shit.zen.render;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import shit.zen.ClientBase;
import shit.zen.utils.render.ColorUtil;

public final class GlHelper {
    private static final Map<FontRenderer, Map<String, Float>> stringWidthCache = new HashMap<>();

    public static DrawContext getCanvas() {
        DrawContext drawContext = Renderer.getCanvas();
        if (drawContext == null) {
            throw new IllegalStateException("GlHelper.getCanvas() called outside a GlRenderer.render block");
        }
        return drawContext;
    }

    public static float drawText(String string, float f, float f2, FontRenderer fontRenderer, int n) {
        Paint paint = GlHelper.toPaint(n);
        return GlHelper.drawTextFormatted(string, f, f2, fontRenderer, paint, false);
    }

    public static int getFontAscent(FontRenderer fontRenderer) {
        GlyphMetrics glyphMetrics = fontRenderer.getMetrics();
        float f = (glyphMetrics.getLineGap() - glyphMetrics.ascent() - glyphMetrics.descent()) / 2.0f;
        return (int)Math.ceil(f);
    }

    public static Texture wrapTexture(AbstractTexture abstractTexture) {
        if (abstractTexture == null) {
            return null;
        }
        int n = abstractTexture.getId();
        int n2 = GL11.glGetTexLevelParameteri(3553, 0, 4096);
        int n3 = GL11.glGetTexLevelParameteri(3553, 0, 4097);
        if (n2 <= 0) {
            n2 = 64;
        }
        if (n3 <= 0) {
            n3 = 64;
        }
        return new Texture(n, n2, n3);
    }

    public static void drawPlayerHead(AbstractClientPlayer abstractClientPlayer, float f, float f2, float f3, float f4, float f5) {
        GlHelper.drawPlayerHeadRounded(abstractClientPlayer, f, f2, f3, f4, f5, 0.0f);
    }

    public static void drawPlayerHeadRounded(AbstractClientPlayer abstractClientPlayer, float f, float f2, float f3, float f4, float f5, float f6) {
        ResourceLocation resourceLocation = abstractClientPlayer.getSkinTextureLocation();
        if (resourceLocation == null || ClientBase.mc.getTextureManager().getTexture(resourceLocation) == null) {
            return;
        }
        AbstractTexture abstractTexture = ClientBase.mc.getTextureManager().getTexture(resourceLocation);
        int n = abstractTexture.getId();
        DrawContext drawContext = GlHelper.getCanvas();
        int n2 = (int)Math.max(0.0f, Math.min(255.0f, f5 * 255.0f)) << 24 | 0xFFFFFF;
        RoundedRectShader roundedRectShader = DrawContext.getRoundedRectShader();
        float f7 = 0.125f;
        float f8 = 0.125f;
        float f9 = 0.25f;
        float f10 = 0.25f;
        float f11 = 0.625f;
        float f12 = 0.125f;
        float f13 = 0.75f;
        float f14 = 0.25f;
        Matrix4f matrix4f = drawContext.getPoseStack().last().pose();
        roundedRectShader.drawTextured(matrix4f, f, f2, f + f3, f2 + f4, f6, f6, f6, f6, n2, n, f7, f8, f9, f10);
        roundedRectShader.drawTextured(matrix4f, f, f2, f + f3, f2 + f4, f6, f6, f6, f6, n2, n, f11, f12, f13, f14);
        if (abstractClientPlayer.hurtTime > 0) {
            int n3 = ColorUtil.withAlphaColor(new Color(255, 0, 0, abstractClientPlayer.hurtTime * 18), f5).getRGB();
            Paint paint = new Paint().setColor(n3);
            drawContext.drawRoundedRect(RoundedRectangle.ofXYWHR(f, f2, f3, f4, f6), paint);
        }
    }

    public static float drawTextFormatted(String string, float f, float f2, FontRenderer fontRenderer, Paint paint, boolean bl) {
        if (string == null || string.isEmpty()) {
            return f;
        }
        DrawContext drawContext = GlHelper.getCanvas();
        int n = paint.getColor();
        float f3 = f;
        float f4 = f2 + (float)GlHelper.getFontAscent(fontRenderer);
        String[] stringArray = string.split("§");
        for (int i = 0; i < stringArray.length; ++i) {
            char c;
            ChatFormatting chatFormatting;
            String string2 = stringArray[i];
            if (i > 0 && !string2.isEmpty() && (chatFormatting = ChatFormatting.getByCode(c = string2.charAt(0))) != null) {
                if (!bl && chatFormatting.getColor() != null) {
                    n = ColorUtil.withAlpha(chatFormatting.getColor(), (float)(n >> 24 & 0xFF) / 255.0f);
                }
                string2 = string2.substring(1);
            }
            drawContext.drawString(string2, f3, f4, fontRenderer, paint.setColor(n));
            f3 += GlHelper.getStringWidth(string2, fontRenderer);
        }
        return f3;
    }

    public static float drawTextWithShadow(String string, float f, float f2, FontRenderer fontRenderer, Paint paint) {
        int n = paint.getColor();
        GlHelper.drawTextFormatted(string, f + 0.5f, f2 + 0.5f, fontRenderer, paint.setColor(ColorUtil.fromARGB(0, 0, 0, (int)((float)ColorUtil.getAlpha(paint.getColor()) * 0.65f * 255.0f))), true);
        paint.setColor(n);
        return GlHelper.drawTextFormatted(string, f, f2, fontRenderer, paint, false);
    }

    public static float drawTextShadowLegacy(String string, float f, float f2, FontRenderer fontRenderer, int n) {
        Paint paint = GlHelper.toPaint(n);
        float f3 = (float)(n >> 24 & 0xFF) / 255.0f;
        GlHelper.drawTextFormatted(string, f + 0.5f, f2 + 0.5f, fontRenderer, GlHelper.toPaint(ColorUtil.fromARGB(0, 0, 0, (int)(f3 * 0.65f * 255.0f))), true);
        return GlHelper.drawTextFormatted(string, f, f2, fontRenderer, paint, false);
    }

    public static float drawTextBlurred(String string, float f, float f2, FontRenderer fontRenderer, int n, int n2, float f3) {
        if (string == null || string.isEmpty()) {
            return f;
        }
        DrawContext drawContext = GlHelper.getCanvas();
        float f4 = fontRenderer.getWidth(string);
        float f5 = fontRenderer.getFont() != null ? fontRenderer.getFont().getFontHeight() : fontRenderer.getSize();
        float f6 = Math.max(0.01f, f3 * 0.5f);
        drawContext.drawBlur(f, f2 - f5, f4, f5 * 2.0f, f6, () -> drawContext.drawString(string, f, f2, fontRenderer, GlHelper.toPaint(n2)));
        return GlHelper.drawText(string, f, f2, fontRenderer, n);
    }

    public static float drawTextCentered(float f, float f2, String string, FontRenderer fontRenderer, Paint paint) {
        float f3 = fontRenderer.getWidth(string);
        float f4 = fontRenderer.getFont() != null ? fontRenderer.getFont().getFontHeight() : fontRenderer.getSize();
        return GlHelper.drawTextFormatted(string, f - f3 / 2.0f, f2 - f4 / 2.0f, fontRenderer, paint, false);
    }

    public static Paint toPaint(Object object) {
        Paint paint = new Paint();
        if (object instanceof float[]) {
            paint.setColorFromArray((float[])object);
        } else if (object instanceof Color color) {
            paint.setColorARGB(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
        } else if (object instanceof Integer n) {
            paint.setColor(n);
        }
        return paint;
    }

    public static float getStringWidth(String string2, FontRenderer fontRenderer2) {
        if (string2 == null || string2.isEmpty()) {
            return 0.0f;
        }
        return stringWidthCache.computeIfAbsent(fontRenderer2, fontRenderer -> new HashMap<>()).computeIfAbsent(string2, string -> string != null ? fontRenderer2.getWidth(string.replaceAll("§.", "")) : 0.0f);
    }

    public static void drawRoundedRect(float f, float f2, float f3, float f4, float f5, Paint paint) {
        GlHelper.getCanvas().drawRoundedRect(RoundedRectangle.ofXYWHR(f, f2, f3, f4, f5), paint);
    }

    public static void drawRoundedRectCorners(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, Paint paint) {
        float[] fArray = new float[]{f5, f5, f6, f6, f7, f7, f8, f8};
        GlHelper.getCanvas().drawRoundedRect(RoundedRectangle.ofXYWHRadii(f, f2, f3, f4, fArray), paint);
    }

    private static int colorToInt(Color color) {
        return color.getAlpha() << 24 | color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
    }

    public static void drawGradientRoundedRect(float f, float f2, float f3, float f4, float f5, Color color, Color color2) {
        Paint.GradientCoords paint$GradientCoords = new Paint.GradientCoords(f, f2, f, f2 + f4, GlHelper.colorToInt(color), GlHelper.colorToInt(color2));
        Paint paint = new Paint().setGradCoords(paint$GradientCoords);
        GlHelper.getCanvas().drawRoundedRect(RoundedRectangle.ofXYWHR(f, f2, f3, f4, f5), paint);
    }

    public static void drawBlurredRoundedRectColor(float f, float f2, float f3, float f4, float f5, Color color, float f6, float f7, float f8) {
        GlHelper.getCanvas().drawBlurredRoundedRect(RoundedRectangle.ofXYWHR(f, f2, f3, f4, f5), f7, f8, f6, 1.0f, color.getRGB());
    }

    public static void drawShadowRoundedRect(float f, float f2, float f3, float f4, float f5, Color color) {
        GlHelper.getCanvas().drawBlurredRoundedRect(RoundedRectangle.ofXYWHR(f, f2, f3, f4, f5), 0.0f, 0.0f, 10.0f, 1.0f, color.getRGB());
    }

    public static void drawRoundedRectCornersColor(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, Color color) {
        float[] fArray = new float[]{f5, f5, f6, f6, f7, f7, f8, f8};
        GlHelper.getCanvas().drawBlurredRoundedRect(RoundedRectangle.ofXYWHRadii(f, f2, f3, f4, fArray), 0.0f, 0.0f, 10.0f, 1.0f, color.getRGB());
    }

    public static void drawRect(float f, float f2, float f3, float f4, Paint paint) {
        GlHelper.getCanvas().drawRect(Rectangle.ofXYWH(f, f2, f3, f4), paint);
    }

    public static void drawLine(float f, float f2, float f3, float f4, float f5, int n) {
        Paint paint = GlHelper.toPaint(n);
        paint.setStrokeWidth(f5);
        paint.setStrokeCap(Paint.StrokeCap.STROKE);
        GlHelper.getCanvas().drawLine(f, f2, f3, f4, paint);
    }

    static {
        new HashMap<>();
    }
}