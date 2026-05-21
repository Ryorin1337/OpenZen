package shit.zen.utils.animation;

import net.minecraft.util.Mth;

public class Timer {
    private long time = System.currentTimeMillis();

    public void reset() {
        this.time = System.currentTimeMillis();
    }

    public void setTime(long l) {
        this.time = l;
    }

    public void setTimeMs(long l) {
        this.time = l;
    }

    public boolean hasPassedReset(long l, boolean bl) {
        if (System.currentTimeMillis() - this.time > l) {
            if (bl) {
                this.reset();
            }
            return true;
        }
        return false;
    }

    public boolean hasPassed(float f) {
        return (float)(System.currentTimeMillis() - this.time) >= f;
    }

    public boolean hasPassedDouble(double d, boolean bl) {
        boolean bl2;
        boolean bl3 = bl2 = (double)Mth.clamp((float)(System.currentTimeMillis() - this.time), 0.0f, (float)d) >= d;
        if (bl2 && bl) {
            this.reset();
        }
        return bl2;
    }

    public long getElapsed() {
        return System.currentTimeMillis() - this.time;
    }

    public boolean hasPassed(long l) {
        return System.currentTimeMillis() - this.time > l;
    }

    public boolean hasPassedOrEqual(long l) {
        return System.currentTimeMillis() - this.time >= l;
    }
}