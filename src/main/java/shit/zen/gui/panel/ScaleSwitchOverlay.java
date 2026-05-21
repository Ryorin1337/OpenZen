package shit.zen.gui.panel;

import java.awt.Color;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import shit.zen.ClientBase;
import shit.zen.render.DrawContext;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.render.Renderer;
import shit.zen.render.TextGlow;
import shit.zen.utils.math.LerpUtil;
import shit.zen.utils.render.RenderUtil;

public class ScaleSwitchOverlay
extends ClientBase {
    private static final Color OVERLAY_BG_COLOR = new Color(124, 124, 124, 13);
    private boolean isActive = false;
    private float alpha = 0.0f;
    private long startTime = 0L;
    private float fromScale = 1.0f;
    private float toScale = 1.0f;

    public void show(float f, float f2) {
        this.fromScale = f;
        this.toScale = f2;
        this.isActive = true;
        this.startTime = System.currentTimeMillis();
    }

    public void hide() {
        this.isActive = false;
    }

    public boolean isShowing() {
        return this.isActive;
    }

    public boolean isFullyShown() {
        return this.isActive && this.alpha >= 1.0f;
    }

    public boolean isFullyHidden() {
        return !this.isActive && this.alpha <= 0.0f;
    }

    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        if (!this.isActive && this.alpha <= 0.005f) {
            return;
        }
        this.updateAlpha();
        if (this.alpha <= 0.005f) {
            return;
        }
        try {
            this.drawBackground(guiGraphics, n, n2);
            float f2 = 400.0f * f;
            float f3 = 180.0f * f;
            int n3 = (int)(((float)n - f2) / 2.0f);
            int n4 = (int)(((float)n2 - f3) / 2.0f);
            this.drawGlow(guiGraphics, n3, n4, f2, f3, f);
            this.drawContent(guiGraphics, n3, n4, f2, f);
        } catch (Exception exception) {
            // empty catch block
        }
    }

    private void updateAlpha() {
        this.alpha = this.isActive ? LerpUtil.lerp(this.alpha, 1.0f, 0.08f) : LerpUtil.lerp(this.alpha, 0.0f, 0.08f);
    }

    private void drawBackground(GuiGraphics guiGraphics, int n, int n2) {
        Color color = new Color(OVERLAY_BG_COLOR.getRed(), OVERLAY_BG_COLOR.getGreen(), OVERLAY_BG_COLOR.getBlue(), (int)((float)OVERLAY_BG_COLOR.getAlpha() * this.alpha));
        RenderUtil.drawRoundedRect(guiGraphics.pose(), 0.0f, 0.0f, n, n2, 0.0f, color.getRGB());
    }

    private void drawGlow(GuiGraphics guiGraphics, int n, int n2, float f, float f2, float f3) {
        TextGlow.drawBackground(guiGraphics.pose(), n, n2, f, f2, 12.0f * f3, this.alpha);
    }

    private void drawContent(GuiGraphics guiGraphics, int n, int n2, float f, float f2) {
        Renderer.renderConsumer((drawContext -> {
            int n3 = (int)(255.0f * this.alpha);
            if (n3 <= 0) {
                return;
            }
            FontRenderer fontRenderer = FontPresets.axiformaBold(24.0f * f2);
            String string = "Waiting";
            float f3 = GlHelper.getStringWidth(string, fontRenderer);
            float f4 = (float)n + (f - f3) / 2.0f;
            float f5 = (float)n2 + 45.0f * f2;
            int n4 = n3 << 24 | 0xFFFFFF;
            int n5 = n3 << 24 | 0xFFFFFF;
            TextGlow.drawGlowText(string, f4, f5, fontRenderer, n4, n5, 10.0f * f2);
            FontRenderer fontRenderer2 = FontPresets.axiformaRegular(18.0f * f2);
            String string2 = String.format(Locale.US, "Switching scale from %.0f%% to %.0f%%", new Object[]{Float.valueOf(this.fromScale * 100.0f), Float.valueOf(this.toScale * 100.0f)});
            float f6 = GlHelper.getStringWidth(string2, fontRenderer2);
            float f7 = (float)n + (f - f6) / 2.0f;
            float f8 = (float)n2 + 75.0f * f2;
            int n6 = n3 << 24 | 0xCCCCCC;
            GlHelper.drawText(string2, f7, f8, fontRenderer2, n6);
            this.drawAnimatedDots(n, (int)((float)n2 + 115.0f * f2), (int)f, n3, f2);
        }));
    }

    private void drawAnimatedDots(int n, int n2, int n3, int n4, float f) {
        FontRenderer fontRenderer = FontPresets.axiformaBold(20.0f * f);
        String string = "•";
        float f2 = GlHelper.getStringWidth(string, fontRenderer);
        float f3 = f2 * 3.0f + 20.0f * f;
        float f4 = (float)n + ((float)n3 - f3) / 2.0f;
        int n5 = n4 << 24 | 0xFFFFFF;
        long l = System.currentTimeMillis() - this.startTime;
        long l2 = l % 1400L;
        for (int i = 0; i < 3; ++i) {
            float f5;
            float f6 = f4 + (float)i * (f2 + 10.0f * f);
            long l3 = (long)i * 150L;
            long l4 = l3 + 300L;
            float f7 = 0.0f;
            if (l2 >= l3 && l2 <= l4) {
                f5 = (float)(l2 - l3) / 300.0f;
                float f8 = f5 * (float)Math.PI;
                f7 = (float)(Math.sin(f8) * 6.0 * (double)f);
            }
            f5 = (float)n2 - f7;
            GlHelper.drawText(string, f6, f5, fontRenderer, n5);
        }
    }

    static {
        new Color(255, 255, 255, 40);
    }
}