package shit.zen.utils.render;

import java.awt.Color;
import lombok.Generated;
import net.minecraft.world.entity.player.Player;
import shit.zen.ClientBase;

public final class ColorUtil
extends ClientBase {
    private static final String UTILITY_CLASS_MSG = "This is a utility class and cannot be instantiated";

    public static Color getPlayerColor(Player player) {
        int n = player.getName().getString().hashCode();
        int n2 = (n & 0xFF0000) >> 16;
        int n3 = (n & 0xFF00) >> 8;
        int n4 = n & 0xFF;
        return new Color(n2, n3, n4);
    }

    public static int animateColor(int n, int n2, double d) {
        if (d > 1.0) {
            d = 1.0 - d % 1.0;
        }
        return ColorUtil.interpolateColor(n, n2, d);
    }

    public static int animateColorOffset(int n, int n2, long l) {
        return ColorUtil.animateColor(n, n2, (double)((System.currentTimeMillis() + l) % 4000L) / 2000.0);
    }

    public static int interpolateColor(int n, int n2, double d) {
        double d2 = 1.0 - d;
        int n3 = (int)((double)(n >> 16 & 0xFF) * d2 + (double)(n2 >> 16 & 0xFF) * d);
        int n4 = (int)((double)(n >> 8 & 0xFF) * d2 + (double)(n2 >> 8 & 0xFF) * d);
        int n5 = (int)((double)(n & 0xFF) * d2 + (double)(n2 & 0xFF) * d);
        int n6 = (int)((double)(n >> 24 & 0xFF) * d2 + (double)(n2 >> 24 & 0xFF) * d);
        return (n6 & 0xFF) << 24 | (n3 & 0xFF) << 16 | (n4 & 0xFF) << 8 | n5 & 0xFF;
    }

    public static Color getRainbowColor(int n, int n2) {
        int n3 = (int)((System.currentTimeMillis() / (long)n + (long)n2) % 360L);
        float f = (float)n3 / 360.0f;
        return new Color(Color.HSBtoRGB(f, 0.5f, 1.0f));
    }

    public static int fromRGB(int n, int n2, int n3) {
        return ColorUtil.fromARGB(n, n2, n3, 255);
    }

    public static int fromARGB(int n, int n2, int n3, int n4) {
        return (n4 & 0xFF) << 24 | (n & 0xFF) << 16 | (n2 & 0xFF) << 8 | n3 & 0xFF;
    }

    public static int withAlpha(int n, float f) {
        Color color = new Color(n);
        return ColorUtil.withAlphaColor(color, f).getRGB();
    }

    public static Color withAlphaColor(Color color, float f) {
        f = Math.min(1.0f, Math.max(0.0f, f));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)((float)color.getAlpha() * f));
    }

    public static int getAlpha(int n) {
        return n >> 24 & 0xFF;
    }

    public static int getRed(int n) {
        return n >> 16 & 0xFF;
    }

    public static int getGreen(int n) {
        return n >> 8 & 0xFF;
    }

    public static int getBlue(int n) {
        return n & 0xFF;
    }

    @Generated
    private ColorUtil() {
        throw new UnsupportedOperationException(UTILITY_CLASS_MSG);
    }
}