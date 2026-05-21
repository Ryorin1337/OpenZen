package shit.zen.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import shit.zen.ClientBase;
import shit.zen.modules.impl.movement.Scaffold;
import shit.zen.render.DrawContext;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.Paint;
import shit.zen.render.RoundedRectangle;
import shit.zen.utils.animation.SpringAnimation;
import shit.zen.utils.game.MovementUtil;

public class ScaffoldHud
extends ClientBase
implements IHudElement {
    private static final FontRenderer blockCountFont = FontPresets.poppinsBold(14.0f);
    private static final FontRenderer speedFont = FontPresets.poppinsMedium(10.0f);
    private final SpringAnimation progressAnim = new SpringAnimation(250.0f, 1.0f, 22.0f, 0.0f);
    private long lastUpdateTime = 0L;

    @Override
    public boolean hasBackground() {
        return true;
    }

    @Override
    public IHudElement.Alignment getHudSize() {
        return IHudElement.Alignment.CENTER;
    }

    @Override
    public void renderGui(GuiGraphics guiGraphics, PoseStack poseStack, float f, float f2, float f3, float f4, float f5) {
        if (mc == null || mc.player == null || f5 <= 0.01f) {
            return;
        }
        ItemStack itemStack = this.getBlockItem();
        if (itemStack.isEmpty()) {
            return;
        }
        float f6 = f4 - 16.0f;
        float f7 = f + 8.0f;
        float f8 = f2 + 8.0f;
        if (f5 > 0.1f && f6 - 4.0f > 0.0f) {
            guiGraphics.renderItem(itemStack, (int)f7 + 2, (int)f8 + 2, 0, (int)f6 - 4);
        }
    }

    @Override
    public void render(DrawContext drawContext, float f, float f2, float f3, float f4, float f5) {
        if (mc == null || mc.player == null || f5 <= 0.01f) {
            return;
        }
        ItemStack itemStack = this.getBlockItem();
        if (itemStack.isEmpty()) {
            return;
        }
        float f6 = f4 - 16.0f;
        int n = itemStack.getCount();
        String string = n + " blocks";
        double d = MovementUtil.getSpeedBps();
        String string2 = String.format("%.2fb/s", new Object[]{d});
        float f7 = blockCountFont.getWidth(string);
        float f8 = speedFont.getWidth(string2);
        float f9 = Math.max(f7, f8);
        float f10 = f3 - f6 - f9 - 32.0f;
        float f11 = 6.0f;
        float f12 = f + 8.0f + f6 + 8.0f;
        float f13 = f2 + f4 / 2.0f - f11 / 2.0f;
        float f14 = Math.min(1.0f, (float)n / 64.0f);
        this.setX(f14);
        try (Paint paint = new Paint()){
            paint.setColor(this.colorWithAlpha(new Color(30, 30, 30).getRGB(), f5));
            drawContext.drawRoundedRect(RoundedRectangle.ofXYWHR(f12, f13, f10, f11, f11 / 2.0f), paint);
            if (this.progressAnim.getValue() > 0.0f) {
                paint.setColor(this.colorWithAlpha(new Color(153, 0, 255).getRGB(), f5));
                drawContext.drawRoundedRect(RoundedRectangle.ofXYWHR(f12, f13, f10 * this.progressAnim.getValue(), f11, f11 / 2.0f), paint);
            }
        }
        float f15 = f12 + f10 + 8.0f;
        float f16 = f2 + f4 / 2.0f;
        float f17 = f15 + (f9 - f7) / 2.0f;
        float f18 = f15 + (f9 - f8) / 2.0f;
        try (Paint paint = new Paint()){
            paint.setColor(this.colorWithAlpha(Color.WHITE.getRGB(), f5));
            drawContext.drawString(string, f17, f16 - blockCountFont.getMetrics().capHeight() / 2.0f + 2.0f, blockCountFont, paint);
            paint.setColor(this.colorWithAlpha(Color.GRAY.getRGB(), f5));
            drawContext.drawString(string2, f18, f16 + speedFont.getMetrics().capHeight() / 2.0f + 8.0f, speedFont, paint);
        }
    }

    private ItemStack getBlockItem() {
        if (mc.player == null) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = mc.player.getMainHandItem();
        if (itemStack.getItem() instanceof BlockItem) {
            return itemStack;
        }
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack2 = mc.player.getInventory().getItem(i);
            if (!(itemStack2.getItem() instanceof BlockItem)) continue;
            return itemStack2;
        }
        return ItemStack.EMPTY;
    }

    private void setX(float f) {
        long l = System.currentTimeMillis();
        if (this.lastUpdateTime == 0L || l - this.lastUpdateTime > 1000L) {
            this.lastUpdateTime = l;
            this.progressAnim.setValue(f);
            this.progressAnim.setTargetValue(f);
            return;
        }
        float f2 = (float)(l - this.lastUpdateTime) / 1000.0f;
        if (f2 <= 0.0f) {
            return;
        }
        this.lastUpdateTime = l;
        this.progressAnim.setTargetValue(f);
        this.progressAnim.update(f2);
    }

    @Override
    public boolean isVisible() {
        return Scaffold.INSTANCE != null && Scaffold.INSTANCE.isEnabled();
    }

    @Override
    public IHudElement.Size getHudAlignment() {
        return new IHudElement.Size(260.0f, 30.0f);
    }
}