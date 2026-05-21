package shit.zen.gui.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import shit.zen.gui.legacy.ModuleButton;
import shit.zen.gui.legacy.SettingComponent;
import shit.zen.render.FontStore;
import shit.zen.settings.Setting;
import shit.zen.settings.impl.BooleanSetting;
import shit.zen.utils.render.RenderUtil;

public class BooleanComponent
extends SettingComponent {
    private final BooleanSetting booleanSetting;
    private float toggleAnim = 0.0f;
    private final float animSpeed = 0.2f;

    public BooleanComponent(Setting<?> setting, ModuleButton moduleButton, int n) {
        super(setting, moduleButton, n);
        this.booleanSetting = (BooleanSetting)setting;
        this.toggleAnim = this.booleanSetting.getValue() != false ? 1.0f : 0.0f;
    }

    @Override
    public void renderWithAlpha(PoseStack poseStack, int n, int n2, float f, float f2) {
        float f3;
        float f4 = f3 = this.booleanSetting.getValue() != false ? 1.0f : 0.0f;
        this.toggleAnim = Math.abs(this.toggleAnim - f3) > 0.01f ? (this.toggleAnim += (f3 - this.toggleAnim) * 0.2f) : f3;
        int n3 = this.parentButton.panel.y + this.parentButton.yOffset + this.parentButton.panel.rowHeight + this.yOffset;
        int n4 = 4;
        int n5 = this.parentButton.panel.rowHeight - n4 * 2;
        int n6 = this.parentButton.panel.x;
        int n7 = this.parentButton.panel.width;
        int n8 = 8;
        String string = this.booleanSetting.getName();
        float f5 = (float)(n3 + n4) + ((float)n5 - FontStore.OPENSANS_16.getFontHeight()) / 2.0f;
        int n9 = new Color(255, 255, 255, (int)(255.0f * f2)).getRGB();
        FontStore.OPENSANS_16.drawStringWithShadow(poseStack, string, n6 + n8, f5, n9);
        int n10 = 18;
        int n11 = 8;
        int n12 = n6 + n7 - n8 - n10;
        int n13 = n3 + n4 + (n5 - n11) / 2 + 2;
        Color color = this.booleanSetting.getValue() != false ? new Color(138, 180, 248, (int)(200.0f * f2)) : new Color(100, 100, 100, (int)(180.0f * f2));
        RenderUtil.drawFilledRect(poseStack, n12, n13, n10, n11, color.getRGB());
        float f6 = 7.0f;
        float f7 = n11 + 4;
        float f8 = (float)(n12 + 1) + ((float)n10 - f6 - 2.0f) * this.toggleAnim;
        float f9 = n13 - 2;
        RenderUtil.drawFilledRect(poseStack, f8, f9, f6, f7, new Color(160, 195, 255, (int)(255.0f * f2)).getRGB());
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        if (this.isHovered(d, d2) && n == 0) {
            boolean bl = this.booleanSetting.getValue();
            this.booleanSetting.setValue(!bl);
            if (bl != this.booleanSetting.getValue()) {
                this.parentButton.panel.recalcLayout();
            }
        }
        super.mouseClicked(d, d2, n);
    }

    @Override
    public void mouseReleased(double d, double d2, int n) {
        super.mouseReleased(d, d2, n);
    }
}