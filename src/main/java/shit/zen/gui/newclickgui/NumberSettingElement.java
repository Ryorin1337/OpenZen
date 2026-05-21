package shit.zen.gui.newclickgui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import shit.zen.gui.NewClickGui;
import shit.zen.gui.newclickgui.CategoryPanel;
import shit.zen.gui.newclickgui.SettingElement;
import shit.zen.render.FontStore;
import shit.zen.settings.impl.NumberSetting;
import shit.zen.utils.animation.SmoothAnimationTimer;
import shit.zen.utils.math.Easings;
import shit.zen.utils.math.MathUtil;
import shit.zen.utils.misc.CursorUtil;
import shit.zen.utils.render.ColorUtil;
import shit.zen.utils.render.RenderUtil;

public class NumberSettingElement
extends SettingElement<NumberSetting> {
    private final SmoothAnimationTimer sliderTimer = new SmoothAnimationTimer();
    private boolean isTruncated;
    private boolean isHovered;
    float sliderX;
    private boolean isDragging;

    public NumberSettingElement(CategoryPanel categoryPanel, NumberSetting numberSetting) {
        super(categoryPanel, numberSetting);
    }

    @Override
    public float getHeight() {
        return 30.0f;
    }

    @Override
    public void render(NewClickGui newClickGui, GuiGraphics guiGraphics, PoseStack poseStack, int n, int n2, float f, float f2) {
        float f3;
        this.isHovered = CursorUtil.isInBounds(n, n2, this.x, this.y, 120.0f, this.getHeight());
        float f4 = 108.0f;
        float f5 = 5.0f;
        float f6 = this.y + this.getHeight() / 2.0f + (this.getHeight() / 2.0f - f5) / 2.0f;
        this.visibilityTimer.animate(this.setting.getVisibility().displayable() ? 1.0 : 0.0, 0.2, Easings.EASE_OUT_POW2);
        this.visibilityTimer.tick();
        if (Mth.equal(f *= this.visibilityTimer.getValueF(), 0.0f)) {
            return;
        }
        float f7 = this.y + (this.getHeight() / 2.0f - FontStore.AXIFORMA_REGULAR_14.getFontHeight()) / 2.0f + 1.0f;
        String string = this.setting.getName();
        if (FontStore.AXIFORMA_REGULAR_14.getStringWidth(string) > 78.0f) {
            string = string.substring(0, 10);
            string = string + "...";
            this.isTruncated = true;
        }
        FontStore.AXIFORMA_REGULAR_14.drawString(poseStack, string, this.x + 6.0f, f7, ColorUtil.withAlpha(-1, f * 0.8f));
        String string2 = String.format("%.2f", new Object[]{Float.valueOf(this.setting.getValue().floatValue())});
        FontStore.AXIFORMA_BOLD_13.drawString(poseStack, string2, this.x + 120.0f - FontStore.AXIFORMA_BOLD_13.getStringWidth(string2) - 6.0f, f7, ColorUtil.withAlpha(-1, f * 0.92f));
        float f8 = this.setting.getValue().floatValue() / this.setting.getMax().floatValue();
        f8 = Mth.clamp(f8, 0.0f, 1.0f);
        this.sliderTimer.animate(f8, 0.2, Easings.EASE_OUT_POW2);
        this.sliderTimer.tick();
        if (this.isDragging) {
            NumberSetting numberSetting = this.setting;
            f3 = ((float)n - (this.x + 6.0f)) / f4;
            double d = numberSetting.getMin().floatValue() + (numberSetting.getMax().floatValue() - numberSetting.getMin().floatValue()) * f3;
            double d2 = numberSetting.getStep().floatValue();
            double d3 = (double)Math.round(MathUtil.clamp(d, numberSetting.getMin().floatValue(), numberSetting.getMax().floatValue()) / d2) * d2;
            numberSetting.setValue((double)Math.round(d3 * 1000.0) / 1000.0);
        }
        RenderUtil.drawRoundedRect(poseStack, this.x + 6.0f, f6, f4, 5.0f, 2.0f, ColorUtil.withAlpha(ColorUtil.fromRGB(60, 60, 60), f));
        float f9 = this.sliderTimer.getValueF();
        f3 = 10.0f;
        float f10 = 5.0f + f3 * 2.0f;
        RenderUtil.drawRoundedRect(poseStack, this.x + 6.0f - f3, f6 - f3, Math.max(f4 * f9 + f3 * 2.0f, f10), f10, 2.0f + f3 / 2.0f + 1.0f, f3, ColorUtil.withAlpha(-13768502, 0.26f * f));
        RenderUtil.drawRoundedRect(poseStack, this.x + 6.0f, f6, f4 * f9, 5.0f, 2.0f, ColorUtil.withAlpha(CategoryPanel.ACCENT_COLOR, f));
        float f11 = Math.max(this.x + 6.0f + this.sliderTimer.getValueF() * f4 - 5.0f - 0.5f, this.x + 6.0f - 0.5f);
        RenderUtil.drawRoundedRect(poseStack, f11 - f3, f6 - 0.5f - f3, 6.0f + f3 * 2.0f, 6.0f + f3 * 2.0f, 2.0f + f3 / 2.0f + 1.0f, f3, ColorUtil.withAlpha(-1, 0.36f * f));
        RenderUtil.drawRoundedRect(poseStack, f11, f6 - 0.5f, 6.0f, 6.0f, 2.9f, ColorUtil.withAlpha(-1, f));
        if (this.isHovered && this.isTruncated) {
            this.parentPanel.setHoveredSettingElement(this);
            this.parentPanel.setTooltipText(this.setting.getName());
            this.parentPanel.setShowTooltip(true);
        } else if (this.parentPanel.getHoveredSettingElement() == this) {
            this.parentPanel.setShowTooltip(false);
            this.parentPanel.setHoveredSettingElement(null);
        }
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (!this.setting.getVisibility().displayable()) {
            return false;
        }
        float f = 108.0f;
        float f2 = 5.0f;
        float f3 = this.y + this.getHeight() / 2.0f + (this.getHeight() / 2.0f - f2) / 2.0f;
        if (this.isHovered && CursorUtil.isInBounds((float)d, (float)d2, this.x + 6.0f, f3, f, 5.0f)) {
            this.isDragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double d, double d2, int n) {
        float f = 108.0f;
        float f2 = 5.0f;
        float f3 = this.y + this.getHeight() / 2.0f + (this.getHeight() / 2.0f - f2) / 2.0f;
        this.isDragging = false;
        return false;
    }
}