package shit.zen.gui;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import shit.zen.gui.newclickgui.CategoryPanel;
import shit.zen.modules.Category;
import shit.zen.utils.animation.SmoothAnimationTimer;
import shit.zen.utils.math.Easings;

public class NewClickGui
extends Screen {
    private static final List<CategoryPanel> categoryPanels;
    private static boolean initialized;
    public static CategoryPanel focusedPanel;
    @Getter
    private boolean closing = false;
    @Getter
    private final SmoothAnimationTimer closeAnim = new SmoothAnimationTimer();

    public NewClickGui() {
        super(Component.literal("ClickGui"));
        System.out.println("12");
    }

    protected void init() {
        System.out.println("13");
        focusedPanel = categoryPanels.get(0);
        float f = (float)this.width / 2.0f - 380.0f;
        for (CategoryPanel categoryPanel : categoryPanels) {
            categoryPanel.setX(f);
            categoryPanel.setY(36.0f);
            f += 128.0f;
        }
        initialized = true;
        System.out.println("14");
    }

    public void render(@NonNull GuiGraphics guiGraphics, int n, int n2, float f) {
        if (guiGraphics == null) {
            throw new NullPointerException("graphics is marked non-null but is null");
        }
        this.closeAnim.animate(this.closing ? 0.0 : 1.0, 0.2, Easings.EASE_OUT_POW2);
        this.closeAnim.tick();
        float f2 = this.closeAnim.getValueF();
        if (Mth.equal(f2, 0.0f) && this.closing) {
            this.closing = false;
            super.onClose();
            categoryPanels.forEach(CategoryPanel::reset);
            return;
        }
        for (CategoryPanel categoryPanel : categoryPanels) {
            categoryPanel.render(this, guiGraphics, guiGraphics.pose(), n, n2, f2, f);
        }
    }

    public void onClose() {
        this.closing = true;
    }

    public boolean mouseClicked(double d, double d2, int n) {
        for (CategoryPanel categoryPanel : categoryPanels) {
            if (!categoryPanel.mouseClicked(d, d2, n)) continue;
            focusedPanel = categoryPanel;
            return true;
        }
        return super.mouseClicked(d, d2, n);
    }

    public boolean mouseReleased(double d, double d2, int n) {
        for (CategoryPanel categoryPanel : categoryPanels) {
            categoryPanel.mouseReleased(d, d2, n);
        }
        return false;
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean mouseScrolled(double d, double d2, double d3) {
        for (CategoryPanel categoryPanel : categoryPanels) {
            if (!categoryPanel.mouseScrolled(d, d2, d3)) continue;
            return true;
        }
        return false;
    }

    static {
        NewClickGui newClickGui = new NewClickGui();
        categoryPanels = new ArrayList<>();
        initialized = false;
        for (Category category : Category.values()) {
            categoryPanels.add(new CategoryPanel(category));
        }
    }
}