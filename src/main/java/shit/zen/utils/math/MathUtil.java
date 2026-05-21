package shit.zen.utils.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Random;
import lombok.Generated;
import shit.zen.ClientBase;

public final class MathUtil
extends ClientBase {
    public static final Random RANDOM;
    private static final String UTILITY_MSG;

    public static double clamp(double d, double d2, double d3) {
        return Math.min(Math.max(d, d2), d3);
    }

    public static double round(double d, int n) {
        if (n == 0) {
            return Math.floor(d);
        }
        double d2 = Math.pow(10.0, n);
        return (double)Math.round(d * d2) / d2;
    }

    public static double randomInt(int n, int n2) {
        return n >= n2 ? (double)n : (double)(RANDOM.nextInt() * (n2 - n) + n);
    }

    public static double randomDouble(double d, double d2) {
        return d >= d2 ? d : RANDOM.nextDouble() * (d2 - d) + d;
    }

    public static double snap(double d, double d2) {
        double d3 = (double)Math.round(d / d2) * d2;
        d3 *= 1000.0;
        d3 = (int)d3;
        return d3 /= 1000.0;
    }

    public static double roundDecimal(double d, int n) {
        if (n < 0) {
            return d;
        }
        BigDecimal bigDecimal = new BigDecimal(d);
        bigDecimal = bigDecimal.setScale(n, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    public static float randomFloat(float f, float f2) {
        SecureRandom secureRandom = new SecureRandom();
        return secureRandom.nextFloat() * (f - f2) + f2;
    }

    public static float clampPitch(float f) {
        if (f > 90.0f) {
            return 90.0f;
        }
        if (f < -90.0f) {
            return -90.0f;
        }
        return f;
    }

    public static float clamp(float f, float f2, float f3) {
        return f < f2 ? f2 : Math.min(f, f3);
    }

    public static float lerp(float f, float f2, float f3) {
        return f2 + f * (f3 - f2);
    }

    public static double lerp(double d, double d2, float f) {
        return d2 + d * ((double)f - d2);
    }

    public static double lerp(float f, double d, double d2) {
        return d + (double)f * (d2 - d);
    }

    public static int lerpColor(int n, int n2, float f) {
        f = Math.max(0.0f, Math.min(1.0f, f));
        int n3 = n >> 24 & 0xFF;
        int n4 = n >> 16 & 0xFF;
        int n5 = n >> 8 & 0xFF;
        int n6 = n & 0xFF;
        int n7 = n2 >> 24 & 0xFF;
        int n8 = n2 >> 16 & 0xFF;
        int n9 = n2 >> 8 & 0xFF;
        int n10 = n2 & 0xFF;
        int n11 = (int)((float)n3 * (1.0f - f) + (float)n7 * f);
        int n12 = (int)((float)n4 * (1.0f - f) + (float)n8 * f);
        int n13 = (int)((float)n5 * (1.0f - f) + (float)n9 * f);
        int n14 = (int)((float)n6 * (1.0f - f) + (float)n10 * f);
        return n11 << 24 | n12 << 16 | n13 << 8 | n14;
    }

    @Generated
    private MathUtil() {
        throw new UnsupportedOperationException(UTILITY_MSG);
    }

    static {
        UTILITY_MSG = "This is a utility class and cannot be instantiated";
        RANDOM = new Random();
    }
}