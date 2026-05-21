package shit.zen.utils.misc;

import java.util.HashMap;
import java.util.Map;
import org.lwjgl.glfw.GLFW;
import shit.zen.ClientBase;

public class CursorUtil {
    private static final Map<Integer, Long> cursorCache = new HashMap<>();
    static long windowHandle = ClientBase.mc.getWindow().getWindow();

    public static boolean isInBounds(float f, float f2, float f3, float f4, float f5, float f6) {
        return f > f3 && f2 > f4 && f < f3 + f5 && f2 < f4 + f6;
    }

    public static void setCursor(int n) {
        long l = cursorCache.computeIfAbsent(n, GLFW::glfwCreateStandardCursor);
        GLFW.glfwSetCursor(windowHandle, l);
    }

    public static void setDefaultCursor() {
        CursorUtil.setCursor(221185);
    }

    public static void destroyCursors() {
        cursorCache.values().forEach(GLFW::glfwDestroyCursor);
    }
}