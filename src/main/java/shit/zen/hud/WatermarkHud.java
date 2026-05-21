package shit.zen.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.Mth;
import shit.zen.ClientBase;
import shit.zen.modules.impl.movement.Scaffold;
import shit.zen.render.DrawContext;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.Paint;

public class WatermarkHud
extends ClientBase
implements IHudElement {
    private static final FontRenderer logoFont = FontPresets.zenIcon(36.0f);
    private static final FontRenderer subFont = FontPresets.poppinsMedium(12.0f);
    private static final int primaryColor = new Color(170, 170, 170).getRGB();
    private static final int shadowColor = new Color(0, 0, 0, 100).getRGB();
    private static final float logoCharWidth = logoFont.getWidth("Z");
    private static final float separatorCharWidth = subFont.getWidth("|");
    private static final float betaRawWidth = subFont.getWidth("beta");
    private static final float b1RawWidth = subFont.getWidth("b1");
    private static final float sep1Width = Math.max(betaRawWidth, b1RawWidth);
    private static final float betaWidth = logoCharWidth + separatorCharWidth * 2.0f + sep1Width + 48.0f;
    private static final float b1Width = logoFont.getMetrics().capHeight();
    private static final float subLineHeight = subFont.getMetrics().capHeight();
    private int lastTick = -1;
    private float maxSubWidth;
    private float line1Width;
    private float line2Width;
    private String line1Text;
    private String line2Text;

    private void updateCache() {
        if (mc == null || mc.player == null || this.lastTick == mc.player.tickCount) {
            return;
        }
        this.lastTick = mc.player.tickCount;
        String[] stringArray = this.getServerInfo();
        this.line1Text = stringArray[0];
        this.line2Text = stringArray[1];
        this.line1Width = subFont.getWidth(this.line1Text);
        this.line2Width = subFont.getWidth(this.line2Text);
        this.maxSubWidth = Math.max(this.line1Width, this.line2Width);
    }

    @Override
    public boolean hasBackground() {
        return true;
    }

    @Override
    public void renderGui(GuiGraphics guiGraphics, PoseStack poseStack, float f, float f2, float f3, float f4, float f5) {
    }

    @Override
    public void render(DrawContext drawContext, float f, float f2, float f3, float f4, float f5) {
        if (mc == null || mc.player == null || f5 <= 0.01f) {
            return;
        }
        this.updateCache();
        float f6 = betaWidth + this.maxSubWidth;
        float f7 = f + (f3 - f6) / 2.0f - 1.0f;
        float f8 = f2 + f4 / 2.0f + 1.0f;
        int n = this.colorWithAlpha(Color.WHITE.getRGB(), f5);
        int n2 = this.colorWithAlpha(primaryColor, f5);
        int n3 = this.colorWithAlpha(shadowColor, f5);
        try (Paint paint = new Paint()){
            this.drawText(drawContext, paint, "Z", f7, f8 + 4.0f, logoFont, b1Width, n, n3, true);
            f7 += logoCharWidth + 12.0f;
            this.drawText(drawContext, paint, "|", (f7 += 12.0f) - 13.0f, f8, subFont, subLineHeight, n2, n3, true);
            float f9 = (f7 += separatorCharWidth + 12.0f) + (sep1Width - betaRawWidth) / 2.0f - 13.0f;
            float f10 = f7 + (sep1Width - b1RawWidth) / 2.0f - 13.0f;
            this.drawText(drawContext, paint, "beta", f9, f8 - 2.0f, subFont, 0.0f, n, n3, false);
            this.drawText(drawContext, paint, "b1", f10, f8 + 7.0f, subFont, 0.0f, n2, n3, false);
            f7 += sep1Width;
            this.drawText(drawContext, paint, "|", (f7 += 12.0f) - 13.0f, f8, subFont, subLineHeight, n2, n3, true);
            float f11 = (f7 += separatorCharWidth + 12.0f) + (this.maxSubWidth - this.line1Width) / 2.0f - 13.0f;
            float f12 = f7 + (this.maxSubWidth - this.line2Width) / 2.0f - 13.0f;
            this.drawText(drawContext, paint, this.line1Text, f11, f8 - 2.0f, subFont, 0.0f, n, n3, false);
            this.drawText(drawContext, paint, this.line2Text, f12, f8 + 7.0f, subFont, 0.0f, n2, n3, false);
        }
    }

    private void drawText(DrawContext drawContext, Paint paint, String string, float f, float f2, FontRenderer fontRenderer, float f3, int n, int n2, boolean bl) {
        float f4 = f2;
        if (bl) {
            f4 = f2 + f3 / 2.0f;
        }
        paint.setColor(n2);
        drawContext.drawString(string, f + 0.5f, f4 + 0.5f, fontRenderer, paint);
        paint.setColor(n);
        drawContext.drawString(string, f, f4, fontRenderer, paint);
    }

    private float getX() {
        this.updateCache();
        return betaWidth + this.maxSubWidth;
    }

    @Override
    public IHudElement.Size getHudAlignment() {
        return new IHudElement.Size(this.getX(), 25.0f);
    }

    private String[] getServerInfo() {
        PlayerInfo playerInfo;
        if (mc.isSingleplayer()) {
            return new String[]{"Singleplayer", "1ms"};
        }
        ServerData serverData = mc.getCurrentServer();
        String string = serverData != null ? serverData.ip : "Multiplayer";
        int n = 0;
        if (mc.getConnection() != null && mc.player != null && (playerInfo = mc.getConnection().getPlayerInfo(mc.player.getUUID())) != null) {
            n = playerInfo.getLatency();
        }
        n = Mth.clamp(n, 0, 9999);
        return new String[]{string, n + "ms"};
    }

    @Override
    public boolean isVisible() {
        return Scaffold.INSTANCE == null || !Scaffold.INSTANCE.isEnabled();
    }
}