package shit.zen.gui.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import shit.zen.gui.legacy.BooleanComponent;
import shit.zen.gui.legacy.CategoryPanel;
import shit.zen.gui.legacy.ModeComponent;
import shit.zen.gui.legacy.MultiSelectComponent;
import shit.zen.gui.legacy.NumberComponent;
import shit.zen.gui.legacy.SettingComponent;
import shit.zen.modules.Module;
import shit.zen.render.FontStore;
import shit.zen.settings.Setting;
import shit.zen.settings.impl.BooleanSetting;
import shit.zen.settings.impl.ModeSetting;
import shit.zen.settings.impl.MultiSelectSetting;
import shit.zen.settings.impl.NumberSetting;
import shit.zen.utils.animation.SmoothAnimationTimer;
import shit.zen.utils.math.Easings;
import shit.zen.utils.render.ColorUtil;
import shit.zen.utils.render.RenderUtil;

public class ModuleButton {
    public Module module;
    public CategoryPanel panel;
    public int yOffset;
    public List<SettingComponent> settingComponents;
    public boolean expanded;
    private final SmoothAnimationTimer expandAnim;
    private float hoverProgress = 0.0f;
    private final float hoverSpeed = 4.0f;
    private long lastTime = System.currentTimeMillis();

    public ModuleButton(Module module, CategoryPanel categoryPanel, int n) {
        this.module = module;
        this.panel = categoryPanel;
        this.yOffset = n;
        this.expanded = false;
        this.settingComponents = new ArrayList<>();
        for (Setting setting : module.getSettings()) {
            if (setting instanceof BooleanSetting) {
                this.settingComponents.add(new BooleanComponent(setting, this, 0));
                continue;
            }
            if (setting instanceof ModeSetting) {
                this.settingComponents.add(new ModeComponent(setting, this, 0));
                continue;
            }
            if (setting instanceof NumberSetting) {
                this.settingComponents.add(new NumberComponent(setting, this, 0));
                continue;
            }
            if (!(setting instanceof MultiSelectSetting)) continue;
            this.settingComponents.add(new MultiSelectComponent(setting, this, 0));
        }
        this.expandAnim = new SmoothAnimationTimer();
    }

    public int getTotalHeight() {
        if (!this.expanded) {
            return this.panel.rowHeight;
        }
        int n = this.panel.rowHeight;
        List<SettingComponent> list = this.settingComponents.stream().filter(settingComponent -> settingComponent.setting.getVisibility().displayable()).collect(Collectors.toList());
        for (SettingComponent settingComponent2 : list) {
            n += this.panel.rowHeight;
            if (settingComponent2 instanceof ModeComponent mode && mode.isDropdownOpen()) {
                n += mode.getDropdownHeight();
            }
            if (settingComponent2 instanceof MultiSelectComponent multi && multi.isDropdownOpen()) {
                n += multi.getDropdownHeight();
            }
        }
        return n;
    }

    public int getExpandedHeight() {
        int n = 0;
        List<SettingComponent> list = this.settingComponents.stream().filter(settingComponent -> settingComponent.setting.getVisibility().displayable()).collect(Collectors.toList());
        for (SettingComponent settingComponent2 : list) {
            n += this.panel.rowHeight;
            if (settingComponent2 instanceof ModeComponent mode && mode.isDropdownOpen()) {
                n += mode.getDropdownHeight();
            }
            if (settingComponent2 instanceof MultiSelectComponent multi && multi.isDropdownOpen()) {
                n += multi.getDropdownHeight();
            }
        }
        return n;
    }

    public void render(PoseStack poseStack, int n, int n2, float f) {
        float f2;
        this.expandAnim.tick();
        long l = System.currentTimeMillis();
        float f3 = (float)(l - this.lastTime) / 1000.0f;
        this.lastTime = l;
        if (this.isHovered(n, n2)) {
            if (this.hoverProgress < 1.0f) {
                this.hoverProgress += f3 * 4.0f;
            }
            if (this.hoverProgress > 1.0f) {
                this.hoverProgress = 1.0f;
            }
        } else {
            if (this.hoverProgress > 0.0f) {
                this.hoverProgress -= f3 * 4.0f;
            }
            if (this.hoverProgress < 0.0f) {
                this.hoverProgress = 0.0f;
            }
        }
        int n3 = (int)(160.0f + 40.0f * this.hoverProgress);
        int n4 = this.panel.y + this.yOffset - (this.yOffset == 0 ? 0 : 1);
        int n5 = this.panel.rowHeight + (this.yOffset == 0 ? 0 : 1);
        RenderUtil.drawBlurredRect(poseStack, this.panel.x, n4, this.panel.width, n5, 4.0f, 6.0f, 0.9f, 0);
        RenderUtil.drawFilledRect(poseStack, this.panel.x, n4, this.panel.width, n5, new Color(21, 21, 21, n3).getRGB());
        String string = this.module.getName();
        float f4 = FontStore.OPENSANS_16.getStringWidth(string);
        float f5 = (float)this.panel.x + (float)this.panel.width / 2.0f - f4 / 2.0f;
        float f6 = (float)(this.panel.y + this.yOffset) + (float)this.panel.rowHeight / 2.0f - FontStore.OPENSANS_16.getFontHeight() / 2.0f;
        FontStore.OPENSANS_16.drawStringWithShadow(poseStack, string, f5, f6, this.module.isEnabled() ? ColorUtil.fromARGB(138, 180, 248, 255) : -1);
        if (!this.module.getSettings().isEmpty()) {
            poseStack.pushPose();
            f2 = this.panel.x + this.panel.width - 15;
            float f7 = (float)(this.panel.y + this.yOffset) + (float)this.panel.rowHeight / 2.0f;
            float f8 = 180.0f * this.expandAnim.getValueF();
            poseStack.translate(f2, f7, 0.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(f8));
            poseStack.translate(-f2, -f7, 0.0f);
            FontStore.MATERIAL_20.drawStringWithShadow(poseStack, "", f2 - FontStore.MATERIAL_20.getStringWidth("") / 2.0f, f7 - FontStore.MATERIAL_20.getFontHeight() / 2.0f, -1);
            poseStack.popPose();
        }
        if ((f2 = (float)this.getExpandedHeight() * this.expandAnim.getValueF()) > 0.0f) {
            int n6 = this.panel.y + this.yOffset + this.panel.rowHeight - 1;
            RenderUtil.drawBlurredRect(poseStack, this.panel.x, n6, this.panel.width, (int)f2 + 1, 4.0f, 6.0f, 0.9f, 0);
            RenderUtil.pushScissor(this.panel.x, n6, this.panel.width, (int)f2 + 1);
            RenderUtil.drawFilledRect(poseStack, this.panel.x, n6, this.panel.width, (int)f2 + 1, new Color(11, 11, 11, 150).getRGB());
            List<SettingComponent> list = this.settingComponents.stream().filter(settingComponent -> settingComponent.setting.getVisibility().displayable()).collect(Collectors.toList());
            int n7 = 0;
            for (SettingComponent settingComponent2 : list) {
                SettingComponent settingComponent3;
                settingComponent2.yOffset = n7;
                settingComponent2.renderWithAlpha(poseStack, n, n2, f, 1.0f);
                n7 += this.panel.rowHeight;
                if (settingComponent2 instanceof ModeComponent && ((ModeComponent)(settingComponent3 = settingComponent2)).isDropdownOpen()) {
                    n7 += ((ModeComponent)settingComponent3).getDropdownHeight();
                }
                if (!(settingComponent2 instanceof MultiSelectComponent) || !((MultiSelectComponent)(settingComponent3 = settingComponent2)).isDropdownOpen()) continue;
                n7 += ((MultiSelectComponent)settingComponent3).getDropdownHeight();
            }
            RenderUtil.popScissor();
        }
    }

    public void mouseClicked(double d, double d2, int n) {
        if (this.isHovered(d, d2)) {
            if (n == 0) {
                this.module.setEnabled(!this.module.isEnabled());
            } else if (n == 1 && !this.module.getSettings().isEmpty()) {
                this.expanded = !this.expanded;
                this.expandAnim.animate(this.expanded ? 1.0 : 0.0, 0.2, Easings.EASE_OUT_QUAD);
                this.panel.recalcLayout();
            }
        }
        if (this.expanded) {
            List<SettingComponent> list = this.settingComponents.stream().filter(settingComponent -> settingComponent.setting.getVisibility().displayable()).collect(Collectors.toList());
            int n2 = 0;
            for (SettingComponent settingComponent2 : list) {
                SettingComponent settingComponent3;
                settingComponent2.yOffset = n2;
                if (settingComponent2.isHovered(d, d2)) {
                    settingComponent2.mouseClicked(d, d2, n);
                }
                n2 += this.panel.rowHeight;
                if (settingComponent2 instanceof ModeComponent && ((ModeComponent)(settingComponent3 = settingComponent2)).isDropdownOpen()) {
                    n2 += ((ModeComponent)settingComponent3).getDropdownHeight();
                }
                if (!(settingComponent2 instanceof MultiSelectComponent) || !((MultiSelectComponent)(settingComponent3 = settingComponent2)).isDropdownOpen()) continue;
                n2 += ((MultiSelectComponent)settingComponent3).getDropdownHeight();
            }
        }
    }

    public void mouseReleased(double d, double d2, int n) {
        if (this.expanded) {
            for (SettingComponent settingComponent : this.settingComponents) {
                settingComponent.mouseReleased(d, d2, n);
            }
        }
    }

    public void mouseScrolled(double d, double d2, double d3) {
    }

    public void reset() {
    }

    public boolean isHovered(double d, double d2) {
        return d >= (double)this.panel.x && d <= (double)(this.panel.x + this.panel.width) && d2 >= (double)(this.panel.y + this.yOffset) && d2 <= (double)(this.panel.y + this.yOffset + this.panel.rowHeight);
    }

    public boolean isAnimating() {
        return !this.expandAnim.isDone();
    }
}