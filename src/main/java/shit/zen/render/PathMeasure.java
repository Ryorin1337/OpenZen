package shit.zen.render;

public final class PathMeasure
implements AutoCloseable {
    private final float totalLength;

    public PathMeasure(Path path) {
        float f = 0.0f;
        float f2 = 0.0f;
        float f3 = 0.0f;
        float f4 = 0.0f;
        float f5 = 0.0f;
        if (path != null) {
            block7: for (Path.PathSegment path$PathSegment : path.getSegments()) {
                switch (path$PathSegment.type) {
                    case MOVE_TO: {
                        f2 = path$PathSegment.coords[0];
                        f3 = path$PathSegment.coords[1];
                        f4 = f2;
                        f5 = f3;
                        continue block7;
                    }
                    case LINE_TO: {
                        float f6 = path$PathSegment.coords[0];
                        float f7 = path$PathSegment.coords[1];
                        f += (float)Math.hypot(f6 - f2, f7 - f3);
                        f2 = f6;
                        f3 = f7;
                        continue block7;
                    }
                    case QUAD_TO: {
                        float f6 = path$PathSegment.coords[0];
                        float f7 = path$PathSegment.coords[1];
                        float f8 = path$PathSegment.coords[2];
                        float f9 = path$PathSegment.coords[3];
                        f += PathMeasure.quadraticBezierLength(f2, f3, f6, f7, f8, f9);
                        f2 = f8;
                        f3 = f9;
                        continue block7;
                    }
                    case CUBIC_TO: {
                        float f6 = path$PathSegment.coords[0];
                        float f7 = path$PathSegment.coords[1];
                        float f8 = path$PathSegment.coords[2];
                        float f9 = path$PathSegment.coords[3];
                        float f10 = path$PathSegment.coords[4];
                        float f11 = path$PathSegment.coords[5];
                        f += PathMeasure.cubicBezierLength(f2, f3, f6, f7, f8, f9, f10, f11);
                        f2 = f10;
                        f3 = f11;
                        continue block7;
                    }
                    case CLOSE: {
                        f += (float)Math.hypot(f4 - f2, f5 - f3);
                        f2 = f4;
                        f3 = f5;
                        continue block7;
                    }
                }
            }
        }
        this.totalLength = f;
    }

    public float getLength() {
        return this.totalLength;
    }

    private static float quadraticBezierLength(float f, float f2, float f3, float f4, float f5, float f6) {
        float f7 = 0.0f;
        float f8 = f;
        float f9 = f2;
        int n = 16;
        for (int i = 1; i <= n; ++i) {
            float f10 = (float)i / (float)n;
            float f11 = 1.0f - f10;
            float f12 = f11 * f11 * f + 2.0f * f11 * f10 * f3 + f10 * f10 * f5;
            float f13 = f11 * f11 * f2 + 2.0f * f11 * f10 * f4 + f10 * f10 * f6;
            f7 += (float)Math.hypot(f12 - f8, f13 - f9);
            f8 = f12;
            f9 = f13;
        }
        return f7;
    }

    private static float cubicBezierLength(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
        float f9 = 0.0f;
        float f10 = f;
        float f11 = f2;
        int n = 24;
        for (int i = 1; i <= n; ++i) {
            float f12 = (float)i / (float)n;
            float f13 = 1.0f - f12;
            float f14 = f13 * f13 * f13 * f + 3.0f * f13 * f13 * f12 * f3 + 3.0f * f13 * f12 * f12 * f5 + f12 * f12 * f12 * f7;
            float f15 = f13 * f13 * f13 * f2 + 3.0f * f13 * f13 * f12 * f4 + 3.0f * f13 * f12 * f12 * f6 + f12 * f12 * f12 * f8;
            f9 += (float)Math.hypot(f14 - f10, f15 - f11);
            f10 = f14;
            f11 = f15;
        }
        return f9;
    }

    public void close() {
    }
}