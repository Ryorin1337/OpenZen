package shit.zen.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import shit.zen.ClientBase;
import shit.zen.modules.impl.world.AutoPlay;
import shit.zen.render.DrawContext;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.Paint;
import shit.zen.render.Path;

public class AutoPlayHud
extends ClientBase
implements IHudElement {
    private static final FontRenderer font = FontPresets.poppinsRegular(24.0f);
    private float animProgress = 0.0f;
    private long lastUpdateTime = -1L;
    private long disableTime = -1L;

    @Override
    public boolean isVisible() {
        if (AutoPlay.instance == null || !AutoPlay.instance.isEnabled()) {
            return false;
        }
        if (AutoPlay.instance.pendingDisconnect) {
            return true;
        }
        long l = AutoPlay.instance.disconnectTime;
        if (l <= 0L) {
            return false;
        }
        double d = AutoPlay.instance.getDelay().getValue().doubleValue() * 1000.0;
        long l2 = System.currentTimeMillis() - l;
        if ((double)l2 >= d) {
            long l3 = l2 - (long)d;
            return l3 < 500L;
        }
        return false;
    }

    @Override
    public IHudElement.Size getHudAlignment() {
        if (!this.isVisible()) {
            return new IHudElement.Size(0.0f, 40.0f);
        }
        long l = AutoPlay.instance.disconnectTime;
        double d = AutoPlay.instance.getDelay().getValue().doubleValue() * 1000.0;
        long l2 = System.currentTimeMillis() - l;
        boolean bl = d <= 0.0 || (double)l2 >= d;
        long l3 = bl ? l2 - (long)d : -1L;
        String string = bl ? "Done!" : "Sending you to next game...";
        float f = font.getBounds(string).getWidth();
        float f2 = 28.0f;
        float f3 = 18.0f + f2 + 8.0f + f + 18.0f;
        float f4 = 30.0f;
        float f5 = f3 - 30.0f;
        float f6 = 60.0f;
        f5 = Math.max(f5, f6);
        float f7 = 40.0f;
        if (bl) {
            float f8 = Mth.clamp((float)l3 / 400.0f, 0.0f, 1.0f);
            f7 = Mth.lerp(f8, 40.0f, 25.0f);
        }
        return new IHudElement.Size(f5, f7);
    }

    @Override
    public IHudElement.Alignment getHudSize() {
        return IHudElement.Alignment.CENTER;
    }

    @Override
    public boolean hasBackground() {
        return true;
    }

    @Override
    public void renderGui(GuiGraphics guiGraphics, PoseStack poseStack, float f, float f2, float f3, float f4, float f5) {
    }

    @Override
    public void render(DrawContext drawContext, float f, float f2, float f3, float f4, float f5) {
        float f6;
        float f7;
        float f8;
        float f9;
        float f10;
        float f11;
        long l = 0L;
        if (mc == null || mc.player == null || f5 <= 0.01f || AutoPlay.instance == null) {
            return;
        }
        long l2 = System.currentTimeMillis();
        if (!AutoPlay.instance.pendingDisconnect) {
            if (this.disableTime == -1L) {
                this.disableTime = l2;
            }
        } else {
            this.disableTime = -1L;
        }
        if (this.lastUpdateTime == -1L) {
            this.lastUpdateTime = l2;
        }
        long l3 = l2 - this.lastUpdateTime;
        this.lastUpdateTime = l2;
        long l4 = AutoPlay.instance.disconnectTime;
        double d = AutoPlay.instance.getDelay().getValue().doubleValue();
        double d2 = d * 1000.0;
        long l5 = System.currentTimeMillis() - l4;
        float f12 = d2 > 0.0 ? (float)((double)l5 / d2) : 1.0f;
        f12 = Mth.clamp(f12, 0.0f, 1.0f);
        float f13 = Mth.clamp((float)l3 / 200.0f, 0.0f, 1.0f);
        this.animProgress = Mth.lerp(f13, this.animProgress, f12);
        if (Math.abs(this.animProgress - f12) < 0.01f) {
            this.animProgress = f12;
        }
        boolean bl = false;
        if (l4 > 0L) {
            long l6 = System.currentTimeMillis() - l4;
            if (d2 <= 0.0 || (double)l6 >= d2) {
                bl = true;
                l = d2 > 0.0 ? l6 - (long)d2 : l6;
            }
        }
        float f14 = f2 + f4 / 2.0f;
        float f15 = f4 - 12.0f;
        float f16 = f + 18.0f;
        float f17 = f2 + 6.0f;
        float f18 = f16 + f15 / 2.0f;
        float f19 = f17 + f15 / 2.0f;
        float f20 = f15 / 2.0f - 2.0f;
        try (Paint object = new Paint();
             Path path = new Path()){
            float f21;
            object.setStrokeWidth(2.0f);
            object.setStrokeCap(Paint.StrokeCap.STROKE);
            object.setStrokeJoin(Paint.StrokeJoin.ROUND);
            object.setColor(this.colorWithAlpha(Color.WHITE.getRGB(), f5));
            if (this.animProgress > 0.001f) {
                f11 = 360.0f * this.animProgress;
                f21 = f18 - f20;
                f10 = f19 - f20;
                f9 = f18 + f20;
                f8 = f19 + f20;
                drawContext.drawArc(f21, f10, f9, f8, -90.0f, f11, false, object);
            }
            if (bl) {
                path.reset();
                f11 = Mth.clamp((float)l / 400.0f, 0.0f, 1.0f);
                f21 = f18 - f20 * 0.4f;
                f10 = f19;
                f9 = f18 - f20 * 0.15f;
                f8 = f19 + f20 * 0.3f;
                f7 = f18 + f20 * 0.4f;
                f6 = f19 - f20 * 0.3f;
                float f22 = (float)Math.hypot(f9 - f21, f8 - f10);
                float f23 = (float)Math.hypot(f7 - f9, f6 - f8);
                float f24 = f22 + f23;
                float f25 = f24 * f11;
                path.moveTo(f21, f10);
                if (f25 <= f22) {
                    float f26 = f25 / f22;
                    path.lineTo(Mth.lerp(f26, f21, f9), Mth.lerp(f26, f10, f8));
                } else {
                    path.lineTo(f9, f8);
                    float f27 = (f25 - f22) / f23;
                    path.lineTo(Mth.lerp(f27, f9, f7), Mth.lerp(f27, f8, f6));
                }
                drawContext.drawPath(path, object);
            }
        }
        String statusText = bl ? "Done!" : "Sending you to next game...";
        float f28 = f15;
        f11 = f16 + f28 + 8.0f;
        try (Paint paint = new Paint()){
            paint.setColor(this.colorWithAlpha(Color.WHITE.getRGB(), f5));
            f10 = f14 - font.getMetrics().capHeight() / 2.0f + 8.0f;
            drawContext.drawString(statusText, f11, f10, font, paint);
            f9 = 2.5f;
            f8 = f10 + font.getMetrics().descent() + 8.0f;
            f7 = f + f3 - f11 - 18.0f;
            paint.setStrokeCap(Paint.StrokeCap.FILL);
            paint.setColor(this.colorWithAlpha(new Color(0, 0, 0, 40).getRGB(), f5));
            if (this.animProgress > 0.0f) {
                paint.setColor(this.colorWithAlpha(Color.WHITE.getRGB(), f5));
                f6 = f7 * this.animProgress;
            }
        }
    }
}