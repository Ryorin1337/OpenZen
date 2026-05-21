package shit.zen.utils.game;

import lombok.Generated;
import shit.zen.utils.math.MathUtil;

public final class CpsUtil {
    public static long toDelayMs(String string, double d) {
        if (string.equals("DBC")) {
            return (long)(500.0 / d + MathUtil.randomDouble(-50.0, 50.0));
        }
        return (long)(1000.0 / d + MathUtil.randomDouble(-25.0, 25.0));
    }

    public static long toDelayMs(double d) {
        return (long)(1000.0 / d);
    }

    public static long toDelayMsWithJitter(double d, double d2) {
        double d3 = 1000.0 / d;
        return (long)(d3 + MathUtil.randomDouble(-d2, d2));
    }

    @Generated
    private CpsUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}