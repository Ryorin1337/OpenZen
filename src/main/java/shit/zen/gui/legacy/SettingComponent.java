package shit.zen.gui.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import shit.zen.gui.legacy.ModuleButton;
import shit.zen.settings.Setting;

public class SettingComponent {
    public Setting<?> setting;
    public ModuleButton parentButton;
    public int yOffset;

    public SettingComponent(Setting<?> setting, ModuleButton moduleButton, int n) {
        this.setting = setting;
        this.parentButton = moduleButton;
        this.yOffset = n;
    }

    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderWithAlpha(poseStack, n, n2, f, 1.0f);
    }

    public void renderWithAlpha(PoseStack poseStack, int n, int n2, float f, float f2) {
    }

    public void mouseClicked(double d, double d2, int n) {
    }

    public void mouseReleased(double d, double d2, int n) {
    }

    public boolean isHovered(double d, double d2) {
        int n = this.parentButton.panel.y + this.parentButton.yOffset + this.parentButton.panel.rowHeight + this.yOffset;
        int n2 = this.parentButton.panel.rowHeight;
        return d >= (double)this.parentButton.panel.x && d <= (double)(this.parentButton.panel.x + this.parentButton.panel.width) && d2 >= (double)n && d2 <= (double)(n + n2);
    }
}