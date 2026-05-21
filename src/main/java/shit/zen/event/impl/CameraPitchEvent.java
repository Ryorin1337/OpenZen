package shit.zen.event.impl;

import lombok.Generated;
import shit.zen.event.EventMarker;

public class CameraPitchEvent
implements EventMarker {
    private float pitch;

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float f) {
        this.pitch = f;
    }

    @Generated
    public CameraPitchEvent(float f) {
        this.pitch = f;
    }
}