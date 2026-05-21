package shit.zen.gui.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import shit.zen.gui.legacy.ModuleButton;
import shit.zen.gui.legacy.SettingComponent;
import shit.zen.render.FontStore;
import shit.zen.settings.Setting;
import shit.zen.settings.impl.NumberSetting;
import shit.zen.utils.math.MathUtil;
import shit.zen.utils.render.RenderUtil;

public class NumberComponent
extends SettingComponent {
    private boolean dragging;
    private final NumberSetting numberSetting;

    public NumberComponent(Setting<?> setting, ModuleButton moduleButton, int n) {
        super(setting, moduleButton, n);
        this.numberSetting = (NumberSetting)setting;
        this.dragging = false;
    }

    @Override
    public void renderWithAlpha(PoseStack poseStack, int n, int n2, float f, float f2) {
        int n3 = this.parentButton.panel.y + this.parentButton.yOffset + this.parentButton.panel.rowHeight + this.yOffset;
        int n4 = this.parentButton.panel.x;
        int n5 = this.parentButton.panel.width;
        int n6 = this.parentButton.panel.rowHeight;
        int n7 = new Color(255, 255, 255, (int)(255.0f * f2)).getRGB();
        int n8 = new Color(138, 180, 248, (int)(255.0f * f2)).getRGB();
        int n9 = 8;
        int n10 = n4 + n9;
        int n11 = (int)FontStore.OPENSANS_16.getStringWidth("00.00");
        int n12 = n5 - n9 * 2 - n11 + 20;
        if (this.dragging) {
            this.updateSliderValue(n, n10, n12);
        }
        String string = this.numberSetting.getName();
        String string2 = this.numberSetting.getStep().doubleValue() % 1.0 == 0.0 ? String.format("%d", new Object[]{this.numberSetting.getValue().intValue()}) : String.format("%.2f", new Object[]{Float.valueOf(this.numberSetting.getValue().floatValue())});
        float f3 = (float)n3 + ((float)n6 - FontStore.OPENSANS_16.getFontHeight()) / 2.0f;
        FontStore.OPENSANS_16.drawStringWithShadow(poseStack, string, n10, f3, n7);
        FontStore.OPENSANS_16.drawStringWithShadow(poseStack, string2, (float)(n4 + n5 - n9) - FontStore.OPENSANS_16.getStringWidth(string2), f3, n7);
        float f4 = this.numberSetting.getMin().floatValue();
        float f5 = this.numberSetting.getMax().floatValue();
        float f6 = this.numberSetting.getValue().floatValue();
        float f7 = (f6 - f4) / (f5 - f4);
        int n13 = n3 + n6 / 2 + 10;
        int n14 = 2;
        RenderUtil.drawFilledRect(poseStack, n10, n13, n12, n14, new Color(10, 10, 10, 200).getRGB());
        RenderUtil.drawFilledRect(poseStack, n10, n13, (int)((float)n12 * f7), n14, n8);
    }

    private void updateSliderValue(double d, int n, int n2) {
        float f = this.numberSetting.getMin().floatValue();
        float f2 = this.numberSetting.getMax().floatValue();
        float f3 = f2 - f;
        double d2 = Math.max(n, Math.min(d, n + n2));
        float f4 = f + f3 * (float)((d2 - (double)n) / (double)n2);
        this.numberSetting.setValue(MathUtil.roundDecimal(f4, 2));
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        boolean bl;
        int n2 = this.parentButton.panel.x;
        int n3 = this.parentButton.panel.width;
        int n4 = this.parentButton.panel.y + this.parentButton.yOffset + this.parentButton.panel.rowHeight + this.yOffset;
        int n5 = this.parentButton.panel.rowHeight;
        int n6 = 8;
        int n7 = n2 + n6;
        int n8 = (int)FontStore.OPENSANS_16.getStringWidth("00.00");
        int n9 = n3 - n6 * 2 - n8 + 20;
        boolean bl2 = bl = d >= (double)n7 && d <= (double)(n7 + n9) && d2 >= (double)n4 && d2 <= (double)(n4 + n5);
        if (n == 0 && bl) {
            this.dragging = true;
            this.updateSliderValue(d, n7, n9);
        }
    }

    @Override
    public void mouseReleased(double d, double d2, int n) {
        if (n == 0) {
            this.dragging = false;
        }
    }
}