package shit.zen.gui.legacy;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import shit.zen.ZenClient;
import shit.zen.gui.legacy.ModuleButton;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.render.FontStore;
import shit.zen.utils.render.RenderUtil;

public class CategoryPanel {
    public int x;
    public int y;
    public int dragOffsetX;
    public int dragOffsetY;
    public int width;
    public int rowHeight;
    public Category category;
    public boolean dragging;
    public boolean expanded;
    public List<ModuleButton> moduleButtons;
    private float[] targetOffsets;
    private float[] currentOffsets;
    private final float lerpFactor = 0.2f;
    private long lastTime = System.currentTimeMillis();
    private boolean needsLayout = false;

    public CategoryPanel(int n, int n2, int n3, int n4, Category category) {
        this.x = n;
        this.y = n2;
        this.width = n3;
        this.rowHeight = n4;
        this.category = category;
        this.dragging = false;
        this.expanded = false;
        this.moduleButtons = new ArrayList<>();
        int n5 = n4;
        for (Module module : ZenClient.getInstance().getModuleManager().getModulesByCategory(category)) {
            this.moduleButtons.add(new ModuleButton(module, this, n5));
            n5 += n4;
        }
        this.initOffsets();
    }

    private void initOffsets() {
        this.targetOffsets = new float[this.moduleButtons.size()];
        this.currentOffsets = new float[this.moduleButtons.size()];
        for (int i = 0; i < this.moduleButtons.size(); ++i) {
            this.targetOffsets[i] = this.rowHeight + i * this.rowHeight;
            this.currentOffsets[i] = this.rowHeight + i * this.rowHeight;
        }
    }

    public void tick() {
        if (!this.needsLayout) {
            return;
        }
        boolean bl = false;
        long l = System.currentTimeMillis();
        float f = (float)(l - this.lastTime) / 1000.0f;
        this.lastTime = l;
        for (ModuleButton moduleButton : this.moduleButtons) {
            if (!moduleButton.isAnimating()) continue;
            bl = true;
        }
        for (int i = 0; i < this.moduleButtons.size(); ++i) {
            float f2 = this.targetOffsets[i] - this.currentOffsets[i];
            if (Math.abs(f2) > 0.5f) {
                int n = i;
                this.currentOffsets[n] = this.currentOffsets[n] + f2 * 0.2f * (f * 60.0f);
                bl = true;
                this.moduleButtons.get(i).yOffset = (int)this.currentOffsets[i];
                continue;
            }
            this.currentOffsets[i] = this.targetOffsets[i];
            this.moduleButtons.get(i).yOffset = (int)this.targetOffsets[i];
        }
        this.needsLayout = bl;
    }

    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.tick();
        RenderUtil.drawFilledRect(guiGraphics.pose(), this.x, this.y, this.width, this.rowHeight, new Color(16, 16, 20, 245).getRGB());
        String string = String.valueOf(switch (this.category) {
            case COMBAT -> 'a';
            case MOVEMENT -> 'b';
            case PLAYER -> 'c';
            case RENDER -> 'd';
            case EXPLOIT -> 'e';
            default -> '?';
        });
        float f2 = (float)this.y + ((float)this.rowHeight / 2.0f - FontStore.ICON_30.getFontHeight() / 2.0f) + 3.0f;
        FontStore.ICON_30.drawStringWithShadow(guiGraphics.pose(), string, this.x + 4, f2, -1);
        float f3 = (float)this.y + ((float)this.rowHeight / 2.0f - FontStore.OPENSANS_18.getFontHeight() / 2.0f) - 0.5f;
        FontStore.OPENSANS_18.drawStringWithShadow(guiGraphics.pose(), this.category.displayName, this.x + this.rowHeight + 4, f3, -1);
        for (ModuleButton moduleButton : this.moduleButtons) {
            moduleButton.render(guiGraphics.pose(), n, n2, f);
        }
    }

    public void mouseClicked(double d, double d2, int n) {
        if (this.isHovered(d, d2)) {
            if (n == 0) {
                this.dragging = true;
                this.dragOffsetX = (int)(d - (double)this.x);
                this.dragOffsetY = (int)(d2 - (double)this.y);
            } else if (n == 1) {
                this.expanded = !this.expanded;
                this.recalcLayout();
            }
        }
        for (ModuleButton moduleButton : this.moduleButtons) {
            moduleButton.mouseClicked(d, d2, n);
        }
    }

    public void mouseReleased(double d, double d2, int n) {
        for (ModuleButton moduleButton : this.moduleButtons) {
            moduleButton.mouseReleased(d, d2, n);
        }
        if (n == 0 && this.dragging) {
            this.dragging = false;
        }
    }

    public void mouseScrolled(double d, double d2, double d3) {
        for (ModuleButton moduleButton : this.moduleButtons) {
            moduleButton.mouseScrolled(d, d2, d3);
        }
    }

    public boolean isHovered(double d, double d2) {
        return d > (double)this.x && d < (double)(this.x + this.width) && d2 > (double)this.y && d2 < (double)(this.y + this.rowHeight);
    }

    public void mouseDragged(double d, double d2) {
        if (this.dragging) {
            this.x = (int)(d - (double)this.dragOffsetX);
            this.y = (int)(d2 - (double)this.dragOffsetY);
        }
    }

    public void recalcLayout() {
        int n = this.rowHeight;
        for (int i = 0; i < this.moduleButtons.size(); ++i) {
            ModuleButton moduleButton = this.moduleButtons.get(i);
            this.targetOffsets[i] = n;
            n += moduleButton.getTotalHeight();
        }
        this.needsLayout = true;
        this.lastTime = System.currentTimeMillis();
    }
}