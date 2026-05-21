package shit.zen.event.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.Generated;
import shit.zen.event.EventMarker;

public class HeadTurnEvent
implements EventMarker {
    @Getter @Setter
    private float yaw;
    @Getter @Setter
    private float pitch;

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof HeadTurnEvent headTurnEvent)) {
            return false;
        }
        if (!headTurnEvent.canEqual(this)) {
            return false;
        }
        if (Float.compare(this.getYaw(), headTurnEvent.getYaw()) != 0) {
            return false;
        }
        return Float.compare(this.getPitch(), headTurnEvent.getPitch()) == 0;
    }

    @Generated
    protected boolean canEqual(Object object) {
        return object instanceof HeadTurnEvent;
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        n2 = n2 * 59 + Float.floatToIntBits(this.getYaw());
        n2 = n2 * 59 + Float.floatToIntBits(this.getPitch());
        return n2;
    }

    @Generated
    public String toString() {
        return "HeadTurnEvent(yaw=" + this.getYaw() + ", lastYaw=" + this.getPitch() + ")";
    }

    @Generated
    public HeadTurnEvent(float f, float f2) {
        this.yaw = f;
        this.pitch = f2;
    }
}