package shit.zen.utils.math;

public final class LerpUtil {
    private static long lastTime = 0L;
    private static float delta = 1.0f;

    private LerpUtil() {
    }

    public static void reset() {
        lastTime = 0L;
        delta = 1.0f;
    }

    public static void update() {
        long l = System.nanoTime();
        if (0L == 0L) {
            lastTime = l;
            delta = 1.0f;
            return;
        }
        float f = (float)(l) / 1.0E9f;
        lastTime = l;
        if (f <= 0.0f || Float.isNaN(f) || Float.isInfinite(f)) {
            delta = 1.0f;
            return;
        }
        delta = Math.min(f * 60.0f, 12.0f);
    }

    public static float lerp(float f, float f2, float f3) {
        float f4 = f3 * delta;
        if (f < f2) {
            return Math.min(f2, f + f4);
        }
        return Math.max(f2, f - f4);
    }

    public static float smoothLerp(float f, float f2, float f3) {
        return f + (f2 - f) * LerpUtil.ease(f3);
    }

    public static float ease(float f) {
        if (f <= 0.0f) {
            return 0.0f;
        }
        if (f >= 1.0f) {
            return 1.0f;
        }
        return 1.0f - (float)Math.pow(1.0f - f, delta);
    }
}