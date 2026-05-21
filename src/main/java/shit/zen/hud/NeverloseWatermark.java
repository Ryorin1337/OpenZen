package shit.zen.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import shit.zen.ClientBase;
import shit.zen.event.impl.GlRenderEvent;
import shit.zen.event.impl.Render2DEvent;
import shit.zen.render.DrawContext;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.render.Paint;
import shit.zen.utils.render.ColorUtil;

public class NeverloseWatermark {
    private final FontRenderer boldFont = FontPresets.museoSans(18.0f);
    private final FontRenderer regularFont = FontPresets.pingfang(13.0f);
    private final FontRenderer smallFont = FontPresets.pingfang(13.0f);
    private final FontRenderer tinyFont = FontPresets.materialIcons(16.0f);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private final float logoWidth = 5.0f;
    private final Paint backgroundPaint = new Paint().setColor(ColorUtil.fromARGB(36, 36, 36, 120));
    private final Paint textPaint = new Paint().setColor(-1);
    private final Paint accentPaint = new Paint().setColor(ColorUtil.fromRGB(42, 180, 255));

    public void onRender2D(Render2DEvent render2DEvent) {
        if (ClientBase.mc.options.renderDebug) {
            return;
        }
        float f = ClientBase.mc.getWindow().getGuiScaledWidth();
        float f2 = f / 2.0f - this.getTotalWidth() / 2.0f;
        float f3 = 10.0f;
        float f4 = 6.0f;
        float f5 = 4.5f;
        float f6 = 15.0f;
        float f7 = this.measureText("ZEN", this.boldFont);
        f2 += f7 + f4;
        f2 = this.renderSectionLegacy(render2DEvent.poseStack(), f2, f3, this.getServerName(), this.smallFont, "", f5, f6, f4);
        f2 = this.renderSectionLegacy(render2DEvent.poseStack(), f2, f3, this.getPingText(), this.regularFont, "", f5, f6, f4);
        f2 = this.renderSectionLegacy(render2DEvent.poseStack(), f2, f3, this.getFpsText(), this.regularFont, "", f5, f6, f4);
        f2 = this.renderSectionLegacy(render2DEvent.poseStack(), f2, f3, this.getTimeText(), this.regularFont, "", f5, f6, f4);
        f2 = this.renderSectionLegacy(render2DEvent.poseStack(), f2, f3, this.getCpsText(), this.regularFont, "", f5, f6, f4);
        this.renderSectionLegacy(render2DEvent.poseStack(), f2, f3, this.getCoordText(), this.regularFont, "", f5, f6, f4);
    }

    private float renderSectionLegacy(PoseStack poseStack, float f, float f2, String string, FontRenderer fontRenderer, String string2, float f3, float f4, float f5) {
        float f6 = this.measureTextWithSub(string, fontRenderer, string2);
        return f + f6 + f5;
    }

    public void onGlRender(GlRenderEvent glRenderEvent) {
        if (ClientBase.mc.options.renderDebug) {
            return;
        }
        float f = ClientBase.mc.getWindow().getGuiScaledWidth();
        float f2 = f / 2.0f - this.getTotalWidth() / 2.0f;
        float f3 = 10.0f;
        float f4 = 6.0f;
        float f5 = 4.5f;
        f2 = this.renderSection(glRenderEvent.drawContext(), f2, f3, "ZEN", this.boldFont, f5, f4);
        f2 = this.renderSectionWithSub(glRenderEvent.drawContext(), f2, f3, this.getServerName(), this.smallFont, "", f5, f4);
        f2 = this.renderSectionWithSub(glRenderEvent.drawContext(), f2, f3, this.getPingText(), this.regularFont, "", f5, f4);
        f2 = this.renderSectionWithSub(glRenderEvent.drawContext(), f2, f3, this.getFpsText(), this.regularFont, "", f5, f4);
        f2 = this.renderSectionWithSub(glRenderEvent.drawContext(), f2, f3, this.getTimeText(), this.regularFont, "", f5, f4);
        f2 = this.renderSectionWithSub(glRenderEvent.drawContext(), f2, f3, this.getCpsText(), this.regularFont, "", f5, f4);
        this.renderSectionWithSub(glRenderEvent.drawContext(), f2, f3, this.getCoordText(), this.regularFont, "", f5, f4);
    }

    private float renderSection(DrawContext drawContext, float f, float f2, String string, FontRenderer fontRenderer, float f3, float f4) {
        float f5 = 8.0f;
        float f6 = (float)GlHelper.getFontAscent(this.boldFont) + 12.0f - 2.0f;
        float f7 = GlHelper.getStringWidth(string, fontRenderer);
        float f8 = f5 + f7 + f5 - 5.0f;
        GlHelper.drawRoundedRect(f, f2, f8, f6, f3, this.backgroundPaint);
        float f9 = f2 + (f6 - (float)GlHelper.getFontAscent(fontRenderer)) / 2.0f + 3.5f;
        GlHelper.drawTextShadowLegacy(string, f + f5 - 2.0f, f9, fontRenderer, this.textPaint.getColor());
        return f + f8 + f4;
    }

    private float renderSectionWithSub(DrawContext drawContext, float f, float f2, String string, FontRenderer fontRenderer, String string2, float f3, float f4) {
        float f5 = 8.0f;
        float f6 = (float)GlHelper.getFontAscent(fontRenderer) + 12.0f;
        float f7 = GlHelper.getStringWidth(string, fontRenderer);
        float f8 = GlHelper.getStringWidth(string2, this.tinyFont);
        float f9 = f5 + f8 + 5.0f + f7 + f5 - 4.0f;
        GlHelper.drawRoundedRect(f, f2, f9, f6, f3, this.backgroundPaint);
        float f10 = f2 + (f6 - (float)GlHelper.getFontAscent(this.tinyFont)) / 2.0f + 3.0f;
        GlHelper.drawTextShadowLegacy(string2, f + f5 - 1.0f, f10, this.tinyFont, this.accentPaint.getColor());
        float f11 = f2 + (f6 - (float)GlHelper.getFontAscent(fontRenderer)) / 2.0f + 1.0f;
        GlHelper.drawTextShadowLegacy(string, f + f5 + f8 + 5.0f - 3.0f, f11, fontRenderer, this.textPaint.getColor());
        return f + f9 + f4;
    }

    private float measureText(String string, FontRenderer fontRenderer) {
        float f = 8.0f;
        return f + GlHelper.getStringWidth(string, fontRenderer) + f - 5.0f;
    }

    private float getTotalWidth() {
        float f = 0.0f;
        float f2 = 6.0f;
        f += this.measureText("ZEN", this.boldFont) + f2;
        f += this.measureTextWithSub(this.getServerName(), this.smallFont, "") + f2;
        f += this.measureTextWithSub(this.getPingText(), this.regularFont, "") + f2;
        f += this.measureTextWithSub(this.getFpsText(), this.regularFont, "") + f2;
        f += this.measureTextWithSub(this.getTimeText(), this.regularFont, "") + f2;
        f += this.measureTextWithSub(this.getCpsText(), this.regularFont, "") + f2;
        return f += this.measureTextWithSub(this.getCoordText(), this.regularFont, "");
    }

    private float measureTextWithSub(String string, FontRenderer fontRenderer, String string2) {
        float f = 8.0f;
        return f + GlHelper.getStringWidth(string2, this.tinyFont) + 5.0f + GlHelper.getStringWidth(string, fontRenderer) + f - 4.0f;
    }

    private String getServerName() {
        return ClientBase.mc.player != null ? ClientBase.mc.player.getGameProfile().getName() : "Player";
    }

    private String getPingText() {
        return "Default Config";
    }

    private String getFpsText() {
        if (ClientBase.mc.player == null || ClientBase.mc.player.connection == null) {
            return "0ms";
        }
        PlayerInfo playerInfo = ClientBase.mc.player.connection.getPlayerInfo(ClientBase.mc.player.getUUID());
        if (playerInfo == null) {
            return "0ms";
        }
        return playerInfo.getLatency() + "ms";
    }

    private String getTimeText() {
        return ClientBase.mc.getFps() + "fps";
    }

    private String getCpsText() {
        ServerData serverData = ClientBase.mc.getCurrentServer();
        return serverData != null ? serverData.ip : "Singleplayer";
    }

    private String getCoordText() {
        return this.timeFormat.format(new Date());
    }
}