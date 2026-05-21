package shit.zen.render;

public final class RoundedRectangle {
    public final float x1;
    public final float y1;
    public final float x2;
    public final float y2;
    public final float topLeftRadius;
    public final float topRightRadius;
    public final float bottomRightRadius;
    public final float bottomLeftRadius;

    private RoundedRectangle(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
        this.x1 = f;
        this.y1 = f2;
        this.x2 = f3;
        this.y2 = f4;
        this.topLeftRadius = f5;
        this.topRightRadius = f6;
        this.bottomRightRadius = f7;
        this.bottomLeftRadius = f8;
    }

    public static RoundedRectangle ofXYWHR(float f, float f2, float f3, float f4, float f5) {
        return new RoundedRectangle(f, f2, f + f3, f2 + f4, f5, f5, f5, f5);
    }

    public static RoundedRectangle ofXYWHRadii(float f, float f2, float f3, float f4, float[] fArray) {
        if (fArray.length >= 8) {
            return new RoundedRectangle(f, f2, f + f3, f2 + f4, fArray[0], fArray[2], fArray[4], fArray[6]);
        }
        if (fArray.length >= 4) {
            return new RoundedRectangle(f, f2, f + f3, f2 + f4, fArray[0], fArray[1], fArray[2], fArray[3]);
        }
        float f5 = fArray.length > 0 ? fArray[0] : 0.0f;
        return new RoundedRectangle(f, f2, f + f3, f2 + f4, f5, f5, f5, f5);
    }

    public float getWidth() {
        return this.x2 - this.x1;
    }

    public float getHeight() {
        return this.y2 - this.y1;
    }
}