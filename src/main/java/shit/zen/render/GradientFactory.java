package shit.zen.render;

import shit.zen.render.Paint.LinearGradient;

public final class GradientFactory {
    public static Paint.LinearGradient buildLinearGradient(float[] fArray, float f) {
        return new Paint.LinearGradient(fArray, f);
    }
}