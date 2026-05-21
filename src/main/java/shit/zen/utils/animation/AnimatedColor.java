package shit.zen.utils.animation;

import lombok.Getter;
import lombok.Setter;
import shit.zen.utils.animation.SmoothAnimationTimer;
import shit.zen.utils.math.Easing;
import shit.zen.utils.math.Easings;
import shit.zen.utils.render.ColorUtil;

public class AnimatedColor {
    @Getter @Setter
    private int color;
    @Getter @Setter
    private SmoothAnimationTimer rTimer = new SmoothAnimationTimer();
    @Getter @Setter
    private SmoothAnimationTimer gTimer = new SmoothAnimationTimer();
    @Getter @Setter
    private SmoothAnimationTimer bTimer = new SmoothAnimationTimer();
    @Getter @Setter
    private SmoothAnimationTimer aTimer = new SmoothAnimationTimer();

    public AnimatedColor(int n) {
        this.color = n;
    }

    public void animateTo(int n, float f) {
        this.animateTo(n, f, Easings.EASE_OUT_QUAD);
    }

    public void animateTo(int n, float f, Easing easing) {
        this.rTimer.animate(ColorUtil.getRed(n), 0.2 / (double)f, easing);
        this.gTimer.animate(ColorUtil.getGreen(n), 0.2 / (double)f, easing);
        this.bTimer.animate(ColorUtil.getBlue(n), 0.2 / (double)f, easing);
        this.aTimer.animate(ColorUtil.getAlpha(n), 0.2 / (double)f, easing);
        this.rTimer.tick();
        this.gTimer.tick();
        this.bTimer.tick();
        this.aTimer.tick();
        this.color = ColorUtil.fromARGB(this.rTimer.getValueI(), this.gTimer.getValueI(), this.bTimer.getValueI(), this.aTimer.getValueI());
    }

    }