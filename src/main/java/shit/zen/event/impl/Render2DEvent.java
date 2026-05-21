package shit.zen.event.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphics;
import shit.zen.event.EventMarker;

import java.util.Objects;

public record Render2DEvent(PoseStack poseStack, GuiGraphics guiGraphics, float partialTick)
        implements EventMarker {
    @Override
    @Generated
    public PoseStack poseStack() {
        return this.poseStack;
    }

    @Override
    @Generated
    public GuiGraphics guiGraphics() {
        return this.guiGraphics;
    }

    @Override
    @Generated
    public float partialTick() {
        return this.partialTick;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Render2DEvent render2DEvent)) {
            return false;
        }
        if (!render2DEvent.canEqual(this)) {
            return false;
        }
        if (Float.compare(this.partialTick(), render2DEvent.partialTick()) != 0) {
            return false;
        }
        PoseStack poseStack = this.poseStack();
        PoseStack poseStack2 = render2DEvent.poseStack();
        if (!Objects.equals(poseStack, poseStack2)) {
            return false;
        }
        GuiGraphics guiGraphics = this.guiGraphics();
        GuiGraphics guiGraphics2 = render2DEvent.guiGraphics();
        return !(!Objects.equals(guiGraphics, guiGraphics2));
    }

    @Generated
    protected boolean canEqual(Object object) {
        return object instanceof Render2DEvent;
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        n2 = n2 * 59 + Float.floatToIntBits(this.partialTick());
        PoseStack poseStack = this.poseStack();
        n2 = n2 * 59 + (poseStack == null ? 43 : poseStack.hashCode());
        GuiGraphics guiGraphics = this.guiGraphics();
        n2 = n2 * 59 + (guiGraphics == null ? 43 : guiGraphics.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        float f = this.partialTick();
        String string = String.valueOf(this.guiGraphics());
        String string2 = String.valueOf(this.poseStack());
        return "Render2DEvent(stack=" + string2 + ", guiGraphics=" + string + ", partialTicks=" + f + ")";
    }

    @Generated
    public Render2DEvent {
    }
}