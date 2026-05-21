package shit.zen.gui.newclickgui;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import shit.zen.gui.NewClickGui;
import shit.zen.gui.newclickgui.CategoryPanel;
import shit.zen.gui.newclickgui.SettingElement;
import shit.zen.render.FontStore;
import shit.zen.settings.impl.BooleanSetting;
import shit.zen.utils.animation.SmoothAnimationTimer;
import shit.zen.utils.math.Easings;
import shit.zen.utils.misc.CursorUtil;
import shit.zen.utils.render.ColorUtil;
import shit.zen.utils.render.RenderUtil;

public class BooleanSettingElement
extends SettingElement<BooleanSetting> {
    @Getter @Setter
    private boolean isTruncated;
    @Getter @Setter
    private boolean isHovered;
    @Getter
    private final SmoothAnimationTimer toggleTimer = new SmoothAnimationTimer();
    private static final String ELLIPSIS = "...";

    public BooleanSettingElement(CategoryPanel categoryPanel, BooleanSetting booleanSetting) {
        super(categoryPanel, booleanSetting);
    }

    @Override
    public void render(NewClickGui newClickGui, GuiGraphics guiGraphics, PoseStack poseStack, int n, int n2, float f, float f2) {
        this.isHovered = CursorUtil.isInBounds(n, n2, this.x, this.y, 120.0f, this.getHeight());
        this.visibilityTimer.animate(this.setting.getVisibility().displayable() ? 1.0 : 0.0, 0.2, Easings.EASE_OUT_POW2);
        this.visibilityTimer.tick();
        if (Mth.equal(f *= this.visibilityTimer.getValueF(), 0.0f)) {
            return;
        }
        String string = this.setting.getName();
        if (FontStore.AXIFORMA_REGULAR_14.getStringWidth(string) > 90.0f) {
            string = string.substring(0, 10);
            string = string + ELLIPSIS;
            this.isTruncated = true;
        }
        this.toggleTimer.animate(this.setting.getValue() != false ? 1.0 : 0.0, 0.2, Easings.EASE_OUT_POW2);
        this.toggleTimer.tick();
        if (Mth.equal(f, 0.0f)) {
            return;
        }
        poseStack.pushPose();
        FontStore.AXIFORMA_REGULAR_14.drawString(poseStack, string, this.x + 6.0f, this.y + (this.getHeight() - FontStore.AXIFORMA_REGULAR_14.getFontHeight()) / 2.0f, ColorUtil.withAlpha(-1, f * 0.8f));
        float f3 = this.toggleTimer.getValueF();
        if (f3 > 0.0f) {
            RenderUtil.drawShadow(poseStack, this.x + 120.0f - 20.0f - 6.0f, this.y + (this.getHeight() - 10.0f) / 2.0f, 20.0f, 10.0f, 12, ColorUtil.withAlpha(-13768502, 0.36f * f * f3));
        }
        RenderUtil.drawRoundedRect(poseStack, this.x + 120.0f - 20.0f - 6.0f, this.y + (this.getHeight() - 10.0f) / 2.0f, 20.0f, 10.0f, 4.0f, ColorUtil.withAlpha(ColorUtil.fromRGB(60, 60, 60), f));
        if (f3 > 0.0f) {
            RenderUtil.drawRoundedRect(poseStack, this.x + 120.0f - 20.0f - 6.0f, this.y + (this.getHeight() - 10.0f) / 2.0f, 10.0f + 10.0f * f3, 10.0f, 4.0f, ColorUtil.withAlpha(CategoryPanel.ACCENT_COLOR, f * f3));
        }
        RenderUtil.drawRoundedRect(poseStack, this.x + 120.0f - 20.0f - 6.0f + 10.0f * f3, this.y + (this.getHeight() - 10.0f) / 2.0f, 10.0f, 10.0f, 4.8f, ColorUtil.withAlpha(-1, f));
        poseStack.popPose();
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
    public float getHeight() {
        return 18.0f;
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (this.isHovered && CursorUtil.isInBounds((float)d, (float)d2, this.x + 120.0f - 20.0f - 6.0f, this.y + (this.getHeight() - 10.0f) / 2.0f, 20.0f, 10.0f)) {
            this.setting.setValue(this.setting.getValue() == false);
            return true;
        }
        return false;
    }

}