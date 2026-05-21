package shit.zen.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.utils.render.RenderUtil;

public final class TextGlow {
    private TextGlow() {
    }

    public static void drawBackground(PoseStack poseStack, float f, float f2, float f3, float f4, float f5, float f6) {
        int n = TextGlow.clampAlpha(150.0f * f6);
        int n2 = TextGlow.clampAlpha(35.0f * f6);
        RenderUtil.drawRoundedRect(poseStack, f, f2, f3, f4, f5, new Color(24, 24, 24, n).getRGB());
        RenderUtil.drawRoundedRect(poseStack, f, f2, f3, f4, f5, new Color(255, 255, 255, n2).getRGB());
    }

    public static float drawGlowText(String string, float f, float f2, FontRenderer fontRenderer, int n, int n2, float f3) {
        return GlHelper.drawText(string, f, f2, fontRenderer, n);
    }

    private static int clampAlpha(float f) {
        return Math.max(0, Math.min(255, Math.round(f)));
    }
}