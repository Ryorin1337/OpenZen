package shit.zen.gui.newclickgui;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import shit.zen.gui.NewClickGui;
import shit.zen.utils.animation.SmoothAnimationTimer;

public abstract class UIElement {
    @Getter @Setter
    protected float x;
    @Getter @Setter
    protected float y;
    @Getter @Setter
    protected float width;
    @Getter @Setter
    protected float height;
    @Getter
    protected final SmoothAnimationTimer animTimer = new SmoothAnimationTimer();

    public abstract void render(NewClickGui var1, GuiGraphics var2, PoseStack var3, int var4, int var5, float var6, float var7);

    public void reset() {
    }

    public boolean mouseClicked(double d, double d2, int n) {
        return false;
    }

    public boolean mouseReleased(double d, double d2, int n) {
        return false;
    }

    public boolean mouseScrolled(double d, double d2, double d3) {
        return false;
    }

    public float getAnimatedHeight() {
        return this.getHeight();
    }

    }