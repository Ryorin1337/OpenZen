package shit.zen.event.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.Generated;
import shit.zen.event.EventMarker;

public class StrafeEvent
implements EventMarker {
    @Getter @Setter
    private float forward;
    @Getter @Setter
    private float strafe;
    @Getter @Setter
    private boolean sprinting;

    @Generated
    public StrafeEvent(float f, float f2, boolean bl) {
        this.forward = f;
        this.strafe = f2;
        this.sprinting = bl;
    }
}