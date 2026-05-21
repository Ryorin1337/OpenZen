package shit.zen.event.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.Generated;
import shit.zen.event.EventMarker;

public class FallFlyingEvent
implements EventMarker {
    @Getter @Setter
    private float speed;
    private static final String TO_STRING_PREFIX = "FallFlyingEvent(pitch=";

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof FallFlyingEvent fallFlyingEvent)) {
            return false;
        }
        if (!fallFlyingEvent.canEqual(this)) {
            return false;
        }
        return Float.compare(this.getSpeed(), fallFlyingEvent.getSpeed()) == 0;
    }

    @Generated
    protected boolean canEqual(Object object) {
        return object instanceof FallFlyingEvent;
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        n2 = n2 * 59 + Float.floatToIntBits(this.getSpeed());
        return n2;
    }

    @Generated
    public String toString() {
        return TO_STRING_PREFIX + this.getSpeed() + ")";
    }

    @Generated
    public FallFlyingEvent(float f) {
        this.speed = f;
    }
}