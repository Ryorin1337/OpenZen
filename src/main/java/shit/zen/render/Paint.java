package shit.zen.render;

public class Paint
implements AutoCloseable {
    public enum StrokeCap { FILL, STROKE, FILL_AND_STROKE, STROKE_AND_FILL }
    public enum StrokeJoin { BUTT, ROUND, MITER }

    public static class GradientCoords {
        public float x1, y1, x2, y2;
        public int color1, color2;

        public GradientCoords(float x1, float y1, float x2, float y2, int color1, int color2) {
            this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
            this.color1 = color1; this.color2 = color2;
        }
    }

    public static class LinearGradient implements AutoCloseable {
        public float[] colors;
        public float angle;

        public LinearGradient(float[] colors, float angle) {
            this.colors = colors;
            this.angle = angle;
        }

        @Override
        public void close() {
        }
    }

    public record BlurMaskFilter(float blurRadius) {
    }

    private int color = -1;
    private Paint.StrokeCap capStyle = Paint.StrokeCap.FILL;
    private Paint.StrokeJoin joinStyle = Paint.StrokeJoin.BUTT;
    private float strokeWidth = 1.0f;
    private float blurRadius = 0.0f;
    private GradientCoords gradCoords;
    private LinearGradient linGradient;
    private Object shader;
    private boolean antialias = true;

    public Paint setColor(int n) {
        this.color = n;
        return this;
    }

    public Paint setColorFromArray(float[] fArray) {
        int n = (int)Math.max(0.0f, Math.min(255.0f, fArray[3] * 255.0f));
        int n2 = (int)Math.max(0.0f, Math.min(255.0f, fArray[0] * 255.0f));
        int n3 = (int)Math.max(0.0f, Math.min(255.0f, fArray[1] * 255.0f));
        int n4 = (int)Math.max(0.0f, Math.min(255.0f, fArray[2] * 255.0f));
        this.color = n << 24 | n2 << 16 | n3 << 8 | n4;
        return this;
    }

    public Paint setColorARGB(int n, int n2, int n3, int n4) {
        this.color = (n & 0xFF) << 24 | (n2 & 0xFF) << 16 | (n3 & 0xFF) << 8 | n4 & 0xFF;
        return this;
    }

    public Paint setAlpha(float f) {
        int n = (int)Math.max(0.0f, Math.min(255.0f, f * 255.0f));
        this.color = this.color & 0xFFFFFF | n << 24;
        return this;
    }

    public Paint setStrokeWidth(float f) {
        this.strokeWidth = f;
        return this;
    }

    public Paint setStrokeCap(Paint.StrokeCap paint$StrokeCap) {
        this.capStyle = paint$StrokeCap;
        return this;
    }

    public Paint setStrokeJoin(Paint.StrokeJoin paint$StrokeJoin) {
        this.joinStyle = paint$StrokeJoin;
        return this;
    }

    public Paint setMaskFilter(Object object) {
        if (object instanceof Paint.BlurMaskFilter) {
            this.blurRadius = ((Paint.BlurMaskFilter)object).blurRadius;
        } else if (object == null) {
            this.blurRadius = 0.0f;
        }
        return this;
    }

    public Paint setGradCoords(GradientCoords gradientCoords) {
        this.gradCoords = gradientCoords;
        return this;
    }

    public Paint setLinGradient(LinearGradient linearGradient) {
        this.linGradient = linearGradient;
        return this;
    }

    public Paint setShader(Object object) {
        this.shader = object;
        return this;
    }

    public Paint setAntialias(boolean bl) {
        this.antialias = bl;
        return this;
    }

    public int getColor() {
        return this.color;
    }

    public Paint.StrokeCap getCapStyle() {
        return this.capStyle;
    }

    public Paint.StrokeJoin getJoinStyle() {
        return this.joinStyle;
    }

    public float getStrokeWidth() {
        return this.strokeWidth;
    }

    public float getBlurRadius() {
        return this.blurRadius;
    }

    public GradientCoords getGradCoords() {
        return this.gradCoords;
    }

    public LinearGradient getLinGradient() {
        return this.linGradient;
    }

    public Object getShader() {
        return this.shader;
    }

    public boolean isAntialias() {
        return this.antialias;
    }

    public void close() {
    }

    public Paint copy() {
        Paint paint = new Paint();
        paint.color = this.color;
        paint.capStyle = Paint.StrokeCap.FILL;
        paint.joinStyle = this.joinStyle;
        paint.strokeWidth = this.strokeWidth;
        paint.blurRadius = this.blurRadius;
        paint.gradCoords = this.gradCoords;
        paint.linGradient = this.linGradient;
        paint.shader = this.shader;
        paint.antialias = this.antialias;
        return paint;
    }
}