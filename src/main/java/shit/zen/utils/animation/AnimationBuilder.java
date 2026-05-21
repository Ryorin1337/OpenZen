package shit.zen.utils.animation;

import java.util.LinkedList;
import java.util.Optional;
import shit.zen.utils.animation.AnimationTimer;
import shit.zen.utils.animation.NamedAnimationTimer;
import shit.zen.utils.math.Easing;
import shit.zen.utils.math.Easings;

public class AnimationBuilder {
    private final LinkedList<NamedAnimationTimer> timers = new LinkedList<>();
    private double duration = 1.0;
    private Easing easing = Easings.EASE_OUT_QUAD;
    private boolean debug = false;

    public NamedAnimationTimer getOrCreate(String string) {
        NamedAnimationTimer namedAnimationTimer2;
        Optional<NamedAnimationTimer> optional = this.timers.stream().filter(namedAnimationTimer -> namedAnimationTimer.getName().equalsIgnoreCase(string)).findFirst();
        if (!optional.isPresent()) {
            namedAnimationTimer2 = new NamedAnimationTimer(string, this);
            this.timers.add(namedAnimationTimer2);
        } else {
            namedAnimationTimer2 = optional.get();
        }
        return namedAnimationTimer2;
    }

    public AnimationBuilder animate(String string, double d, double d2) {
        return this.animate(string, d, d2, Easings.EASE_OUT_QUAD);
    }

    public AnimationBuilder animate(String string, double d, double d2, Easing easing) {
        return this.animate(string, d, d2, easing, false);
    }

    private AnimationBuilder animate(String string, double d, double d2, Easing easing, boolean bl) {
        this.setDuration(d2);
        this.setEasing(easing);
        this.setDebug(bl);
        this.getOrCreate(string).setCurrentValue(d);
        return this;
    }

    public AnimationBuilder withTask(double d, Runnable runnable) {
        return this.withTask(d, this.easing, runnable);
    }

    public AnimationBuilder withTask(double d, Easing easing, Runnable runnable) {
        return this.withTask(d, easing, false, runnable);
    }

    private AnimationBuilder withTask(double d, Easing easing, boolean bl, Runnable runnable) {
        this.setDuration(d);
        this.setEasing(easing);
        this.setDebug(bl);
        runnable.run();
        return this;
    }

    public boolean tick() {
        this.getTimers().forEach(AnimationTimer::tick);
        return this.getTimers().stream().anyMatch(AnimationTimer::isAnimating);
    }

    public void setDuration(double d) {
        this.duration = d;
    }

    public void setDebug(boolean bl) {
        this.debug = bl;
    }

    public void setEasing(Easing easing) {
        this.easing = easing;
    }

    public double getDuration() {
        return this.duration;
    }

    public LinkedList<NamedAnimationTimer> getTimers() {
        return this.timers;
    }

    public Easing getEasing() {
        return this.easing;
    }

    public boolean isDebug() {
        return this.debug;
    }
}