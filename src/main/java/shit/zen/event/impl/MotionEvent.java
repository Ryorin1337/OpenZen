package shit.zen.event.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.Generated;
import shit.zen.event.Event;

public class MotionEvent
extends Event {
    @Getter @Setter
    public boolean pre;
    @Getter @Setter
    public double x;
    @Getter @Setter
    public double y;
    @Getter @Setter
    public double z;
    @Getter @Setter
    public float yaw;
    @Getter @Setter
    public float pitch;
    @Getter @Setter
    public boolean onGround;

    public boolean isPost() {
        return !this.isPre();
    }

    @Generated
    public MotionEvent(boolean bl, double d, double d2, double d3, float f, float f2, boolean bl2) {
        this.pre = bl;
        this.x = d;
        this.y = d2;
        this.z = d3;
        this.yaw = f;
        this.pitch = f2;
        this.onGround = bl2;
    }
}