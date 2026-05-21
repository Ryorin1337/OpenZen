package shit.zen.gui.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import lombok.Getter;
import shit.zen.gui.legacy.ModuleButton;
import shit.zen.gui.legacy.SettingComponent;
import shit.zen.render.FontStore;
import shit.zen.settings.Setting;
import shit.zen.settings.impl.ModeSetting;
import shit.zen.utils.render.RenderUtil;

public class ModeComponent
extends SettingComponent {
    private final ModeSetting modeSetting;
    @Getter
    private boolean dropdownOpen = false;

    public ModeComponent(Setting<?> setting, ModuleButton moduleButton, int n) {
        super(setting, moduleButton, n);
        this.modeSetting = (ModeSetting)setting;
    }

    @Override
    public void renderWithAlpha(PoseStack poseStack, int n, int n2, float f, float f2) {
        int n3 = this.parentButton.panel.y + this.parentButton.yOffset + this.parentButton.panel.rowHeight + this.yOffset + 3;
        int n4 = this.parentButton.panel.x;
        int n5 = this.parentButton.panel.width;
        int n6 = this.parentButton.panel.rowHeight;
        int n7 = 4;
        int n8 = n6 - n7 * 2;
        int n9 = 8;
        int n10 = 4;
        int n11 = n4 + n9 + n10;
        int n12 = n5 - (n9 + n10) * 2;
        int n13 = new Color(255, 255, 255, (int)(255.0f * f2)).getRGB();
        boolean bl = n >= n11 && n <= n11 + n12 && n2 >= n3 + n7 && n2 <= n3 + n7 + n8;
        Color color = bl && !this.dropdownOpen ? new Color(45, 45, 45, (int)(180.0f * f2)) : new Color(25, 25, 25, (int)(150.0f * f2));
        RenderUtil.drawFilledRect(poseStack, n11, n3 + n7, n12, n8, color.getRGB());
        Color color2 = new Color(100, 100, 100, (int)(180.0f * f2));
        RenderUtil.drawFilledRect(poseStack, n4 + n9 + 2, n3 + n7, n10 - 2, n8, color2.getRGB());
        RenderUtil.drawFilledRect(poseStack, n4 + n5 - n9 - n10, n3 + n7, n10 - 2, n8, color2.getRGB());
        String string = this.modeSetting.getName();
        float f3 = (float)(n3 + n7) + ((float)n8 - FontStore.OPENSANS_16.getFontHeight()) / 2.0f - 1.5f;
        FontStore.OPENSANS_16.drawStringWithShadow(poseStack, string, n11 + n10, f3, n13);
        String string2 = this.modeSetting.getValue();
        float f4 = (float)(n11 + n12) - FontStore.OPENSANS_16.getStringWidth(string2) - (float)n10;
        FontStore.OPENSANS_16.drawStringWithShadow(poseStack, string2, f4, f3, new Color(138, 180, 248).getRGB());
        if (this.dropdownOpen) {
            int n14 = n3 + n7 + n8;
            int n15 = this.getDropdownHeight();
            RenderUtil.drawFilledRect(poseStack, n11, n14, n12, n15, new Color(33, 33, 33, 150).getRGB());
            String[] stringArray = this.getModes();
            for (int i = 0; i < stringArray.length; ++i) {
                boolean bl2;
                String string3 = stringArray[i];
                float f5 = n14 + i * n8;
                boolean bl3 = bl2 = n >= n11 && n <= n11 + n12 && (float)n2 >= f5 && (float)n2 < f5 + (float)n8;
                if (bl2) {
                    RenderUtil.drawFilledRect(poseStack, n11, f5, n12, n8, new Color(0, 0, 0, 100).getRGB());
                }
                float f6 = FontStore.OPENSANS_16.getStringWidth(string3);
                float f7 = (float)n11 + ((float)n12 - f6) / 2.0f;
                float f8 = f5 + ((float)n8 - FontStore.OPENSANS_16.getFontHeight()) / 2.0f;
                boolean bl4 = string3.equals(this.modeSetting.getValue());
                FontStore.OPENSANS_16.drawStringWithShadow(poseStack, string3, f7, f8, bl4 ? new Color(138, 180, 248).getRGB() : n13);
            }
        }
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        int n2;
        int n3 = this.parentButton.panel.y + this.parentButton.yOffset + this.parentButton.panel.rowHeight + this.yOffset + 3;
        int n4 = this.parentButton.panel.rowHeight;
        int n5 = 4;
        int n6 = n4 - n5 * 2;
        int n7 = this.parentButton.panel.x;
        int n8 = this.parentButton.panel.width;
        int n9 = 8;
        int n10 = 4;
        int n11 = n7 + n9 + n10;
        int n12 = n8 - (n9 + n10) * 2;
        String[] stringArray = this.modeSetting.getModes();
        if (this.dropdownOpen) {
            n2 = n3 + n5 + n6;
            int n13 = this.getDropdownHeight();
            if (d >= (double)n11 && d <= (double)(n11 + n12) && d2 >= (double)n2 && d2 < (double)(n2 + n13)) {
                int n14 = (int)((d2 - (double)n2) / (double)n6);
                if (n14 >= 0 && n14 < stringArray.length) {
                    this.modeSetting.setValue(stringArray[n14]);
                    this.dropdownOpen = false;
                    this.parentButton.panel.recalcLayout();
                }
                return;
            }
        }
        int n15 = n2 = d >= (double)n11 && d <= (double)(n11 + n12) && d2 >= (double)(n3 + n5) && d2 <= (double)(n3 + n5 + n6) ? 1 : 0;
        if (n2 != 0 && (n == 0 || n == 1)) {
            this.dropdownOpen = !this.dropdownOpen;
            this.parentButton.panel.recalcLayout();
        }
    }

    @Override
    public boolean isHovered(double d, double d2) {
        boolean bl;
        int n = this.parentButton.panel.y + this.parentButton.yOffset + this.parentButton.panel.rowHeight + this.yOffset;
        int n2 = this.parentButton.panel.x;
        int n3 = this.parentButton.panel.width;
        int n4 = this.parentButton.panel.rowHeight;
        boolean bl2 = bl = d >= (double)n2 && d <= (double)(n2 + n3) && d2 >= (double)n && d2 <= (double)(n + n4);
        if (bl) {
            return true;
        }
        if (this.dropdownOpen) {
            int n5 = n + n4;
            int n6 = this.getDropdownHeight();
            return d >= (double)n2 && d <= (double)(n2 + n3) && d2 >= (double)n5 && d2 <= (double)(n5 + n6);
        }
        return false;
    }

    @Override
    public void mouseReleased(double d, double d2, int n) {
        super.mouseReleased(d, d2, n);
    }

    public int getDropdownHeight() {
        int n = 4;
        int n2 = this.parentButton.panel.rowHeight - n * 2;
        return n2 * this.modeSetting.getModes().length;
    }

    public String[] getModes() {
        return this.modeSetting.getModes();
    }

    }