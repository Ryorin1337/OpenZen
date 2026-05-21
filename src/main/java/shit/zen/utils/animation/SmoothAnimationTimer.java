package shit.zen.utils.animation;

import shit.zen.utils.animation.AnimationTimer;
import shit.zen.utils.math.Easing;
import shit.zen.utils.math.Easings;

public class SmoothAnimationTimer
extends AnimationTimer {
    public void animate(double d, double d2) {
        this.animate(d, d2, Easings.EASE_OUT_QUAD);
    }

    public void animate(double d, double d2, Easing easing) {
        this.animate(d, d2, easing, false);
    }

    public void animate(double d, double d2, Easing easing, boolean bl) {
        this.setDebug(bl);
        if (this.isAnimating() && (d == this.getFromValue() || d == this.getToValue() || d == (double)this.getValueF())) {
            if (this.isDebug()) {
                System.out.println("Animate cancelled due to valueTo equals fromValue");
            }
            return;
        }
        this.setEasing(easing);
        this.setDuration(d2 * 1000.0);
        this.setStartTime(System.currentTimeMillis());
        this.setFromValue(this.getValueF());
        this.setToValue(d);
        if (this.isDebug()) {
            double d3 = this.getDuration();
            float f = this.getValueF();
            double d4 = this.getToValue();
            System.out.println("#animate {\n    to value: " + d4 + "\n    from value: " + f + "\n    duration: " + d3 + "\n}");
        }
    }
}