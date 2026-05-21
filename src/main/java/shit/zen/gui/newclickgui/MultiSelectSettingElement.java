package shit.zen.gui.newclickgui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import shit.zen.gui.NewClickGui;
import shit.zen.gui.newclickgui.CategoryPanel;
import shit.zen.gui.newclickgui.SettingElement;
import shit.zen.render.FontStore;
import shit.zen.settings.impl.MultiSelectSetting;
import shit.zen.utils.animation.SmoothAnimationTimer;
import shit.zen.utils.math.Easings;
import shit.zen.utils.misc.CursorUtil;
import shit.zen.utils.render.ColorUtil;
import shit.zen.utils.render.RenderUtil;

public class MultiSelectSettingElement
extends SettingElement<MultiSelectSetting> {
    @Getter
    private final SmoothAnimationTimer hoverTimer = new SmoothAnimationTimer();
    @Getter
    private final SmoothAnimationTimer visTimer = new SmoothAnimationTimer();
    @Getter
    private final SmoothAnimationTimer highlightTimer = new SmoothAnimationTimer();
    @Getter
    private final SmoothAnimationTimer highlightYTimer = new SmoothAnimationTimer();
    @Getter @Setter
    private boolean isDropdownHovered;
    @Getter @Setter
    private boolean isOpen;
    @Getter @Setter
    private String hoveredOption;
    @Getter @Setter
    private boolean hasMultipleSelected;
    @Getter @Setter
    private boolean isTooltipShown;
    private static final String ELLIPSIS = "...";

    public MultiSelectSettingElement(CategoryPanel categoryPanel, MultiSelectSetting multiSelectSetting) {
        super(categoryPanel, multiSelectSetting);
    }

    @Override
    public void render(NewClickGui newClickGui, GuiGraphics guiGraphics, PoseStack poseStack, int n, int n2, float f, float f2) {
        float f3;
        float f4;
        float f5 = this.y + 18.0f + 2.0f;
        float f6 = 108.0f;
        float f7 = 14.0f;
        this.hoveredOption = null;
        this.isDropdownHovered = CursorUtil.isInBounds(n, n2, this.x + 6.0f, f5, f6, f7);
        this.hoverTimer.animate(this.isDropdownHovered ? 1.0 : 0.0, 0.22, Easings.EASE_OUT_POW2);
        this.hoverTimer.tick();
        this.visibilityTimer.animate(this.setting.getVisibility().displayable() ? 1.0 : 0.0, 0.2, Easings.EASE_OUT_POW2);
        this.visibilityTimer.tick();
        this.visTimer.animate(this.isOpen ? 1.0 : 0.0, 0.2, Easings.EASE_OUT_POW2);
        this.visTimer.tick();
        if (Mth.equal(f *= this.visibilityTimer.getValueF(), 0.0f)) {
            return;
        }
        float f8 = this.y + (18.0f - FontStore.AXIFORMA_REGULAR_14.getFontHeight()) / 2.0f + 1.0f;
        FontStore.AXIFORMA_REGULAR_14.drawString(poseStack, this.setting.getName(), this.x + 6.0f, f8, ColorUtil.withAlpha(-1, f * 0.8f));
        float f9 = this.visTimer.getValueF();
        if (f9 > 0.0f) {
            f4 = f7 + (float) this.setting.getOptions().size() * f7 * f9;
            RenderUtil.drawRoundedRect(poseStack, this.x + 6.0f, f5, f6, f4, 3.0f, ColorUtil.withAlpha(ColorUtil.fromRGB(60, 60, 60), f * f9));
            f3 = (f7 - FontStore.AXIFORMA_BOLD_13.getFontHeight()) / 2.0f;
            float f10 = f5 + f3 + f7;
            for (String string : this.setting.getOptions()) {
                if (CursorUtil.isInBounds(n, n2, this.x + 6.0f, f10 - f3, f6, f7)) {
                    this.hoveredOption = string;
                    this.highlightYTimer.animate(f10 - f3, 0.2, Easings.EASE_OUT_POW2);
                }
                if (f5 + f4 > f10 + FontStore.AXIFORMA_BOLD_13.getFontHeight()) {
                    FontStore.AXIFORMA_BOLD_13.drawStringCentered(poseStack, string, this.x + 60.0f, f10, ColorUtil.withAlpha(-1, f * 0.8f * f9));
                    if (this.setting.getValue().contains(string)) {
                        FontStore.MATERIAL_14.drawString(poseStack, "", this.x + 6.0f + f6 - FontStore.MATERIAL_14.getStringWidth("") - 4.0f, f10 - f3 + (f7 - FontStore.MATERIAL_14.getFontHeight()) / 2.0f + 0.5f, ColorUtil.withAlpha(-1, f * 0.56f * f9));
                    }
                }
                f10 += f7;
            }
        }
        this.highlightTimer.animate(this.hoveredOption == null || !this.isOpen ? 0.0 : 1.0, 0.18, Easings.EASE_OUT_POW2);
        this.highlightTimer.tick();
        this.highlightYTimer.tick();
        f4 = this.highlightTimer.getValueF();
        if (f4 > 0.0f) {
            RenderUtil.drawRoundedRect(poseStack, this.x + 6.0f, this.highlightYTimer.getValueF(), f6, f7, 3.0f, ColorUtil.withAlpha(ColorUtil.fromRGB(255, 255, 255), f * f4 * 1.0f * 0.1f));
        }
        f3 = this.hoverTimer.getValueF();
        RenderUtil.drawRoundedRect(poseStack, this.x + 6.0f, f5, f6, f7, 3.0f, ColorUtil.withAlpha(ColorUtil.fromRGB((int)(60.0f + 30.0f * f3), (int)(60.0f + 30.0f * f3), (int)(60.0f + 30.0f * f3)), f));
        String string = this.setting.getValue().get(0);
        if (this.setting.getValue().size() > 1) {
            string = string + ELLIPSIS;
            this.hasMultipleSelected = true;
        }
        FontStore.AXIFORMA_BOLD_13.drawStringCentered(poseStack, string, this.x + 60.0f, f5 + (f7 - FontStore.AXIFORMA_BOLD_13.getFontHeight()) / 2.0f, ColorUtil.withAlpha(-1, f * 0.8f));
        Object object = String.valueOf('\ueb5d');
        FontStore.MATERIAL_20.drawString(poseStack, (String)object, this.x + 6.0f + f6 - FontStore.MATERIAL_20.getStringWidth((String)object) - 2.0f, f5 + (f7 - FontStore.MATERIAL_20.getFontHeight()) / 2.0f + 0.5f, ColorUtil.withAlpha(-1, f * 0.8f));
        if (this.isDropdownHovered && this.hasMultipleSelected) {
            this.parentPanel.setHoveredSettingElement(this);
            this.parentPanel.setTooltipText(this.setting.getValue().toString());
            this.parentPanel.setShowTooltip(true);
        } else if (this.parentPanel.getHoveredSettingElement() == this) {
            this.parentPanel.setShowTooltip(false);
            this.parentPanel.setHoveredSettingElement(null);
        }
    }

    @Override
    public float getHeight() {
        return 36 + (this.isOpen ? 14 * this.setting.getOptions().size() : 0);
    }

    @Override
    public float getAnimatedHeight() {
        return 36.0f + (float)(14 * this.setting.getOptions().size()) * this.visTimer.getValueF();
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (this.isDropdownHovered) {
            this.isOpen = !this.isOpen;
            return true;
        }
        if (this.hoveredOption != null && this.isOpen) {
            ArrayList<String> arrayList = new ArrayList<>(this.setting.getValue());
            if (arrayList.contains(this.hoveredOption)) {
                if (arrayList.size() > 1) {
                    arrayList.remove(this.hoveredOption);
                }
            } else {
                arrayList.add(this.hoveredOption);
            }
            this.setting.setValue(arrayList);
            return true;
        }
        return false;
    }

}