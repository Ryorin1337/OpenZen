package shit.zen.utils.animation;

import shit.zen.utils.animation.AnimationBuilder;
import shit.zen.utils.animation.AnimationTimer;

public class NamedAnimationTimer
extends AnimationTimer {
    private final String name;
    private final AnimationBuilder builder;

    public NamedAnimationTimer(String string, AnimationBuilder animationBuilder) {
        this.name = string;
        this.builder = animationBuilder;
    }

    @Override
    public void setCurrentValue(double d) {
        if (d == (double)this.getValueF()) {
            return;
        }
        this.setDebug(this.getBuilder().isDebug());
        if (this.isAnimating() && (d == this.getFromValue() || d == this.getToValue() || d == (double)this.getValueF())) {
            if (this.isDebug()) {
                System.out.println("Animating " + this.name + " cancelled due to valueTo equals fromValue");
            }
            return;
        }
        this.setEasing(this.getBuilder().getEasing());
        this.setDuration(this.getBuilder().getDuration() * 1000.0);
        this.setStartTime(System.currentTimeMillis());
        this.setFromValue(this.getValueF());
        this.setToValue(d);
        if (this.isDebug()) {
            double d2 = this.getDuration();
            float f = this.getValueF();
            double d3 = this.getToValue();
            System.out.println(this.name + "#animate {\n    to value: " + d3 + "\n    from value: " + f + "\n    duration: " + d2 + "\n}");
        }
    }

    public AnimationBuilder getBuilder() {
        return this.builder;
    }

    public String getName() {
        return this.name;
    }
}