package shit.zen.gui.newclickgui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import shit.zen.ZenClient;
import shit.zen.gui.NewClickGui;
import shit.zen.gui.newclickgui.ModuleElement;
import shit.zen.gui.newclickgui.SettingElement;
import shit.zen.gui.newclickgui.UIElement;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.render.FontStore;
import shit.zen.render.StencilHelper;
import shit.zen.utils.animation.SmoothAnimationTimer;
import shit.zen.utils.math.Easings;
import shit.zen.utils.misc.CursorUtil;
import shit.zen.utils.render.ColorUtil;
import shit.zen.utils.render.RenderHelper;
import shit.zen.utils.render.RenderUtil;

public class CategoryPanel
extends UIElement {
    public static final int BG_COLOR = ColorUtil.fromRGB(23, 23, 23);
    public static final int ACCENT_COLOR_DARK = new Color(-13768502).darker().darker().getRGB();
    public static final int ACCENT_COLOR = new Color(-13768502).darker().getRGB();
    @Getter
    private final List<ModuleElement> moduleElements = new ArrayList<>();
    @Getter
    private final Category category;
    private float posX;
    private float posY;
    private float panelHeight;
    @Getter @Setter
    private boolean isHovered;
    @Getter @Setter
    private boolean isDragging;
    @Getter @Setter
    private float dragOffsetX;
    @Getter @Setter
    private float dragOffsetY;
    @Getter @Setter
    private float scrollAmount;
    @Getter @Setter
    private float prevHeight;
    @Getter
    private final SmoothAnimationTimer scaleTimer = new SmoothAnimationTimer();
    @Getter
    private final SmoothAnimationTimer scrollTimer = new SmoothAnimationTimer();
    @Getter
    private final SmoothAnimationTimer tooltipTimer = new SmoothAnimationTimer();
    @Getter
    private final SmoothAnimationTimer collapseTimer = new SmoothAnimationTimer();
    @Getter @Setter
    private SettingElement<?> hoveredSettingElement;
    @Getter @Setter
    private String tooltipText = "";
    @Getter @Setter
    private String tooltipText2 = "";
    @Getter @Setter
    private boolean isCollapsed;
    @Getter @Setter
    private boolean showTooltip;
    @Getter @Setter
    private float savedPosX;
    @Getter @Setter
    private float savedPosY;
    @Getter @Setter
    private float savedScrollAmount;
    @Getter @Setter
    private float savedHeight;

    public CategoryPanel(Category category) {
        System.out.println("15");
        this.category = category;
        for (Module module : ZenClient.getInstance().getModuleManager().getModulesByCategory(category)) {
            this.moduleElements.add(new ModuleElement(this, module));
        }
        System.out.println("15.5");
        this.panelHeight = 20.0f + Math.min(240.0f, 20.0f * (float)this.moduleElements.size());
        System.out.println("16");
    }

    @Override
    public void render(NewClickGui newClickGui, GuiGraphics guiGraphics, PoseStack poseStack, int n, int n2, float f, float f2) {
        this.isHovered = CursorUtil.isInBounds(n, n2, this.posX, this.posY, 120.0f, this.panelHeight);
        if (this.isHovered) {
            NewClickGui.focusedPanel = this;
        }
        this.scaleTimer.animate(newClickGui.isClosing() ? 0.0 : 1.0, newClickGui.isClosing() ? 0.22 : 0.32, Easings.BACK_OUT);
        this.scaleTimer.tick();
        float f3 = 0.0f;
        for (ModuleElement moduleElement : this.moduleElements) {
            f3 += moduleElement.getHeight();
        }
        if (this.moduleElements.size() < 10) {
            this.panelHeight = Math.min(f3, 240.0f) + 20.0f;
        }
        this.scrollAmount = Mth.clamp(this.scrollAmount, 0.0f, f3 - this.panelHeight + 20.0f);
        this.scrollTimer.animate(this.scrollAmount, 0.22, Easings.EASE_OUT_POW2);
        this.scrollTimer.tick();
        this.tooltipTimer.animate(this.showTooltip ? 1.0 : 0.0, 0.3, Easings.EASE_OUT_POW2);
        this.tooltipTimer.tick();
        if (this.isDragging) {
            this.posX = (float)n + this.dragOffsetX;
            this.posY = (float)n2 + this.dragOffsetY;
        }
        this.collapseTimer.animate(this.isCollapsed ? 0.0 : 1.0, 0.2, Easings.EASE_OUT_POW2);
        this.collapseTimer.tick();
        float f4 = this.collapseTimer.getValueF();
        if (!this.isCollapsed) {
            this.prevHeight = this.panelHeight;
        }
        float f5 = this.scaleTimer.getValueF();
        RenderHelper.pushScaleAround(poseStack, this.posX + 60.0f, this.posY + this.panelHeight / 2.0f, 0.4f + 0.6f * f5);
        float f6 = 12.0f;
        RenderUtil.drawRoundedRect(poseStack, this.posX - f6, this.posY - f6, 120.0f + f6 * 2.0f, this.panelHeight + f6 * 2.0f, 6.0f + f6 / 2.0f, f6, ColorUtil.fromARGB(0, 0, 0, (int)(80.0f * f * 1.0f)));
        RenderUtil.drawRoundedRect(poseStack, this.posX, this.posY, 120.0f, this.panelHeight, 6.0f, ColorUtil.withAlpha(BG_COLOR, f));
        StencilHelper.beginWrite(false);
        RenderUtil.drawRoundedRect(poseStack, this.posX + 0.5f, this.posY, 118.0f, 20.0f, 6.0f, -1);
        StencilHelper.beginRead(true);
        RenderUtil.drawGradientH(poseStack, this.posX, this.posY, 120.0f, 1.0f, ColorUtil.withAlpha(ColorUtil.animateColorOffset(-13768502, ACCENT_COLOR_DARK, 100L), f), ColorUtil.withAlpha(ColorUtil.animateColorOffset(-13768502, ACCENT_COLOR_DARK, 2000L), f));
        StencilHelper.end();
        FontStore.AXIFORMA_EXTRABOLD_18.drawString(poseStack, this.category.displayName, this.posX + 8.0f, this.posY + (20.0f - FontStore.AXIFORMA_EXTRABOLD_18.getFontHeight()) / 2.0f + 3.0f, ColorUtil.withAlpha(-1, f));
        float f7 = this.scrollTimer.getValueF();
        float f8 = this.posY + 20.0f - f7;
        StencilHelper.beginWrite(false);
        RenderUtil.drawFilledRect(poseStack, this.posX + 0.5f, this.posY + 20.0f, 119.0f, 6.0f, -1);
        RenderUtil.drawRoundedRect(poseStack, this.posX, this.posY + 20.0f, 120.0f, this.panelHeight - 20.0f - 0.5f, 6.0f, -1);
        StencilHelper.beginRead(true);
        for (ModuleElement moduleElement : this.moduleElements) {
            moduleElement.setX(this.posX);
            moduleElement.setY(f8);
            moduleElement.render(newClickGui, guiGraphics, poseStack, n, n2, f, f2);
            f8 += moduleElement.getHeight();
        }
        RenderUtil.drawGradientV(poseStack, this.posX + 0.5f, this.posY + 20.0f - 0.5f, 119.0f, 6.0f, ColorUtil.withAlpha(-16777216, 0.36f * f), ColorUtil.withAlpha(-16777216, 0.0f));
        StencilHelper.end();
        float f9 = this.tooltipTimer.getValueF();
        if (f9 > 0.0f) {
            float f10 = FontStore.AXIFORMA_REGULAR_16.getStringWidth(this.tooltipText);
            RenderUtil.drawShadow(poseStack, n + 5, n2 + 5, f10 + 6.0f, FontStore.AXIFORMA_REGULAR_16.getFontHeight() + 4.0f, 12, ColorUtil.withAlpha(BG_COLOR, f * f9 * 0.66f));
            RenderUtil.drawRoundedRect(poseStack, n + 5, n2 + 5, f10 + 6.0f, FontStore.AXIFORMA_REGULAR_16.getFontHeight() + 4.0f, 3.0f, ColorUtil.withAlpha(BG_COLOR, f * f9));
            FontStore.AXIFORMA_REGULAR_16.drawString(poseStack, this.tooltipText, n + 5 + 3, n2 + 5 + 1, ColorUtil.withAlpha(-1, f * f9));
        }
        RenderHelper.popPose(poseStack);
    }

    @Override
    public void reset() {
        this.scaleTimer.setFromValue(0.0);
        this.scaleTimer.setCurrentValue(0.0);
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (this.isHovered) {
            if (CursorUtil.isInBounds((float)d, (float)d2, this.posX, this.posY, 120.0f, 20.0f)) {
                this.isDragging = true;
                this.dragOffsetX = this.posX - (float)d;
                this.dragOffsetY = this.posY - (float)d2;
            } else if (CursorUtil.isInBounds((float)d, (float)d2, this.posX, this.posY + 20.0f, 120.0f, this.panelHeight - 20.0f)) {
                for (ModuleElement moduleElement : this.moduleElements) {
                    if (!moduleElement.mouseClicked(d, d2, n)) continue;
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double d, double d2, int n) {
        if (CursorUtil.isInBounds((float)d, (float)d2, this.posX, this.posY + 20.0f, 120.0f, this.panelHeight - 20.0f)) {
            for (ModuleElement moduleElement : this.moduleElements) {
                if (!moduleElement.mouseReleased(d, d2, n)) continue;
                return true;
            }
        }
        this.isDragging = false;
        return true;
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3) {
        if (this.isHovered) {
            this.scrollAmount -= (float)d3 * 50.0f;
            return true;
        }
        return false;
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
        return this.panelHeight;
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
        this.panelHeight = f;
    }

    }