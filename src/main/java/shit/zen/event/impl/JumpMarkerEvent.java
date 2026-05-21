package shit.zen.event.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.Generated;
import shit.zen.event.EventMarker;

public class JumpMarkerEvent
implements EventMarker {
    @Getter @Setter
    private float jumpHeight;
    private static final String TO_STRING_PREFIX = "JumpEvent(yaw=";

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof JumpMarkerEvent jumpMarkerEvent)) {
            return false;
        }
        if (!jumpMarkerEvent.canEqual(this)) {
            return false;
        }
        return Float.compare(this.getJumpHeight(), jumpMarkerEvent.getJumpHeight()) == 0;
    }

    @Generated
    protected boolean canEqual(Object object) {
        return object instanceof JumpMarkerEvent;
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        n2 = n2 * 59 + Float.floatToIntBits(this.getJumpHeight());
        return n2;
    }

    @Generated
    public String toString() {
        return TO_STRING_PREFIX + this.getJumpHeight() + ")";
    }

    @Generated
    public JumpMarkerEvent(float f) {
        this.jumpHeight = f;
    }
}