package shit.zen.gui.newclickgui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import shit.zen.gui.NewClickGui;
import shit.zen.gui.newclickgui.CategoryPanel;
import shit.zen.gui.newclickgui.SettingElement;
import shit.zen.render.FontStore;
import shit.zen.settings.impl.ModeSetting;
import shit.zen.utils.animation.SmoothAnimationTimer;
import shit.zen.utils.math.Easings;
import shit.zen.utils.misc.CursorUtil;
import shit.zen.utils.render.ColorUtil;
import shit.zen.utils.render.RenderUtil;

public class ModeSettingElement
extends SettingElement<ModeSetting> {
    private final SmoothAnimationTimer hoverTimer = new SmoothAnimationTimer();
    private final SmoothAnimationTimer visTimer = new SmoothAnimationTimer();
    private final SmoothAnimationTimer highlightTimer = new SmoothAnimationTimer();
    private final SmoothAnimationTimer highlightYTimer = new SmoothAnimationTimer();
    private boolean isDropdownHovered;
    private boolean isOpen;
    private String hoveredMode;

    public ModeSettingElement(CategoryPanel categoryPanel, ModeSetting modeSetting) {
        super(categoryPanel, modeSetting);
    }

    @Override
    public void render(NewClickGui newClickGui, GuiGraphics guiGraphics, PoseStack poseStack, int n, int n2, float f, float f2) {
        float f3;
        float f4;
        float f5 = 36.0f;
        float f6 = this.y + f5 / 2.0f + 2.0f - 2.0f;
        float f7 = 108.0f;
        float f8 = 14.0f;
        this.hoveredMode = null;
        this.isDropdownHovered = CursorUtil.isInBounds(n, n2, this.x + 6.0f, f6, f7, f8);
        this.hoverTimer.animate(this.isDropdownHovered ? 1.0 : 0.0, 0.22, Easings.EASE_OUT_POW2);
        this.hoverTimer.tick();
        this.visibilityTimer.animate(this.setting.getVisibility().displayable() ? 1.0 : 0.0, 0.2, Easings.EASE_OUT_POW2);
        this.visibilityTimer.tick();
        this.visTimer.animate(this.isOpen ? 1.0 : 0.0, 0.2, Easings.EASE_OUT_POW2);
        this.visTimer.tick();
        if (Mth.equal(f *= this.visibilityTimer.getValueF(), 0.0f)) {
            return;
        }
        float f9 = this.y + (f5 / 2.0f - FontStore.AXIFORMA_REGULAR_14.getFontHeight()) / 2.0f + 1.0f;
        FontStore.AXIFORMA_REGULAR_14.drawString(poseStack, this.setting.getName(), this.x + 6.0f, f9, ColorUtil.withAlpha(-1, f * 0.8f));
        float f10 = this.visTimer.getValueF();
        if (f10 > 0.0f) {
            f4 = f8 + (float) this.setting.getModes().length * f8 * f10;
            RenderUtil.drawRoundedRect(poseStack, this.x + 6.0f, f6, f7, f4, 3.0f, ColorUtil.withAlpha(ColorUtil.fromRGB(60, 60, 60), f * f10));
            f3 = (f8 - FontStore.AXIFORMA_BOLD_13.getFontHeight()) / 2.0f;
            float f11 = f6 + f3 + f8;
            for (String string : this.setting.getModes()) {
                if (CursorUtil.isInBounds(n, n2, this.x + 6.0f, f11 - f3, f7, f8)) {
                    this.hoveredMode = string;
                    this.highlightYTimer.animate(f11 - f3, 0.2, Easings.EASE_OUT_POW2);
                }
                if (f6 + f4 > f11 + FontStore.AXIFORMA_BOLD_13.getFontHeight()) {
                    FontStore.AXIFORMA_BOLD_13.drawStringCentered(poseStack, string, this.x + 60.0f, f11, ColorUtil.withAlpha(-1, f * 0.8f * f10));
                }
                f11 += f8;
            }
        }
        this.highlightTimer.animate(this.hoveredMode == null || !this.isOpen ? 0.0 : 1.0, 0.18, Easings.EASE_OUT_POW2);
        this.highlightTimer.tick();
        this.highlightYTimer.tick();
        f4 = this.highlightTimer.getValueF();
        if (f4 > 0.0f) {
            RenderUtil.drawRoundedRect(poseStack, this.x + 6.0f, this.highlightYTimer.getValueF(), f7, f8, 3.0f, ColorUtil.withAlpha(ColorUtil.fromRGB(255, 255, 255), f * f4 * 1.0f * 0.1f));
        }
        f3 = this.hoverTimer.getValueF();
        RenderUtil.drawRoundedRect(poseStack, this.x + 6.0f, f6, f7, f8, 3.0f, ColorUtil.withAlpha(ColorUtil.fromRGB((int)(60.0f + 30.0f * f3), (int)(60.0f + 30.0f * f3), (int)(60.0f + 30.0f * f3)), f));
        FontStore.AXIFORMA_BOLD_13.drawStringCentered(poseStack, this.setting.getValue(), this.x + 60.0f, f6 + (f8 - FontStore.AXIFORMA_BOLD_13.getFontHeight()) / 2.0f, ColorUtil.withAlpha(-1, f * 0.8f));
        String string = String.valueOf('\ueb5d');
        FontStore.MATERIAL_20.drawStringCentered(poseStack, string, this.x + 6.0f + f7 - FontStore.MATERIAL_20.getStringWidth(string) + 2.0f, f6 + (f8 - FontStore.MATERIAL_20.getFontHeight()) / 2.0f + 0.5f, ColorUtil.withAlpha(-1, f * 0.8f));
    }

    @Override
    public float getHeight() {
        return 36 + (this.isOpen ? 14 * this.setting.getModes().length : 0);
    }

    @Override
    public float getAnimatedHeight() {
        return 36.0f + (float)(14 * this.setting.getModes().length) * this.visTimer.getValueF();
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (this.isDropdownHovered) {
            this.isOpen = !this.isOpen;
            return true;
        }
        if (this.hoveredMode != null && this.isOpen) {
            this.setting.setValue(this.hoveredMode);
            this.isOpen = false;
            this.hoveredMode = null;
            return true;
        }
        return false;
    }
}