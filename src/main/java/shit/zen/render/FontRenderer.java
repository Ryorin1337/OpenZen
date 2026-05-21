package shit.zen.render;

import shit.zen.render.CustomFont;
import shit.zen.render.FontMetricsImpl;
import shit.zen.render.Fonts;
import shit.zen.render.GlyphMetrics;
import shit.zen.render.Path;
import shit.zen.render.Rectangle;

public final class FontRenderer {
    private final String fontName;
    private final float size;
    private CustomFont customFont;

    public FontRenderer(String string, float f) {
        this.fontName = string;
        this.size = f;
    }

    public FontRenderer(CustomFont customFont, float f) {
        this.fontName = null;
        this.size = f;
        this.customFont = customFont;
    }

    public String getFontName() {
        return this.fontName;
    }

    public float getSize() {
        return this.size;
    }

    public CustomFont getFont() {
        if (this.customFont == null && this.fontName != null) {
            this.customFont = Fonts.getCustomFont(this.fontName, this.size);
        }
        return this.customFont;
    }

    public int getId() {
        CustomFont customFont = this.getFont();
        return customFont == null ? 0 : System.identityHashCode(customFont);
    }

    public int getHeight() {
        return this.getId();
    }

    public FontRenderer withBold(boolean bl) {
        return this;
    }

    public FontRenderer withItalic(boolean bl) {
        return this;
    }

    public FontRenderer withColor(Object object) {
        return this;
    }

    public Rectangle getBounds(String string) {
        if (string == null || string.isEmpty()) {
            return Rectangle.ofXYWH(0.0f, 0.0f, 0.0f, 0.0f);
        }
        CustomFont customFont = this.getFont();
        if (customFont == null) {
            return Rectangle.ofXYWH(0.0f, 0.0f, 0.0f, 0.0f);
        }
        float f = customFont.getStringWidth(string);
        float f2 = customFont.getStringHeight(string);
        return Rectangle.ofXYWH(0.0f, 0.0f, f, f2);
    }

    public float getWidth(String string) {
        if (string == null || string.isEmpty()) {
            return 0.0f;
        }
        CustomFont customFont = this.getFont();
        return customFont == null ? 0.0f : customFont.getStringWidth(string);
    }

    public short[] getGlyphCodes(String string) {
        return new short[0];
    }

    public Path getGlyphPath(short s) {
        return null;
    }

    public GlyphMetrics getMetrics() {
        CustomFont customFont = this.getFont();
        if (customFont == null) {
            return new GlyphMetrics(-this.size * 0.8f, this.size * 0.2f, this.size * 1.2f, this.size * 0.7f);
        }
        FontMetricsImpl fontMetricsImpl = customFont.getFontMetrics();
        int n = customFont.getScale();
        if (n <= 0) {
            n = 1;
        }
        float f = -((float)fontMetricsImpl.getAscent()) / (float)n;
        float f2 = (float)fontMetricsImpl.getDescent() / (float)n;
        float f3 = (float)fontMetricsImpl.getLeading() / (float)n;
        float f4 = (float)fontMetricsImpl.getHeight() / (float)n;
        float f5 = (float)fontMetricsImpl.getAscent() * 0.7f / (float)n;
        GlyphMetrics glyphMetrics = new GlyphMetrics(f, f2, f4, f5);
        return glyphMetrics;
    }
}