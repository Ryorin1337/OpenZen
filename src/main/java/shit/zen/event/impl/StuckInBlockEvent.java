package shit.zen.event.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.Generated;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import shit.zen.event.Event;

import java.util.Objects;

public class StuckInBlockEvent
extends Event {
    @Getter @Setter
    private BlockState blockState;
    @Getter @Setter
    private Vec3 motion;

    @Generated
    public String toString() {
        return "StuckInBlockEvent(state=" + this.getBlockState() + ", stuckSpeedMultiplier=" + this.getMotion() + ")";
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof StuckInBlockEvent stuckInBlockEvent)) {
            return false;
        }
        if (!stuckInBlockEvent.canEqual(this)) {
            return false;
        }
        BlockState blockState = this.getBlockState();
        BlockState blockState2 = stuckInBlockEvent.getBlockState();
        if (!Objects.equals(blockState, blockState2)) {
            return false;
        }
        Vec3 vec3 = this.getMotion();
        Vec3 vec32 = stuckInBlockEvent.getMotion();
        return !(!Objects.equals(vec3, vec32));
    }

    @Generated
    protected boolean canEqual(Object object) {
        return object instanceof StuckInBlockEvent;
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        BlockState blockState = this.getBlockState();
        n2 = n2 * 59 + (blockState == null ? 43 : blockState.hashCode());
        Vec3 vec3 = this.getMotion();
        n2 = n2 * 59 + (vec3 == null ? 43 : vec3.hashCode());
        return n2;
    }

    @Generated
    public StuckInBlockEvent(BlockState blockState, Vec3 vec3) {
        this.blockState = blockState;
        this.motion = vec3;
    }
}