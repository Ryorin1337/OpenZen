package shit.zen.gui;

import java.awt.Color;
import shit.zen.ClientBase;
import shit.zen.ZenClient;
import shit.zen.event.impl.GlRenderEvent;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.render.Paint;
import shit.zen.event.EventTarget;

public class IntroAnimation
extends ClientBase {
    private static volatile boolean isActive = false;
    private long startTime = -1L;
    private boolean finished = false;

    public IntroAnimation() {
        isActive = true;
    }

    public static boolean isRunning() {
        return isActive;
    }

    @EventTarget(value=4)
    public void onRender(GlRenderEvent glRenderEvent) {
        float f;
        float f2;
        long l;
        if (this.finished) {
            return;
        }
        if (this.startTime < 0L) {
            this.startTime = System.currentTimeMillis();
        }
        long l2 = l = System.currentTimeMillis() - this.startTime;
        float f3 = mc.getWindow().getGuiScaledWidth();
        float f4 = mc.getWindow().getGuiScaledHeight();
        float f5 = f3 / 2.0f;
        float f6 = f4 / 2.0f;
        long l3 = 1100L;
        long l4 = l3 + 900L + 700L + 500L + 300L + 500L + 1300L;
        if (l2 <= 800L) {
            float fadeIn = IntroAnimation.easeOutCubic(IntroAnimation.clamp01((float)l2 / 800.0f));
            f2 = 0.6f * fadeIn;
        } else if (l2 <= l4) {
            f2 = 0.6f;
        } else if (l2 <= l4 + 700L) {
            float fadeOut = 1.0f - IntroAnimation.easeInCubic(IntroAnimation.clamp01((float)(l2 - l4) / 700.0f));
            f2 = 0.6f * fadeOut;
        } else {
            this.finish();
            return;
        }
        Paint paint = GlHelper.toPaint(new Color(0, 0, 0, (int)(f2 * 255.0f)));
        GlHelper.drawRect(0.0f, 0.0f, f3, f4, paint);
        float f7 = 1.0f;
        float f8 = 0.0f;
        if (l2 >= l3) {
            long l5 = l2 - l3;
            if (l5 <= 900L) {
                float f9 = IntroAnimation.easeOutCubic(IntroAnimation.clamp01((float)l5 / 900.0f));
                f7 = IntroAnimation.lerp(2.0f, 1.0f, f9);
                f8 = f9;
            } else {
                f7 = 1.0f;
                f8 = 1.0f;
            }
        }
        float f10 = 0.0f;
        long l6 = l3 + 900L + 700L;
        if (l2 > l6 && l2 <= l6 + 500L) {
            f10 = IntroAnimation.easeOutCubic((float)(l2 - l6) / 500.0f);
        } else if (l2 > l6 + 500L) {
            f10 = 1.0f;
        }
        FontRenderer fontRenderer = FontPresets.axiformaBold(64.0f * f7);
        FontRenderer fontRenderer2 = FontPresets.axiformaBold(64.0f);
        float f11 = GlHelper.getStringWidth("Z", fontRenderer);
        float f12 = GlHelper.getStringWidth("EN", fontRenderer2);
        float f13 = f5 - f11 / 2.0f;
        float f14 = f5 - (f11 + 0.0f + f12) / 2.0f;
        float f15 = IntroAnimation.lerp(f13, f14, f10);
        float f16 = f6 - fontRenderer.getMetrics().capHeight() / 2.0f;
        float f17 = 0.0f;
        float f18 = 12.0f;
        long l7 = l6 + 500L + 300L;
        if (l2 > l7 && l2 <= l7 + 500L) {
            f17 = f = IntroAnimation.easeOutCubic((float)(l2 - l7) / 500.0f);
            f18 = (1.0f - f) * 12.0f;
        } else if (l2 > l7 + 500L) {
            f17 = 1.0f;
            f18 = 0.0f;
        }
        f = 1.0f;
        if (l2 > l4) {
            f = 1.0f - IntroAnimation.clamp01((float)(l2 - l4) / 700.0f);
        }
        int n = new Color(1.0f, 1.0f, 1.0f, IntroAnimation.clamp01(f8 * f)).getRGB();
        GlHelper.drawText("Z", f15, f16, fontRenderer, n);
        if (f17 > 0.0f) {
            float f19 = f15 + f11 + 0.0f;
            float f20 = f6 - fontRenderer2.getMetrics().capHeight() / 2.0f + f18;
            int n2 = new Color(1.0f, 1.0f, 1.0f, IntroAnimation.clamp01(f17 * f)).getRGB();
            GlHelper.drawText("EN", f19, f20, fontRenderer2, n2);
        }
    }

    private void finish() {
        if (!this.finished) {
            this.finished = true;
            try {
                ZenClient.instance.getEventBus().unregister(this);
            } catch (Throwable throwable) {
                // empty catch block
            }
            isActive = false;
        }
    }

    private static float clamp01(float f) {
        return f < 0.0f ? 0.0f : (f > 1.0f ? 1.0f : f);
    }

    private static float lerp(float f, float f2, float f3) {
        return f + (f2 - f) * f3;
    }

    private static float easeOutCubic(float f) {
        float f2 = IntroAnimation.clamp01(f);
        f2 = (float)(1.0 - Math.pow(1.0f - f2, 3.0));
        return f2;
    }

    private static float easeInCubic(float f) {
        float f2 = IntroAnimation.clamp01(f);
        f2 = f2 * f2 * f2;
        return f2;
    }
}