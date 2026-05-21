package shit.zen.utils.animation;

import java.util.ArrayList;
import java.util.List;

public class TickTimer {
    private static final List<TickTimer> instances = new ArrayList<>();
    private int ticks = 0;

    public static void tickAll() {
        for (TickTimer tickTimer : instances) {
            ++tickTimer.ticks;
        }
    }

    public TickTimer() {
        instances.add(this);
    }

    public boolean hasPassed(int n) {
        return this.ticks >= n;
    }

    public boolean hasPassed(float f) {
        return (float)this.ticks >= f;
    }

    public void reset() {
        this.ticks = 0;
    }
}