package shit.zen.gui.panel;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import shit.zen.ClientBase;
import shit.zen.modules.Category;
import shit.zen.render.DrawContext;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.render.Renderer;
import shit.zen.render.TextGlow;
import shit.zen.utils.math.LerpUtil;

public class CategoryBar
extends ClientBase {
    private static final Map<Category, String> CATEGORY_ICONS = new HashMap<>();
    private final Map<Category, Float> hoverAnimations = new HashMap<>();
    private Category selectedCategory = Category.COMBAT;

    public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, float f, float f2) {
        try {
            int n5 = (int)(20.0f * f);
            int n6 = (int)(25.0f * f);
            int n7 = (int)(80.0f * f);
            float f3 = 24.0f * f;
            int n8 = (int)(420.0f * f);
            int n9 = n + n8;
            int n10 = n2 + n7 - (int)(65.0f * f);
            Category[] categoryArray = Category.values();
            Renderer.renderConsumer(drawContext -> {
                FontRenderer iconFont = FontPresets.materialIcons(f3);
                for (int i = 0; i < categoryArray.length; ++i) {
                    Category category = categoryArray[i];
                    String iconString = CATEGORY_ICONS.get(category);
                    if (iconString == null) continue;
                    int iconX = n9 + i * n6;
                    int iconY = n10;
                    this.updateCategoryHover(category, iconX, iconY, n3, n4, n5);
                    int categoryColor = this.getCategoryColor(category);
                    if (category == this.selectedCategory) {
                        int glowColor = new Color(255, 255, 255, (int)(150.0f * f2)).getRGB();
                        TextGlow.drawGlowText(iconString, iconX, iconY, iconFont, this.applyAlpha(categoryColor, f2), glowColor, 8.0f * f);
                        continue;
                    }
                    GlHelper.drawText(iconString, iconX, iconY, iconFont, this.applyAlpha(categoryColor, f2));
                }
            });
        } catch (Exception exception) {
            // empty catch block
        }
    }

    private int applyAlpha(int n, float f) {
        int n2 = n >> 24 & 0xFF;
        int n3 = (int)((float)n2 * f);
        return n3 << 24 | n & 0xFFFFFF;
    }

    private void updateCategoryHover(Category category, int n, int n2, int n3, int n4, int n5) {
        float f = this.hoverAnimations.getOrDefault(category, Float.valueOf(0.0f)).floatValue();
        boolean bl = this.isMouseOverCategory(n, n2, n3, n4, n5);
        this.hoverAnimations.put(category, Float.valueOf(LerpUtil.lerp(f, bl ? 1.0f : 0.0f, 0.12f)));
    }

    private int getCategoryColor(Category category) {
        if (category == this.selectedCategory) {
            return -1;
        }
        float f = this.hoverAnimations.getOrDefault(category, Float.valueOf(0.0f)).floatValue();
        return this.lerpColor(-7829368, -3355444, f);
    }

    private int lerpColor(int n, int n2, float f) {
        float f2 = 1.0f - f;
        int n3 = n >> 24 & 0xFF;
        int n4 = n >> 16 & 0xFF;
        int n5 = n >> 8 & 0xFF;
        int n6 = n & 0xFF;
        int n7 = n2 >> 24 & 0xFF;
        int n8 = n2 >> 16 & 0xFF;
        int n9 = n2 >> 8 & 0xFF;
        int n10 = n2 & 0xFF;
        int n11 = (int)((float)n3 * f2 + (float)n7 * f);
        int n12 = (int)((float)n4 * f2 + (float)n8 * f);
        int n13 = (int)((float)n5 * f2 + (float)n9 * f);
        int n14 = (int)((float)n6 * f2 + (float)n10 * f);
        return n11 << 24 | n12 << 16 | n13 << 8 | n14;
    }

    private boolean isMouseOverCategory(int n, int n2, int n3, int n4, int n5) {
        int n6 = n5 / 2;
        return n3 >= n && n3 <= n + n5 && n4 >= n2 - n6 && n4 <= n2 + n6;
    }

    public boolean onMouseClick(int n, int n2, int n3, int n4, float f) {
        int n5 = (int)(20.0f * f);
        int n6 = (int)(25.0f * f);
        int n7 = (int)(80.0f * f);
        int n8 = (int)(420.0f * f);
        int n9 = n + n8;
        int n10 = n2 + n7 - (int)(65.0f * f);
        Category[] categoryArray = Category.values();
        for (int i = 0; i < categoryArray.length; ++i) {
            Category category = categoryArray[i];
            int n11 = n9 + i * n6;
            int n12 = n10;
            if (!this.isMouseOverCategory(n11, n12, n3, n4, n5)) continue;
            this.selectedCategory = category;
            return true;
        }
        return false;
    }

    public Category getSelectedCategory() {
        return this.selectedCategory;
    }

    public void setSelectedCategory(Category category) {
        this.selectedCategory = category;
    }

    public boolean isMouseOverAnyCategory(int n, int n2, int n3, int n4, float f) {
        int n5 = (int)(20.0f * f);
        int n6 = (int)(25.0f * f);
        int n7 = (int)(80.0f * f);
        int n8 = (int)(420.0f * f);
        int n9 = n + n8;
        int n10 = n2 + n7 - (int)(65.0f * f);
        Category[] categoryArray = Category.values();
        for (int i = 0; i < categoryArray.length; ++i) {
            int n11 = n9 + i * n6;
            int n12 = n10;
            if (!this.isMouseOverCategory(n11, n12, n3, n4, n5)) continue;
            return true;
        }
        return false;
    }

    public int getTotalWidth(float f) {
        int n = (int)(20.0f * f);
        int n2 = (int)(25.0f * f);
        return (Category.values().length - 1) * n2 + n;
    }

    public int getCategoryHeight(float f) {
        return (int)(20.0f * f);
    }

    static {
        CATEGORY_ICONS.put(Category.COMBAT, "");
        CATEGORY_ICONS.put(Category.MOVEMENT, "");
        CATEGORY_ICONS.put(Category.PLAYER, "");
        CATEGORY_ICONS.put(Category.RENDER, "");
        CATEGORY_ICONS.put(Category.EXPLOIT, "");
        CATEGORY_ICONS.put(Category.WORLD, "");
        CATEGORY_ICONS.put(Category.MISC, "");
    }
}