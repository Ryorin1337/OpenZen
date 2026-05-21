package shit.zen.event.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.Generated;
import shit.zen.event.EventMarker;

public class RotationEvent
implements EventMarker {
    @Getter @Setter
    private float yaw;
    @Getter @Setter
    private float pitch;

    @Generated
    public RotationEvent(float f, float f2) {
        this.yaw = f;
        this.pitch = f2;
    }
}