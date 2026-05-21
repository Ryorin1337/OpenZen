package shit.zen.utils.math;

import shit.zen.utils.math.Easing;

public class Easings {
    public static final Easing BACK_OUT = d -> 1.0 + 2.70158 * Math.pow(d - 1.0, 3.0) + 1.70158 * Math.pow(d - 1.0, 2.0);
    public static final Easing EASE_OUT_QUAD = d -> 1.0 - (d - 1.0) * (d - 1.0);
    public static final Easing EASE_OUT_POW2 = Easings.easeOut(2);
    public static final Easing EASE_IN_POW3 = Easings.easeIn(3);
    public static final Easing EASE_OUT_POW3 = Easings.easeOut(3);
    public static final Easing EASE_OUT_POW4 = Easings.easeOut(4);
    public static final Easing EASE_OUT_POW5 = Easings.easeOut(5);
    public static final Easing EASE_OUT_SINE = d -> Math.sin(d * Math.PI / 2.0);
    public static final Easing EASE_OUT_ELASTIC = d -> {
        if (d == 0.0 || d == 1.0) {
            return d;
        }
        return Math.pow(2.0, -10.0 * d) * Math.sin((d * 10.0 - 0.75) * 2.0943951023931953) + 1.0;
    };
    public static final Easing EASE_OUT_BOUNCE = d -> {
        double d2 = 7.5625;
        double d3 = 2.75;
        if (d < 1.0 / d3) {
            return d2 * Math.pow(d, 2.0);
        }
        if (d < 2.0 / d3) {
            return d2 * Math.pow(d - 1.5 / d3, 2.0) + 0.75;
        }
        if (d < 2.5 / d3) {
            return d2 * Math.pow(d - 2.25 / d3, 2.0) + 0.9375;
        }
        return d2 * Math.pow(d - 2.625 / d3, 2.0) + 0.984375;
    };

    private Easings() {
    }

    public static Easing easeIn(double d) {
        return d2 -> Math.pow(d2, d);
    }

    public static Easing easeIn(int n) {
        return Easings.easeIn((double)n);
    }

    public static Easing easeOut(double d) {
        return d2 -> 1.0 - Math.pow(1.0 - d2, d);
    }

    public static Easing easeOut(int n) {
        return Easings.easeOut((double)n);
    }

    public static Easing easeInOut(double d) {
        return d2 -> {
            if (d2 < 0.5) {
                return Math.pow(2.0, d - 1.0) * Math.pow(d2, d);
            }
            return 1.0 - Math.pow(-2.0 * d2 + 2.0, d) / 2.0;
        };
    }

    private static /* synthetic */ double easeInOutBounce(double d) {
        if (d < 0.5) {
            return (1.0 - EASE_OUT_BOUNCE.ease(1.0 - 2.0 * d)) / 2.0;
        }
        return (1.0 + EASE_OUT_BOUNCE.ease(2.0 * d - 1.0)) / 2.0;
    }

    private static /* synthetic */ double easeInBounce(double d) {
        return 1.0 - EASE_OUT_BOUNCE.ease(1.0 - d);
    }

    private static /* synthetic */ double easeInOutExpo(double d) {
        if (d == 0.0 || d == 1.0) {
            return d;
        }
        if (d < 0.5) {
            return Math.pow(2.0, 20.0 * d - 10.0) / 2.0;
        }
        return (2.0 - Math.pow(2.0, -20.0 * d + 10.0)) / 2.0;
    }

    private static /* synthetic */ double easeOutExpo(double d) {
        if (d != 1.0) {
            return 1.0 - Math.pow(2.0, -10.0 * d);
        }
        return d;
    }

    private static /* synthetic */ double easeInExpo(double d) {
        if (d != 0.0) {
            return Math.pow(2.0, 10.0 * d - 10.0);
        }
        return d;
    }

    private static /* synthetic */ double easeInOutElastic(double d) {
        if (d == 0.0 || d == 1.0) {
            return d;
        }
        if (d < 0.5) {
            return -(Math.pow(2.0, 20.0 * d - 10.0) * Math.sin((20.0 * d - 11.125) * 1.3962634015954636)) / 2.0;
        }
        return Math.pow(2.0, -20.0 * d + 10.0) * Math.sin((20.0 * d - 11.125) * 1.3962634015954636) / 2.0 + 1.0;
    }

    private static /* synthetic */ double easeInElastic(double d) {
        if (d == 0.0 || d == 1.0) {
            return d;
        }
        return Math.pow(-2.0, 10.0 * d - 10.0) * Math.sin((d * 10.0 - 10.75) * 2.0943951023931953);
    }

    private static /* synthetic */ double easeInOutCirc(double d) {
        if (d < 0.5) {
            return (1.0 - Math.sqrt(1.0 - Math.pow(2.0 * d, 2.0))) / 2.0;
        }
        return (Math.sqrt(1.0 - Math.pow(-2.0 * d + 2.0, 2.0)) + 1.0) / 2.0;
    }

    private static /* synthetic */ double easeOutCirc(double d) {
        return Math.sqrt(1.0 - Math.pow(d - 1.0, 2.0));
    }

    private static /* synthetic */ double easeInCirc(double d) {
        return 1.0 - Math.sqrt(1.0 - Math.pow(d, 2.0));
    }

    private static /* synthetic */ double easeInOutSine(double d) {
        return -(Math.cos(Math.PI * d) - 1.0) / 2.0;
    }

    private static /* synthetic */ double easeInSine(double d) {
        return 1.0 - Math.cos(d * Math.PI / 2.0);
    }

    private static /* synthetic */ double easeInBack(double d) {
        return 2.70158 * Math.pow(d, 3.0) - 1.70158 * Math.pow(d, 2.0);
    }

    private static /* synthetic */ double easeInOutBack(double d) {
        if (d < 0.5) {
            return Math.pow(2.0 * d, 2.0) * (7.189819 * d - 2.5949095) / 2.0;
        }
        return (Math.pow(2.0 * d - 2.0, 2.0) * (3.5949095 * (d * 2.0 - 2.0) + 2.5949095) + 2.0) / 2.0;
    }
}