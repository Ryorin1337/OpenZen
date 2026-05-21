package shit.zen.gui.panel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import shit.zen.ClientBase;
import shit.zen.ZenClient;
import shit.zen.gui.panel.SettingsPopup;
import shit.zen.render.DrawContext;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.render.Renderer;
import shit.zen.render.TextGlow;
import shit.zen.utils.math.LerpUtil;
import shit.zen.utils.render.RenderUtil;

public class ProfileWidget
extends ClientBase {
    private float hoverAlpha = 0.0f;
    private boolean isHovered = false;
    private final SettingsPopup settingsPopup;

    public ProfileWidget(Consumer<Float> consumer) {
        this.settingsPopup = new SettingsPopup(consumer);
    }

    public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, float f, float f2) {
        if (mc.player == null) {
            return;
        }
        try {
            int n5 = (int)(20.0f * f);
            int n6 = (int)(20.0f * f);
            int n7 = (int)(10.0f * f);
            int n8 = (int)(20.0f * f);
            float f3 = 6.0f * f;
            int n9 = n + n6;
            int n10 = n2 + n8;
            String string = this.getUserId();
            String string2 = this.getUserRole();
            int n11 = n9 + n5 + n7 - (int)(11.0f * f);
            int n12 = n10 + n5 / 2 - (int)(10.0f * f);
            int n13 = n11 - n5 - (int)(5.0f * f);
            int n14 = n12 - (int)(8.0f * f);
            this.checkHover(n13, n14, n3, n4, n5);
            this.updateHoverAlpha();
            Renderer.renderConsumer(drawContext -> {
                if (this.hoverAlpha > 0.01f) {
                    int hoverColor = new Color(255, 255, 255, (int)(30.0f * this.hoverAlpha * f2)).getRGB();
                    RenderUtil.drawRoundedRect(guiGraphics.pose(), n13 - 2, n14 - 2, n5 + 4, n5 + 4, f3 + 1.0f, hoverColor);
                }
                if (mc.player instanceof AbstractClientPlayer) {
                    GlHelper.drawPlayerHeadRounded(mc.player, n13, n14, n5, n5, f2, f3);
                }
                FontRenderer fontRenderer = FontPresets.axiformaRegular(14.0f * f);
                int glowColor = new Color(255, 255, 255, (int)(100.0f * f2)).getRGB();
                TextGlow.drawGlowText(string, n11, n12, fontRenderer, this.applyAlpha(-1, f2), glowColor, 8.0f * f);
                float roleWidth = GlHelper.getStringWidth(string, fontRenderer);
                int roleBoxX = (int)((float)n11 + roleWidth + 8.0f * f);
                int roleBoxY = n12 - (int)(6.0f * f);
                Color color = this.getRoleColor(string2);
                FontRenderer fontRenderer2 = FontPresets.axiformaBold(11.0f * f);
                float roleStrWidth = GlHelper.getStringWidth(string2, fontRenderer2);
                int roleBoxW = (int)(roleStrWidth + 8.0f * f);
                int roleBoxH = (int)(10.0f * f);
                int roleShadowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(180.0f * f2)).getRGB();
                RenderUtil.drawRoundedRect(guiGraphics.pose(), roleBoxX - 3, (float)roleBoxY + 2.5f * f - 2.0f, roleBoxW + 2, roleBoxH + 2, 5.0f * f, this.applyAlpha(roleShadowColor, f2 * 0.35f));
                RenderUtil.drawRoundedRect(guiGraphics.pose(), roleBoxX - 2, (float)roleBoxY + 3.5f * f - 2.0f, roleBoxW, roleBoxH, 4.0f * f, this.applyAlpha(color.getRGB(), f2));
                int roleTextGlow = new Color(255, 255, 255, (int)(120.0f * f2)).getRGB();
                TextGlow.drawGlowText(string2, (float)roleBoxX + 1.5f * f, (float)roleBoxY + 6.5f * f, fontRenderer2, this.applyAlpha(-1, f2), roleTextGlow, 5.0f * f);
            });
            this.settingsPopup.render(guiGraphics, n3, n4, f, f2);
        } catch (Exception exception) {
            // empty catch block
        }
    }

    private int applyAlpha(int n, float f) {
        int n2 = n >> 24 & 0xFF;
        int n3 = (int)((float)n2 * f);
        return n3 << 24 | n & 0xFFFFFF;
    }

    private String getUserId() {
        return ZenClient.username != null && !ZenClient.username.isEmpty() ? ZenClient.username : "Unknown";
    }

    private String getUserRole() {
        try {
            if (false) {
                List<String> list = new ArrayList<>();
                if (list.contains("ROLE_OWNER")) return "Premium";
                if (list.contains("ROLE_ADMIN")) return "Admin";
                if (list.contains("ROLE_BETA")) return "Beta";
                return list.get(0).replace("ROLE_", "");
            }
        } catch (Exception exception) {
            // empty catch block
        }
        return "User";
    }

    public boolean isMouseOverAvatar(int n, int n2, int n3, int n4, float f) {
        int n5 = (int)(20.0f * f);
        int n6 = (int)(20.0f * f);
        int n7 = (int)(10.0f * f);
        int n8 = (int)(20.0f * f);
        int n9 = n + n6;
        int n10 = n2 + n8;
        int n11 = n9 + n5 + n7 - (int)(11.0f * f);
        int n12 = n10 + n5 / 2 - (int)(10.0f * f);
        int n13 = n11 - n5 - (int)(5.0f * f);
        int n14 = n12 - (int)(8.0f * f);
        return n3 >= n13 && n3 <= n13 + n5 && n4 >= n14 && n4 <= n14 + n5;
    }

    public boolean onMouseClick(int n, int n2, int n3, int n4, float f) {
        if (this.settingsPopup.isOpen() && this.settingsPopup.onMouseClick(n3, n4, f)) {
            return true;
        }
        if (this.isMouseOverAvatar(n, n2, n3, n4, f)) {
            this.settingsPopup.toggleOpen();
            return true;
        }
        return false;
    }

    private Color getRoleColor(String role) {
        switch (role.toLowerCase()) {
            case "owner":
                return new Color(220, 53, 69);
            case "admin":
                return new Color(255, 193, 7);
            case "beta":
                return new Color(108, 117, 225);
            case "vip":
                return new Color(40, 167, 69);
            case "premium":
                return new Color(102, 16, 242);
            default:
                return new Color(108, 117, 125);
        }
    }

    private void checkHover(int n, int n2, int n3, int n4, int n5) {
        this.isHovered = n3 >= n && n3 <= n + n5 && n4 >= n2 && n4 <= n2 + n5;
    }

    private void updateHoverAlpha() {
        this.hoverAlpha = this.isHovered ? LerpUtil.lerp(this.hoverAlpha, 1.0f, 0.12f) : LerpUtil.lerp(this.hoverAlpha, 0.0f, 0.12f);
    }

    public void onMouseDrag(int n, int n2) {
        this.settingsPopup.onMouseDrag(n, n2);
    }

    public void onMouseRelease() {
        this.settingsPopup.stopDrag();
    }

    public boolean isPopupOpen() {
        return this.settingsPopup.isOpen();
    }
}