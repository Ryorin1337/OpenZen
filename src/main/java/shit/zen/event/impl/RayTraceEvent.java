package shit.zen.event.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.Generated;
import net.minecraft.world.entity.Entity;
import shit.zen.event.EventMarker;

import java.util.Objects;

public class RayTraceEvent
implements EventMarker {
    @Getter @Setter
    public Entity entity;
    @Getter @Setter
    public float range;
    @Getter @Setter
    public float blockRange;

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof RayTraceEvent rayTraceEvent)) {
            return false;
        }
        if (!rayTraceEvent.canEqual(this)) {
            return false;
        }
        if (Float.compare(this.getRange(), rayTraceEvent.getRange()) != 0) {
            return false;
        }
        if (Float.compare(this.getBlockRange(), rayTraceEvent.getBlockRange()) != 0) {
            return false;
        }
        Entity entity = this.getEntity();
        Entity entity2 = rayTraceEvent.getEntity();
        return !(!Objects.equals(entity, entity2));
    }

    @Generated
    protected boolean canEqual(Object object) {
        return object instanceof RayTraceEvent;
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        n2 = n2 * 59 + Float.floatToIntBits(this.getRange());
        n2 = n2 * 59 + Float.floatToIntBits(this.getBlockRange());
        Entity entity = this.getEntity();
        n2 = n2 * 59 + (entity == null ? 43 : entity.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        float f = this.getBlockRange();
        float f2 = this.getRange();
        String string = String.valueOf(this.getEntity());
        return "RayTraceEvent(entity=" + string + ", yaw=" + f2 + ", pitch=" + f + ")";
    }

    @Generated
    public RayTraceEvent(Entity entity, float f, float f2) {
        this.entity = entity;
        this.range = f;
        this.blockRange = f2;
    }
}