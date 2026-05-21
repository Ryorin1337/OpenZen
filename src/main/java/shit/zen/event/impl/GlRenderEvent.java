package shit.zen.event.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphics;
import shit.zen.event.EventMarker;
import shit.zen.render.DrawContext;

import java.util.Objects;

public record GlRenderEvent(GuiGraphics guiGraphics, PoseStack poseStack, DrawContext drawContext)
        implements EventMarker {
    @Override
    @Generated
    public GuiGraphics guiGraphics() {
        return this.guiGraphics;
    }

    @Override
    @Generated
    public PoseStack poseStack() {
        return this.poseStack;
    }

    @Override
    @Generated
    public DrawContext drawContext() {
        return this.drawContext;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof GlRenderEvent glRenderEvent)) {
            return false;
        }
        if (!glRenderEvent.canEqual(this)) {
            return false;
        }
        GuiGraphics guiGraphics = this.guiGraphics();
        GuiGraphics guiGraphics2 = glRenderEvent.guiGraphics();
        if (!Objects.equals(guiGraphics, guiGraphics2)) {
            return false;
        }
        PoseStack poseStack = this.poseStack();
        PoseStack poseStack2 = glRenderEvent.poseStack();
        if (!Objects.equals(poseStack, poseStack2)) {
            return false;
        }
        DrawContext drawContext = this.drawContext();
        DrawContext drawContext2 = glRenderEvent.drawContext();
        return !(!Objects.equals(drawContext, drawContext2));
    }

    @Generated
    protected boolean canEqual(Object object) {
        return object instanceof GlRenderEvent;
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        GuiGraphics guiGraphics = this.guiGraphics();
        n2 = n2 * 59 + (guiGraphics == null ? 43 : guiGraphics.hashCode());
        PoseStack poseStack = this.poseStack();
        n2 = n2 * 59 + (poseStack == null ? 43 : poseStack.hashCode());
        DrawContext drawContext = this.drawContext();
        n2 = n2 * 59 + (drawContext == null ? 43 : drawContext.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        String string = String.valueOf(this.drawContext());
        String string2 = String.valueOf(this.poseStack());
        String string3 = String.valueOf(this.guiGraphics());
        return "GlRenderEvent(guiGraphics=" + string3 + ", stack=" + string2 + ", context=" + string + ")";
    }

    @Generated
    public GlRenderEvent {
    }
}