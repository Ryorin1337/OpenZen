package shit.zen.render;

public record Rectangle(float x1, float y1, float x2, float y2) {

    public static Rectangle ofXYWH(float f, float f2, float f3, float f4) {
        return new Rectangle(f, f2, f + f3, f2 + f4);
    }

    public static Rectangle ofCorners(float f, float f2, float f3, float f4) {
        return new Rectangle(f, f2, f3, f4);
    }

    public float getWidth() {
        return this.x2 - this.x1;
    }

    public float getHeight() {
        return this.y2 - this.y1;
    }

    public float getX() {
        return this.x1;
    }

    public float getY() {
        return this.y1;
    }

    public float getRight() {
        return this.x2;
    }

    public float getBottom() {
        return this.y2;
    }
}