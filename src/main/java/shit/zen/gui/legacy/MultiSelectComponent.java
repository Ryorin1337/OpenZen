package shit.zen.gui.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.List;
import lombok.Getter;
import shit.zen.gui.legacy.ModuleButton;
import shit.zen.gui.legacy.SettingComponent;
import shit.zen.render.FontStore;
import shit.zen.settings.Setting;
import shit.zen.settings.impl.MultiSelectSetting;
import shit.zen.utils.render.RenderUtil;

public class MultiSelectComponent
extends SettingComponent {
    private final MultiSelectSetting multiSelectSetting;
    @Getter
    private boolean dropdownOpen;

    public MultiSelectComponent(Setting<?> setting, ModuleButton moduleButton, int n) {
        super(setting, moduleButton, n);
        this.multiSelectSetting = (MultiSelectSetting)setting;
    }

    @Override
    public void renderWithAlpha(PoseStack poseStack, int n, int n2, float f, float f2) {
        int n3 = this.parentButton.panel.y + this.parentButton.yOffset + this.parentButton.panel.rowHeight + this.yOffset;
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
        int n14 = new Color(138, 180, 248, (int)(255.0f * f2)).getRGB();
        int n15 = n8;
        if (this.dropdownOpen) {
            n15 += this.getDropdownHeight();
        }
        boolean bl = n >= n11 && n <= n11 + n12 && n2 >= n3 + n7 && n2 <= n3 + n7 + n8;
        Color color = bl && !this.dropdownOpen ? new Color(40, 40, 40, (int)(180.0f * f2)) : new Color(15, 15, 15, (int)(220.0f * f2));
        RenderUtil.drawFilledRect(poseStack, n11, n3 + n7, n12, n15, color.getRGB());
        Color color2 = new Color(100, 100, 100, (int)(180.0f * f2));
        RenderUtil.drawFilledRect(poseStack, n4 + n9 + 2, n3 + n7, n10 - 2, n8, color2.getRGB());
        RenderUtil.drawFilledRect(poseStack, n4 + n5 - n9 - n10, n3 + n7, n10 - 2, n8, color2.getRGB());
        String string = this.multiSelectSetting.getName();
        float f3 = FontStore.OPENSANS_16.getStringWidth(string);
        float f4 = (float)n11 + ((float)n12 - f3) / 2.0f;
        float f5 = (float)(n3 + n7) + ((float)n8 - FontStore.OPENSANS_16.getFontHeight()) / 2.0f - 1.0f;
        FontStore.OPENSANS_16.drawStringWithShadow(poseStack, string, f4, f5, n13);
        if (this.dropdownOpen) {
            int n16 = n3 + n7 + n8;
            List<String> list = this.multiSelectSetting.getOptions();
            for (int i = 0; i < list.size(); ++i) {
                boolean bl2;
                String string2 = list.get(i);
                float f6 = n16 + i * n8;
                boolean bl3 = bl2 = n >= n11 && n <= n11 + n12 && (float)n2 >= f6 && (float)n2 < f6 + (float)n8;
                if (bl2) {
                    RenderUtil.drawFilledRect(poseStack, n11, f6, n12, n8, new Color(0, 0, 0, 100).getRGB());
                }
                float f7 = FontStore.OPENSANS_16.getStringWidth(string2);
                float f8 = (float)n11 + ((float)n12 - f7) / 2.0f;
                float f9 = f6 + ((float)n8 - FontStore.OPENSANS_16.getFontHeight()) / 2.0f;
                boolean bl4 = this.multiSelectSetting.isSelected(string2);
                FontStore.OPENSANS_16.drawStringWithShadow(poseStack, string2, f8, f9, bl4 ? n14 : n13);
            }
        }
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        if (this.isHeaderHovered(d, d2) && n == 1) {
            this.dropdownOpen = !this.dropdownOpen;
            this.parentButton.panel.recalcLayout();
            return;
        }
        if (this.dropdownOpen) {
            int n2 = this.parentButton.panel.y + this.parentButton.yOffset + this.parentButton.panel.rowHeight + this.yOffset;
            int n3 = this.parentButton.panel.rowHeight;
            int n4 = n2 + n3;
            int n5 = 4;
            int n6 = n3 - n5 * 2;
            int n7 = this.parentButton.panel.x;
            int n8 = this.parentButton.panel.width;
            int n9 = 8;
            int n10 = 4;
            int n11 = n7 + n9 + n10;
            int n12 = n8 - (n9 + n10) * 2;
            if (d >= (double)n11 && d <= (double)(n11 + n12) && d2 >= (double)n4) {
                int n13 = (int)((d2 - (double)n4) / (double)n6);
                List<String> list = this.multiSelectSetting.getOptions();
                if (n13 >= 0 && n13 < list.size()) {
                    String string = list.get(n13);
                    if (this.multiSelectSetting.isSelected(string)) {
                        this.multiSelectSetting.getValue().remove(string);
                    } else {
                        this.multiSelectSetting.getValue().add(string);
                    }
                }
            }
        }
    }

    public int getDropdownHeight() {
        if (!this.dropdownOpen) {
            return 0;
        }
        int n = 4;
        int n2 = this.parentButton.panel.rowHeight - n * 2;
        return n2 * this.multiSelectSetting.getOptions().size();
    }

    private boolean isHeaderHovered(double d, double d2) {
        int n = this.parentButton.panel.y + this.parentButton.yOffset + this.parentButton.panel.rowHeight + this.yOffset;
        return d >= (double)this.parentButton.panel.x && d <= (double)(this.parentButton.panel.x + this.parentButton.panel.width) && d2 >= (double)n && d2 <= (double)(n + this.parentButton.panel.rowHeight);
    }

    @Override
    public boolean isHovered(double d, double d2) {
        if (this.isHeaderHovered(d, d2)) {
            return true;
        }
        if (this.dropdownOpen) {
            int n = this.parentButton.panel.y + this.parentButton.yOffset + this.parentButton.panel.rowHeight + this.yOffset;
            int n2 = n + this.parentButton.panel.rowHeight;
            int n3 = this.getDropdownHeight();
            int n4 = this.parentButton.panel.x;
            int n5 = this.parentButton.panel.width;
            int n6 = 8;
            int n7 = 4;
            int n8 = n4 + n6 + n7;
            int n9 = n5 - (n6 + n7) * 2;
            return d >= (double)n8 && d <= (double)(n8 + n9) && d2 >= (double)n2 && d2 <= (double)(n2 + n3);
        }
        return false;
    }

    }