package shit.zen.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import shit.zen.ZenClient;
import shit.zen.gui.legacy.CategoryPanel;
import shit.zen.gui.legacy.ModuleButton;
import shit.zen.modules.Category;

public class OldClickGui
extends Screen {
    private final List<CategoryPanel> categoryPanels = new ArrayList<>();
    private static final String TITLE = "Click GUI";

    public OldClickGui() {
        super(Component.nullToEmpty(TITLE));
        int n = 20;
        for (Category category : Category.values()) {
            this.categoryPanels.add(new CategoryPanel(n, 20, 140, 20, category));
            n += 160;
        }
    }

    public void init() {
        super.init();
        for (CategoryPanel categoryPanel : this.categoryPanels) {
            for (ModuleButton moduleButton : categoryPanel.moduleButtons) {
                moduleButton.reset();
            }
        }
    }

    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        for (CategoryPanel categoryPanel : this.categoryPanels) {
            categoryPanel.render(guiGraphics, n, n2, f);
            categoryPanel.mouseDragged(n, n2);
        }
        super.render(guiGraphics, n, n2, f);
    }

    public boolean mouseReleased(double d, double d2, int n) {
        for (CategoryPanel categoryPanel : this.categoryPanels) {
            categoryPanel.mouseReleased(d, d2, n);
        }
        return super.mouseReleased(d, d2, n);
    }

    public boolean mouseClicked(double d, double d2, int n) {
        for (CategoryPanel categoryPanel : this.categoryPanels) {
            categoryPanel.mouseClicked(d, d2, n);
        }
        return true;
    }

    public boolean mouseScrolled(double d, double d2, double d3) {
        for (CategoryPanel categoryPanel : this.categoryPanels) {
            categoryPanel.mouseScrolled(d, d2, d3);
        }
        return true;
    }

    public void onClose() {
        if (ZenClient.isReady()) {
            ZenClient.instance.getConfigManager().saveAll();
        }
        super.onClose();
    }

    static {
        OldClickGui oldClickGui = new OldClickGui();
    }
}