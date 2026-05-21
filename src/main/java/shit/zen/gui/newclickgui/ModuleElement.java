package shit.zen.gui.newclickgui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphics;
import shit.zen.gui.NewClickGui;
import shit.zen.gui.newclickgui.BooleanSettingElement;
import shit.zen.gui.newclickgui.CategoryPanel;
import shit.zen.gui.newclickgui.ModeSettingElement;
import shit.zen.gui.newclickgui.MultiSelectSettingElement;
import shit.zen.gui.newclickgui.NumberSettingElement;
import shit.zen.gui.newclickgui.SettingElement;
import shit.zen.gui.newclickgui.UIElement;
import shit.zen.modules.Module;
import shit.zen.render.FontStore;
import shit.zen.settings.Setting;
import shit.zen.settings.impl.BooleanSetting;
import shit.zen.settings.impl.ModeSetting;
import shit.zen.settings.impl.MultiSelectSetting;
import shit.zen.settings.impl.NumberSetting;
import shit.zen.utils.animation.SmoothAnimationTimer;
import shit.zen.utils.math.Easings;
import shit.zen.utils.misc.CursorUtil;
import shit.zen.utils.render.ColorUtil;
import shit.zen.utils.render.RenderHelper;
import shit.zen.utils.render.RenderUtil;

public class ModuleElement
extends UIElement {
    public static final int BG_COLOR;
    @Getter
    private final List<SettingElement<?>> settingElements = new ArrayList<>();
    @Getter
    private final CategoryPanel parentPanel;
    @Getter
    private final Module module;
    @Getter
    private final SmoothAnimationTimer enabledTimer = new SmoothAnimationTimer();
    private final SmoothAnimationTimer hoveredTimer = new SmoothAnimationTimer();
    private final SmoothAnimationTimer expandTimer = new SmoothAnimationTimer();
    private final SmoothAnimationTimer settingsHeightTimer = new SmoothAnimationTimer();
    private float posX;
    private float posY;
    private float totalHeight = 20.0f;
    @Getter @Setter
    private float scrollOffset;
    @Getter @Setter
    private boolean isHovered;
    @Getter @Setter
    private boolean isExpanded;
    private static final String BUILD_TAG;

    public ModuleElement(CategoryPanel categoryPanel, Module module) {
        System.out.println(BUILD_TAG);
        this.parentPanel = categoryPanel;
        this.module = module;
        for (Setting setting : module.getSettings()) {
            if (setting instanceof BooleanSetting booleanSetting) {
                this.settingElements.add(new BooleanSettingElement(categoryPanel, booleanSetting));
                continue;
            }
            if (setting instanceof ModeSetting modeSetting) {
                this.settingElements.add(new ModeSettingElement(categoryPanel, modeSetting));
                continue;
            }
            if (setting instanceof MultiSelectSetting multiSelectSetting) {
                this.settingElements.add(new MultiSelectSettingElement(categoryPanel, multiSelectSetting));
                continue;
            }
            if (!(setting instanceof NumberSetting numberSetting)) continue;
            this.settingElements.add(new NumberSettingElement(categoryPanel, numberSetting));
        }
    }

    @Override
    public void render(NewClickGui newClickGui, GuiGraphics guiGraphics, PoseStack poseStack, int n, int n2, float f, float f2) {
        float f3;
        float f4;
        float f5;
        float f6 = 0.0f;
        for (SettingElement settingElement : this.settingElements) {
            if (!settingElement.getSetting().getVisibility().displayable()) continue;
            f6 += settingElement.getHeight();
        }
        this.settingsHeightTimer.animate(f6, 0.2, Easings.EASE_OUT_POW2);
        this.settingsHeightTimer.tick();
        this.parentPanel.setCollapsed(!this.settingsHeightTimer.isDone());
        this.hoveredTimer.animate(this.isHovered ? 1.0 : 0.0, 0.2, Easings.EASE_OUT_POW2);
        this.hoveredTimer.tick();
        this.enabledTimer.animate(this.module.isEnabled() ? 1.0 : 0.0, 0.3, Easings.EASE_OUT_POW2);
        this.enabledTimer.tick();
        this.expandTimer.animate(this.isExpanded ? 1.0 : 0.0, 0.2, Easings.EASE_OUT_POW3);
        this.expandTimer.tick();
        float f7 = this.expandTimer.getValueF();
        this.totalHeight = 20.0f + f7 * this.settingsHeightTimer.getValueF();
        this.isHovered = this.parentPanel.equals(NewClickGui.focusedPanel) && CursorUtil.isInBounds(n, n2, this.posX, this.posY, 120.0f, this.totalHeight);
        RenderUtil.drawFilledRect(poseStack, this.posX + 1.0f, this.posY + 20.0f, 118.0f, this.totalHeight - 20.0f, ColorUtil.withAlpha(BG_COLOR, f7 * f));
        float f8 = this.hoveredTimer.getValueF();
        if (f8 > 0.0f) {
            RenderUtil.drawFilledRect(poseStack, this.posX + 0.5f, this.posY, 119.0f, 20.0f, ColorUtil.withAlpha(-1, 0.1f * f * f8));
        }
        if (1.0f - (f5 = this.enabledTimer.getValueF()) > 0.0f) {
            FontStore.AXIFORMA_REGULAR_16.drawStringCentered(poseStack, this.module.getName(), this.posX + 60.0f, this.posY + (20.0f - FontStore.AXIFORMA_REGULAR_16.getFontHeight()) / 2.0f, ColorUtil.withAlpha(-1, f * (1.0f - f5) * 0.6f));
        }
        if (f5 > 0.0f) {
            f4 = FontStore.AXIFORMA_BOLD_16.getStringWidth(this.module.getName());
            f3 = this.posY + (20.0f - FontStore.AXIFORMA_BOLD_16.getFontHeight()) / 2.0f;
            RenderUtil.drawShadow(poseStack, this.posX + (120.0f - f4) / 2.0f, f3 + FontStore.AXIFORMA_BOLD_16.getFontHeight() / 4.0f, f4, FontStore.AXIFORMA_BOLD_16.getFontHeight() / 2.0f, 12, ColorUtil.withAlpha(-13768502, f * f5 * 0.36f));
            FontStore.AXIFORMA_BOLD_16.drawStringCentered(poseStack, this.module.getName(), this.posX + 60.0f, f3, ColorUtil.withAlpha(-13768502, f * f5));
        }
        if (!this.module.getSettings().isEmpty()) {
            String string = String.valueOf('\ueb4e');
            f3 = FontStore.MATERIAL_20.getStringWidth(string);
            float f9 = this.posX + 120.0f - f3 - 6.0f;
            float f10 = this.posY + (20.0f - FontStore.MATERIAL_20.getFontHeight()) / 2.0f + 1.0f;
            RenderHelper.pushRotateAround(poseStack, f9 + f3 / 2.0f, f10 + FontStore.MATERIAL_20.getFontHeight() / 2.0f - 1.0f, 180.0f * f7);
            FontStore.MATERIAL_20.drawString(poseStack, string, f9, f10, ColorUtil.withAlpha(-1, (0.8f - 0.3f * f7) * f));
            RenderHelper.popPose(poseStack);
        }
        if (this.isExpanded) {
            f4 = this.posY + 20.0f;
            for (SettingElement settingElement : this.settingElements) {
                settingElement.setX(this.posX);
                settingElement.setY(f4);
                settingElement.render(newClickGui, guiGraphics, poseStack, n, n2, f * f7, f2);
                f4 += settingElement.getAnimatedHeight() * settingElement.getVisibilityTimer().getValueF();
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isHovered) {
            return false;
        }
        if (CursorUtil.isInBounds((float) mouseX, (float) mouseY, this.posX, this.posY, 120.0f, 20.0f)) {
            if (button == 0) {
                this.module.setEnabled(!this.module.isEnabled());
            } else if (button == 1 && !this.module.getSettings().isEmpty()) {
                this.isExpanded = !this.isExpanded;
            }
            return true;
        }
        if (CursorUtil.isInBounds((float) mouseX, (float) mouseY, this.posX, this.posY + 20.0f, 120.0f, this.totalHeight - 20.0f)) {
            Iterator<SettingElement<?>> iterator = this.settingElements.iterator();
            while (iterator.hasNext() && !iterator.next().mouseClicked(mouseX, mouseY, button)) {
            }
        }
        return this.isHovered;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.isHovered && CursorUtil.isInBounds((float) mouseX, (float) mouseY, this.posX, this.posY + 20.0f, 120.0f, this.totalHeight - 20.0f)) {
            Iterator<SettingElement<?>> iterator = this.settingElements.iterator();
            while (iterator.hasNext() && !iterator.next().mouseReleased(mouseX, mouseY, button)) {
            }
        }
        return this.isHovered;
    }

    @Override
    @Generated
    public SmoothAnimationTimer getAnimTimer() {
        return this.hoveredTimer;
    }

    @Generated
    public SmoothAnimationTimer getHoveredTimer() {
        return this.expandTimer;
    }

    @Generated
    public SmoothAnimationTimer getExpandTimer() {
        return this.settingsHeightTimer;
    }

    @Override
    @Generated
    public float getX() {
        return this.posX;
    }

    @Override
    @Generated
    public float getY() {
        return this.posY;
    }

    @Override
    @Generated
    public float getHeight() {
        return this.totalHeight;
    }

    @Override
    @Generated
    public void setX(float f) {
        this.posX = f;
    }

    @Override
    @Generated
    public void setY(float f) {
        this.posY = f;
    }

    @Override
    @Generated
    public void setHeight(float f) {
        this.totalHeight = f;
    }

    static {
        BUILD_TAG = "17";
        BG_COLOR = ColorUtil.fromRGB(32, 32, 32);
    }
}