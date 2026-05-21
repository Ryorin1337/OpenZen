package shit.zen.hud;

import lombok.Getter;
import lombok.Setter;
import lombok.Generated;
import shit.zen.event.impl.GlRenderEvent;
import shit.zen.event.impl.Render2DEvent;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.utils.render.RenderUtil;

public abstract class HudElement
extends Module {
    @Getter @Setter
    protected float x;
    @Getter @Setter
    protected float y;
    protected float hudWidth;
    protected float hudHeight;
    @Getter
    private boolean dragging = true;
    @Getter @Setter
    private float dragOffsetX;
    @Getter @Setter
    private float dragOffsetY;
    protected boolean visible;

    public HudElement(String string) {
        super(string, Category.RENDER);
    }

    public abstract void onRender2D(Render2DEvent var1, float var2, float var3);

    public abstract void onGlRender(GlRenderEvent var1, float var2, float var3);

    public abstract void onSettings();

    public boolean mousePressed(int n, int n2, int n3) {
        if (this.isHovered(n, n2) && n3 == 0) {
            this.visible = true;
            this.dragOffsetX = (float)n - this.getX();
            this.dragOffsetY = (float)n2 - this.getY();
            return true;
        }
        return false;
    }

    public void mouseDragged(int n, int n2) {
        this.x = (float)n - this.dragOffsetX;
        this.y = (float)n2 - this.dragOffsetY;
    }

    public boolean isHovered(int n, int n2) {
        return RenderUtil.isHovered(this.x, this.y, this.hudWidth, this.hudHeight, n, n2);
    }

    public void stopDragging() {
        this.setEnabled(false);
    }

    @Generated
    public float getWidth() {
        return this.hudWidth;
    }

    @Generated
    public float getHeight() {
        return this.hudHeight;
    }

    @Override
    @Generated
    public boolean isEnabled() {
        return this.visible;
    }

    @Generated
    public void setWidth(float f) {
        this.hudWidth = f;
    }

    @Generated
    public void setHeight(float f) {
        this.hudHeight = f;
    }

    @Generated
    protected void setDragging(boolean bl) {
        this.dragging = bl;
    }

    @Override
    @Generated
    public void setEnabled(boolean bl) {
        this.visible = bl;
    }
}