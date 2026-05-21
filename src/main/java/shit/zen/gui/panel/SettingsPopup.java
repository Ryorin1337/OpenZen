package shit.zen.gui.panel;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.client.gui.GuiGraphics;
import shit.zen.ClientBase;
import shit.zen.ZenClient;
import shit.zen.render.DrawContext;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.render.Rectangle;
import shit.zen.render.Renderer;
import shit.zen.render.TextGlow;
import shit.zen.utils.math.LerpUtil;
import shit.zen.utils.render.RenderUtil;

public class SettingsPopup
extends ClientBase {
    private boolean isOpen = false;
    private boolean isDragging = false;
    private int lastDragX = 0;
    private int lastDragY = 0;
    private int offsetX = 0;
    private int offsetY = 0;
    private float openAlpha = 0.0f;
    private float closeButtonHoverAlpha = 0.0f;
    private boolean isCloseButtonHovered = false;
    private final Map<String, Boolean> dropdownOpen = new HashMap<>();
    private final Map<String, Float> dropdownAlpha = new HashMap<>();
    private final Map<String, Map<String, Float>> dropdownItemHover = new HashMap<>();
    private static final String[] LANGUAGES = new String[]{"English", "Chinese"};
    private String selectedLanguage = "English";
    private static final String[] SCALES = new String[]{"50%", "75%", "100%", "125%", "150%"};
    private String selectedScale = "100%";
    private static final Color POPUP_BG_COLOR = new Color(20, 20, 24, 230);
    private final Consumer<Float> scaleChangeCallback;

    public SettingsPopup(Consumer<Float> consumer) {
        this.scaleChangeCallback = consumer;
        this.dropdownOpen.put("language", false);
        this.dropdownOpen.put("scale", false);
        this.dropdownAlpha.put("language", Float.valueOf(0.0f));
        this.dropdownAlpha.put("scale", Float.valueOf(0.0f));
        this.dropdownItemHover.put("language", new HashMap<>());
        this.dropdownItemHover.put("scale", new HashMap<>());
    }

    public void render(GuiGraphics guiGraphics, int n, int n2, float f, float f2) {
        this.updatePopupPosition(n, n2, f);
        this.updateOpenAlpha();
        this.updateCloseButtonHover();
        this.updateDropdownAlpha();
        if (this.openAlpha > 0.01f) {
            this.clampPopupPosition(f);
            this.renderPopupContent(guiGraphics, n, n2, f, f2);
        }
    }

    private void renderPopupContent(GuiGraphics guiGraphics, int n, int n2, float f, float f2) {
        int n3 = (int)(220.0f * f);
        int n4 = this.calculatePopupHeight(f);
        int n5 = mc.getWindow().getGuiScaledWidth();
        int n6 = mc.getWindow().getGuiScaledHeight();
        int n7 = (n5 - n3) / 2 + this.offsetX;
        int n8 = (n6 - (int)(200.0f * f)) / 2 + this.offsetY;
        float f3 = this.openAlpha * f2;
        int n9 = (int)(255.0f * f3);
        TextGlow.drawBackground(guiGraphics.pose(), n7, n8, n3, n4, 12.0f * f, f3);
        Renderer.renderConsumer((drawContext -> this.drawPopupBody(drawContext, guiGraphics, n7, n8, n, n2, n4, n9, f, n3)));
    }

    private void drawPopupBody(DrawContext drawContext, GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, float f, int n7) {
        int n8 = n6 << 24 | 0xFFFFFF;
        FontRenderer fontRenderer = FontPresets.materialIcons(18.0f * f);
        GlHelper.drawText("", (float)n + 15.0f * f, (float)n2 + 16.0f * f, fontRenderer, n8);
        FontRenderer fontRenderer2 = FontPresets.museoSans(22.0f * f);
        String string = "ZENLESS.ZONE";
        float f2 = GlHelper.getStringWidth(string, fontRenderer2);
        GlHelper.drawText(string, (float)n + ((float)n7 - f2) / 2.0f, (float)n2 + 35.0f * f, fontRenderer2, n8);
        this.drawCloseButton(n, n2, fontRenderer, n6, f, n7);
        FontRenderer fontRenderer3 = FontPresets.axiformaRegular(13.0f * f);
        FontRenderer fontRenderer4 = FontPresets.axiformaRegular(13.0f * f);
        int n9 = n6 << 24 | 0xAAAAAA;
        int n10 = n6 << 24 | 0xFFFFFF;
        int n11 = (int)(18.0f * f);
        int n12 = (int)((float)n2 + 65.0f * f);
        int n13 = (int)((float)(n + n7) - 15.0f * f);
        GlHelper.drawText("Username:", (float)n + 15.0f * f, n12, fontRenderer3, n9);
        String string2 = this.getUserId();
        float f3 = GlHelper.getStringWidth(string2, fontRenderer4);
        GlHelper.drawText(string2, (float)n13 - f3, n12, fontRenderer4, n10);
        GlHelper.drawText("Branch:", (float)n + 15.0f * f, n12 += n11, fontRenderer3, n9);
        String string3 = this.getUserRole();
        float f4 = GlHelper.getStringWidth(string3, fontRenderer4);
        GlHelper.drawText(string3, (float)n13 - f4, n12, fontRenderer4, n10);
        GlHelper.drawText("Updated:", (float)n + 15.0f * f, n12 += n11, fontRenderer3, n9);
        String string4 = "Aug 4 2025";
        float f5 = GlHelper.getStringWidth(string4, fontRenderer4);
        GlHelper.drawText(string4, (float)n13 - f5, n12, fontRenderer4, n10);
        n12 += n11;
        n12 = (int)((float)n12 + 8.0f * f);
        n12 += this.drawDropdown(drawContext, guiGraphics, "Language", this.selectedLanguage, LANGUAGES, "language", n, n12, n3, n4, this.openAlpha, f, n7);
        n12 = (int)((float)n12 + 8.0f * f);
        this.drawDropdown(drawContext, guiGraphics, "Menu Scale", this.selectedScale, SCALES, "scale", n, n12, n3, n4, this.openAlpha, f, n7);
        FontRenderer fontRenderer5 = FontPresets.axiformaRegular(12.0f * f);
        String string5 = "7unknown © 2024-2025";
        float f6 = GlHelper.getStringWidth(string5, fontRenderer5);
        GlHelper.drawText(string5, (float)n + ((float)n7 - f6) / 2.0f, (float)(n2 + n5) - 15.0f * f, fontRenderer5, n9);
    }

    private void drawCloseButton(int n, int n2, FontRenderer fontRenderer, int n3, float f, int n4) {
        float f2 = (float)(n + n4) - 25.0f * f;
        float f3 = (float)n2 + 16.0f * f;
        Color color = new Color(255, 255, 255);
        Color color2 = new Color(255, 255, 255);
        int n5 = (int)((float)color.getRed() + (float)(color2.getRed() - color.getRed()) * this.closeButtonHoverAlpha);
        int n6 = (int)((float)color.getGreen() + (float)(color2.getGreen() - color.getGreen()) * this.closeButtonHoverAlpha);
        int n7 = (int)((float)color.getBlue() + (float)(color2.getBlue() - color.getBlue()) * this.closeButtonHoverAlpha);
        int n8 = n3 << 24 | n5 << 16 | n6 << 8 | n7;
        int n9 = (int)(180.0f * this.closeButtonHoverAlpha * this.openAlpha);
        int n10 = new Color(n5, n6, n7, n9).getRGB();
        TextGlow.drawGlowText("", f2, f3, fontRenderer, n8, n10, 10.0f * f);
    }

    private int drawDropdown(DrawContext drawContext, GuiGraphics guiGraphics, String string, String string2, String[] stringArray, String string3, int n, int n2, int n3, int n4, float f, float f2, int n5) {
        FontRenderer fontRenderer = FontPresets.axiformaRegular(13.0f * f2);
        FontRenderer fontRenderer2 = FontPresets.axiformaRegular(13.0f * f2);
        int n6 = this.applyAlpha(new Color(0xAAAAAA).getRGB(), f);
        int n7 = this.applyAlpha(new Color(0xFFFFFF).getRGB(), f);
        GlHelper.drawText(string, (float)n + 15.0f * f2, (float)n2 + 6.0f * f2, fontRenderer, n6);
        int n8 = (int)(90.0f * f2);
        int n9 = (int)((float)(n + n5 - n8) - 15.0f * f2);
        int n10 = (int)(20.0f * f2);
        int n11 = (int)(18.0f * f2);
        float f3 = this.dropdownAlpha.getOrDefault(string3, Float.valueOf(0.0f)).floatValue();
        String[] stringArray2 = this.filterDropdownItems(stringArray, string2);
        int n12 = (int)((float)(stringArray2.length * n11) * f3);
        RenderUtil.drawRoundedRect(guiGraphics.pose(), n9, n2, n8, n10 + n12, 4.0f * f2, this.applyAlpha(POPUP_BG_COLOR.getRGB(), f));
        float f4 = (float)n9 + 8.0f * f2;
        float f5 = (float)n2 + (float)n10 / 2.0f - fontRenderer2.getMetrics().capHeight() / 2.0f + 3.0f * f2;
        GlHelper.drawText(string2, f4, f5 - 2.0f, fontRenderer2, n7);
        FontRenderer fontRenderer3 = FontPresets.materialIcons(18.0f * f2);
        String string4 = "";
        float f6 = (float)(n9 + n8) - 18.0f * f2;
        float f7 = (float)n2 + (float)n10 / 2.0f + fontRenderer3.getMetrics().capHeight() / 2.0f - 10.5f * f2 + 7.0f;
        GlHelper.drawText(string4, f6, f7, fontRenderer3, n7);
        if (f3 > 0.01f) {
            drawContext.save();
            drawContext.clip(Rectangle.ofXYWH(n9, n2 + n10, n8, n12));
            Map<String, Float> map = this.dropdownItemHover.get(string3);
            int n13 = n2 + n10;
            for (String string5 : stringArray2) {
                boolean bl = this.isPointInBounds(n3, n4, n9, n13, n8, n11);
                this.updateItemHover(map, string5, bl);
                float f8 = map.getOrDefault(string5, 0.0f);
                float f9 = (float)n9 + 8.0f * f2;
                float f10 = (float)n13 + (float)n11 / 2.0f - fontRenderer2.getMetrics().capHeight() / 2.0f + 3.0f * f2;
                int n14 = this.applyAlpha(n7, f3);
                float f11 = f8 * f3;
                if (f11 > 0.01f) {
                    int n15 = new Color(1.0f, 1.0f, 1.0f, f11).getRGB();
                    TextGlow.drawGlowText(string5, f9, f10, fontRenderer2, n14, n15, 8.0f * f2);
                } else {
                    GlHelper.drawText(string5, f9, f10, fontRenderer2, n14);
                }
                n13 += n11;
            }
            drawContext.restore();
        }
        return n10 + n12;
    }

    private String getUserId() {
        return ZenClient.username != null && !ZenClient.username.isEmpty() ? ZenClient.username : "Unknown";
    }

    private String getUserRole() {
        return "User";
    }

    public boolean onMouseClick(int n, int n2, float f) {
        int n3;
        int n4;
        int n5 = (int)(220.0f * f);
        int n6 = this.calculatePopupHeight(f);
        int n7 = mc.getWindow().getGuiScaledWidth();
        int n8 = (n7 - n5) / 2 + this.offsetX;
        if (this.isMouseOverCloseButton(n, n2, n8, n4 = ((n3 = mc.getWindow().getGuiScaledHeight()) - (int)(200.0f * f)) / 2 + this.offsetY, f, n5)) {
            this.toggleOpen();
            return true;
        }
        if (this.isDragging) {
            return true;
        }
        if (this.isMouseInRect(n, n2, n8, n4, f, n5)) {
            this.beginDrag(n, n2);
            return true;
        }
        int n9 = (int)(90.0f * f);
        int n10 = (int)((float)(n8 + n5 - n9) - 15.0f * f);
        int n11 = (int)((float)n4 + 127.0f * f);
        boolean bl = this.handleDropdownClick(n, n2, n10, n11, n9, LANGUAGES, this.selectedLanguage, "language", value -> {
            this.selectedLanguage = value;
        }, f);
        float f2 = (float)this.filterDropdownItems(LANGUAGES, this.selectedLanguage).length * (18.0f * f) * this.dropdownAlpha.getOrDefault("language", 0.0f);
        int n12 = (int)((float)n11 + 20.0f * f + f2 + 8.0f * f);
        boolean bl2 = this.handleDropdownClick(n, n2, n10, n12, n9, SCALES, this.selectedScale, "scale", value -> {
            this.selectedScale = value;
            try {
                float parsed = Float.parseFloat(value.replace("%", "")) / 100.0f;
                this.scaleChangeCallback.accept(parsed);
            } catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }, f);
        boolean bl3 = this.isPointInBounds(n, n2, n8, n4, n5, n6);
        if (bl || bl2) {
            return true;
        }
        if (bl3) {
            this.dropdownOpen.put("language", false);
            this.dropdownOpen.put("scale", false);
            return true;
        }
        return false;
    }

    private boolean handleDropdownClick(int n, int n2, int n3, int n4, int n5, String[] stringArray, String string3, String string4, Consumer<String> consumer, float f) {
        boolean bl = this.dropdownOpen.getOrDefault(string4, false);
        int n6 = (int)(18.0f * f);
        int n7 = (int)(20.0f * f);
        if (this.isPointInBounds(n, n2, n3, n4, n5, n7)) {
            this.dropdownOpen.put(string4, !bl);
            this.dropdownOpen.keySet().stream().filter(string2 -> !string2.equals(string4)).forEach(string -> this.dropdownOpen.put(string, false));
            return true;
        }
        if (bl) {
            String[] stringArray2 = this.filterDropdownItems(stringArray, string3);
            for (int i = 0; i < stringArray2.length; ++i) {
                if (!this.isPointInBounds(n, n2, n3, n4 + n7 + i * n6, n5, n6)) continue;
                consumer.accept(stringArray2[i]);
                this.dropdownOpen.put(string4, false);
                return true;
            }
        }
        return false;
    }

    private boolean isMouseInRect(int n, int n2, int n3, int n4, float f, int n5) {
        float f2 = (float)(n3 + n5) - 25.0f * f;
        boolean bl = (float)n >= f2 - 10.0f * f && (float)n <= f2 + 15.0f * f;
        return n >= n3 && n <= n3 + n5 && n2 >= n4 && (float)n2 <= (float)n4 + 30.0f * f && !bl;
    }

    private boolean isMouseOverCloseButton(int n, int n2, int n3, int n4, float f, int n5) {
        float f2 = (float)(n3 + n5) - 25.0f * f;
        float f3 = (float)n4 + 16.0f * f;
        return (float)n >= f2 - 10.0f * f && (float)n <= f2 + 15.0f * f && (float)n2 >= f3 - 10.0f * f && (float)n2 <= f3 + 10.0f * f;
    }

    private void beginDrag(int n, int n2) {
        this.isDragging = true;
        this.lastDragX = n;
        this.lastDragY = n2;
    }

    public void onMouseDrag(int n, int n2) {
        if (this.isDragging) {
            this.offsetX += n - this.lastDragX;
            this.offsetY += n2 - this.lastDragY;
            this.lastDragX = n;
            this.lastDragY = n2;
        }
    }

    public void stopDrag() {
        this.isDragging = false;
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public void toggleOpen() {
        this.isOpen = !this.isOpen;
    }

    private void updateOpenAlpha() {
        if (this.isOpen) {
            this.openAlpha = LerpUtil.lerp(this.openAlpha, 1.0f, 0.1f);
        } else {
            this.openAlpha = LerpUtil.lerp(this.openAlpha, 0.0f, 0.1f);
            if (this.openAlpha < 0.01f) {
                this.dropdownOpen.put("language", false);
                this.dropdownOpen.put("scale", false);
            }
        }
    }

    private void updateDropdownAlpha() {
        for (String string : this.dropdownOpen.keySet()) {
            boolean bl = this.dropdownOpen.getOrDefault(string, false);
            float f = this.dropdownAlpha.getOrDefault(string, Float.valueOf(0.0f)).floatValue();
            float f2 = bl ? 1.0f : 0.0f;
            f = Math.abs(f - f2) > 0.01f ? LerpUtil.smoothLerp(f, f2, 0.22f) : f2;
            this.dropdownAlpha.put(string, Float.valueOf(f));
        }
    }

    private void updatePopupPosition(int n, int n2, float f) {
        if (this.isOpen) {
            int n3 = (int)(220.0f * f);
            int n4 = mc.getWindow().getGuiScaledWidth();
            int n5 = mc.getWindow().getGuiScaledHeight();
            int n6 = (n4 - n3) / 2 + this.offsetX;
            int n7 = (n5 - (int)(200.0f * f)) / 2 + this.offsetY;
            this.isCloseButtonHovered = this.isMouseOverCloseButton(n, n2, n6, n7, f, n3);
        } else {
            this.isCloseButtonHovered = false;
        }
    }

    private void updateCloseButtonHover() {
        this.closeButtonHoverAlpha = this.isCloseButtonHovered ? LerpUtil.lerp(this.closeButtonHoverAlpha, 1.0f, 0.16f) : LerpUtil.lerp(this.closeButtonHoverAlpha, 0.0f, 0.16f);
    }

    private void updateItemHover(Map<String, Float> map, String string, boolean bl) {
        float f = map.getOrDefault(string, Float.valueOf(0.0f)).floatValue();
        float f2 = bl ? 1.0f : 0.0f;
        f = Math.abs(f - f2) > 0.01f ? LerpUtil.smoothLerp(f, f2, 0.28f) : f2;
        map.put(string, Float.valueOf(f));
    }

    private String[] filterDropdownItems(String[] stringArray, String string) {
        return Stream.of((Object[])stringArray).filter(string2 -> !Objects.equals(string2, string)).toArray(String[]::new);
    }

    private boolean isPointInBounds(int n, int n2, int n3, int n4, int n5, int n6) {
        return n >= n3 && n <= n3 + n5 && n2 >= n4 && n2 <= n4 + n6;
    }

    private int applyAlpha(int n, float f) {
        int n2 = n >> 24 & 0xFF;
        int n3 = (int)((float)n2 * f);
        return n3 << 24 | n & 0xFFFFFF;
    }

    private int calculatePopupHeight(float f) {
        float f2 = 200.0f * f;
        float f3 = 18.0f * f;
        float f4 = (float)this.filterDropdownItems(LANGUAGES, this.selectedLanguage).length * f3 * this.dropdownAlpha.getOrDefault("language", Float.valueOf(0.0f)).floatValue();
        float f5 = (float)this.filterDropdownItems(SCALES, this.selectedScale).length * f3 * this.dropdownAlpha.getOrDefault("scale", Float.valueOf(0.0f)).floatValue();
        return (int)(f2 + f4 + f5);
    }

    private void clampPopupPosition(float f) {
        int n = mc.getWindow().getGuiScaledWidth();
        int n2 = mc.getWindow().getGuiScaledHeight();
        int n3 = this.calculatePopupHeight(f);
        int n4 = (int)(220.0f * f);
        int n5 = (n - n4) / 2;
        int n6 = -(n - n4) / 2;
        int n7 = (n2 - n3) / 2;
        int n8 = -(n2 - (int)(200.0f * f)) / 2;
        this.offsetX = Math.max(n6, Math.min(this.offsetX, n5));
        this.offsetY = Math.max(n8, Math.min(this.offsetY, n7));
    }
}