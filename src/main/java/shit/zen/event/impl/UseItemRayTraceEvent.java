package shit.zen.event.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.Generated;
import shit.zen.event.EventMarker;

public class UseItemRayTraceEvent
implements EventMarker {
    @Getter @Setter
    private float range;
    @Getter @Setter
    private float blockRange;

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof UseItemRayTraceEvent useItemRayTraceEvent)) {
            return false;
        }
        if (!useItemRayTraceEvent.canEqual(this)) {
            return false;
        }
        if (Float.compare(this.getRange(), useItemRayTraceEvent.getRange()) != 0) {
            return false;
        }
        return Float.compare(this.getBlockRange(), useItemRayTraceEvent.getBlockRange()) == 0;
    }

    @Generated
    protected boolean canEqual(Object object) {
        return object instanceof UseItemRayTraceEvent;
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        n2 = n2 * 59 + Float.floatToIntBits(this.getRange());
        n2 = n2 * 59 + Float.floatToIntBits(this.getBlockRange());
        return n2;
    }

    @Generated
    public String toString() {
        return "UseItemRayTraceEvent(yaw=" + this.getRange() + ", pitch=" + this.getBlockRange() + ")";
    }

    @Generated
    public UseItemRayTraceEvent(float f, float f2) {
        this.range = f;
        this.blockRange = f2;
    }
}