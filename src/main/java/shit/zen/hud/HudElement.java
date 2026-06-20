package shit.zen.hud;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.gui.screens.ChatScreen;
import shit.zen.event.impl.GlRenderEvent;
import shit.zen.event.impl.Render2DEvent;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.utils.render.RenderUtil;

public abstract class HudElement extends Module {
    @Getter @Setter protected float x;
    @Getter @Setter protected float y;
    @Getter @Setter protected float width;
    @Getter @Setter protected float height;
    @Getter @Setter private boolean dragging = false;
    @Getter @Setter private float dragOffsetX;
    @Getter @Setter private float dragOffsetY;

    public HudElement(String string) {
        super(string, Category.RENDER);
    }

    public abstract void onRender2D(Render2DEvent var1, float var2, float var3);
    public abstract void onGlRender(GlRenderEvent var1, float var2, float var3);
    public abstract void onSettings();

    //我不知道为什么给name protect加上开关会把编辑炸飞
    // 这个实现虽然不优雅但是能用，
    //idk why
    //why why why why why why why why why why why why why why why why why why why why why why why why why why why why why why why why
    public void editHandler() {
        if (!this.isEnabled()) return;
        if (mc.level == null || mc.player == null) return;

        if (mc.screen instanceof ChatScreen) {
            double mouseX = mc.mouseHandler.xpos() * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
            double mouseY = mc.mouseHandler.ypos() * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
            boolean isLeftDown = GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), 0) == 1;
            if (isLeftDown) {
                if (!this.dragging) {
                    if (this.isHovered((int) mouseX, (int) mouseY)) {
                        this.dragging = true;
                        this.dragOffsetX = (float) mouseX - this.getX();
                        this.dragOffsetY = (float) mouseY - this.getY();
                    }
                } else {
                    this.mouseDragged((int) mouseX, (int) mouseY);
                }
            } else {
                this.stopDragging();
            }
        } else {
            this.stopDragging();
        }
    }

    public void mouseDragged(int mouseX, int mouseY) {
        this.x = (float)mouseX - this.dragOffsetX;
        this.y = (float)mouseY - this.dragOffsetY;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return RenderUtil.isHovered(this.x, this.y, this.width, this.height, mouseX, mouseY);
    }

    public void stopDragging() {
        this.dragging = false;
    }
}