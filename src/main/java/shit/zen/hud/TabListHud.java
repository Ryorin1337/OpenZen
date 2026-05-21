package shit.zen.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import shit.zen.ClientBase;
import shit.zen.render.DrawContext;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.render.Paint;
import shit.zen.utils.render.ColorUtil;

public class TabListHud
extends ClientBase
implements IHudElement {
    private static final FontRenderer nameFont = FontPresets.pingfang(12.0f);
    private static final FontRenderer headerFont = FontPresets.poppinsBold(20.0f);
    private static final FontRenderer titleFont = FontPresets.pingfang(16.0f);

    @Override
    public boolean hasBackground() {
        return true;
    }

    @Override
    public void renderGui(GuiGraphics guiGraphics, PoseStack poseStack, float f, float f2, float f3, float f4, float f5) {
        float f6;
        float f7;
        if (mc == null || mc.player == null || mc.getConnection() == null || f5 <= 0.01f) {
            return;
        }
        ArrayList<PlayerInfo> arrayList = new ArrayList<>(mc.getConnection().getOnlinePlayers());
        arrayList.sort(Comparator.comparing(p -> p.getProfile().getName()));
        Component component = TabListInfo.header;
        float f8 = component != null && !component.getString().isEmpty() ? (float)component.getString().split("\n").length * 11.0f : 20.0f;
        int n = 20;
        int n2 = Math.max(1, (int)Math.ceil((double)arrayList.size() / (double)n));
        float f9 = 0.0f;
        if (n2 == 1) {
            float f10;
            f7 = 0.0f;
            if (!arrayList.isEmpty()) {
                for (PlayerInfo playerInfo2 : arrayList) {
                    String string = playerInfo2.getProfile().getName();
                    String string2 = playerInfo2.getLatency() + "ms";
                    f6 = 12.0f + nameFont.getWidth(string) + 5.0f + nameFont.getWidth(string2);
                    f7 = Math.max(f7, f6);
                }
            }
            if (f7 < (f10 = f3 - 20.0f)) {
                f9 = (f10 - f7) / 2.0f;
            }
        }
        f7 = f2 + 10.0f + f8 + 10.0f;
        for (int i = 0; i < arrayList.size(); ++i) {
            int n3 = i / n;
            int n4 = i % n;
            float f11 = f + 10.0f + (float)n3 * 150.0f;
            if (n2 == 1) {
                f11 += f9;
            }
            f6 = f7 + (float)n4 * 11.0f;
            PlayerInfo playerInfo3 = arrayList.get(i);
            poseStack.pushPose();
            poseStack.translate(f11, f6, 0.0f);
            guiGraphics.blit(playerInfo3.getSkinLocation(), 0, 0, 8, 8, 8.0f, 8.0f, 8, 8, 64, 64);
            guiGraphics.blit(playerInfo3.getSkinLocation(), 0, 0, 8, 8, 40.0f, 8.0f, 8, 8, 64, 64);
            poseStack.popPose();
        }
    }

    @Override
    public void render(DrawContext drawContext, float f, float f2, float f3, float f4, float f5) {
        if (mc == null || mc.player == null || mc.getConnection() == null || f5 <= 0.01f) {
            return;
        }
        ArrayList<PlayerInfo> arrayList = new ArrayList<>(mc.getConnection().getOnlinePlayers());
        arrayList.sort(Comparator.comparing(p -> p.getProfile().getName()));
        Component component = TabListInfo.header;
        Component component2 = TabListInfo.footer;
        try (Paint paint = new Paint()){
            int n;
            float f6;
            float f7;
            float f8;
            paint.setColor(this.colorWithAlpha(Color.WHITE.getRGB(), f5));
            if (component != null && !component.getString().isEmpty()) {
                f8 = (float)component.getString().split("\n").length * 11.0f;
                float f9 = f2 + 10.0f;
                for (String string : component.getString().split("\n")) {
                    f7 = f + f3 / 2.0f;
                    this.drawFormattedComponent(drawContext, Component.literal(string), f7, f9 + titleFont.getMetrics().ascent() / 2.0f + 9.0f, titleFont, paint, f5);
                    f9 += 11.0f;
                }
            } else {
                f8 = 20.0f;
                String string = "Player List (" + arrayList.size() + ")";
                float f10 = f + (f3 - headerFont.getWidth(string)) / 2.0f;
                GlHelper.drawTextShadowLegacy(string, f10, f2 + 10.0f + headerFont.getMetrics().ascent() + 15.0f, headerFont, -1);
            }
            if (arrayList.isEmpty() && (component2 == null || component2.getString().isEmpty())) {
                return;
            }
            int n2 = arrayList.size();
            int n3 = 20;
            int n4 = Math.max(1, (int)Math.ceil((double)n2 / (double)n3));
            float f11 = f2 + 10.0f + f8 + 10.0f;
            float f12 = 0.0f;
            f7 = 0.0f;
            if (n4 == 1) {
                float f13;
                if (!arrayList.isEmpty()) {
                    for (PlayerInfo playerInfo2 : arrayList) {
                        String string = playerInfo2.getProfile().getName();
                        String string2 = playerInfo2.getLatency() + "ms";
                        f6 = 12.0f + nameFont.getWidth(string) + 5.0f + nameFont.getWidth(string2);
                        f7 = Math.max(f7, f6);
                    }
                }
                if (f7 < (f13 = f3 - 20.0f)) {
                    f12 = (f13 - f7) / 2.0f;
                }
            }
            for (n = 0; n < arrayList.size(); ++n) {
                int n5 = n / n3;
                int n6 = n % n3;
                float f14 = f + 10.0f + (float)n5 * 150.0f;
                if (n4 == 1) {
                    f14 += f12;
                }
                f6 = f11 + (float)n6 * 11.0f;
                PlayerInfo object = arrayList.get(n);
                paint.setColor(this.colorWithAlpha(Color.WHITE.getRGB(), f5));
                String f19 = object.getProfile().getName();
                String string = object.getLatency() + "ms";
                float f9 = nameFont.getWidth(string);
                float f10 = n4 > 1 ? 128.0f - f9 - 5.0f : f7 - 8.0f - 4.0f - f9 - 5.0f;
                if (nameFont.getWidth(f19) > f10 && f10 > 0.0f) {
                    while (nameFont.getWidth(f19) > f10 && !f19.isEmpty()) {
                        f19 = f19.substring(0, f19.length() - 1);
                    }
                }
                drawContext.drawString(f19, f14 + 8.0f + 4.0f + 0.5f, f6 + nameFont.getMetrics().ascent() / 2.0f + 11.0f + 0.5f, nameFont, paint.setColor(ColorUtil.fromARGB(0, 0, 0, (int)(f5 * 0.65f * 255.0f))));
                drawContext.drawString(f19, f14 + 8.0f + 4.0f, f6 + nameFont.getMetrics().ascent() / 2.0f + 11.0f, nameFont, paint.setColor(-1));
                paint.setColor(this.colorWithAlpha(Color.GRAY.getRGB(), f5));
                float f13 = n4 > 1 ? f14 + 140.0f - f9 : f14 + f7 - f9;
                drawContext.drawString(string, f13, f6 + nameFont.getMetrics().ascent() / 2.0f + 11.0f, nameFont, paint);
            }
            if (component2 != null && !component2.getString().isEmpty()) {
                n = arrayList.isEmpty() ? 0 : Math.min(arrayList.size(), n3);
                float f18 = f11 + (float)n * 11.0f + 10.0f;
                for (String string : component2.getString().split("\n")) {
                    float f14 = f + f3 / 2.0f;
                    this.drawFormattedComponent(drawContext, Component.literal(string), f14, f18, titleFont, paint, f5);
                    f18 += 11.0f;
                }
            }
        }
    }

    private void drawFormattedComponent(DrawContext drawContext, Component component, float f, float f2, FontRenderer fontRenderer, Paint paint, float f3) {
        float f4 = f2;
        for (String string : component.getString().split("\n")) {
            float f5 = f;
            ArrayList<Component> arrayList = new ArrayList<>();
            String[] stringArray = ("§r" + string).split("§");
            Style style = Style.EMPTY;
            for (int i = 0; i < stringArray.length; ++i) {
                if (stringArray[i].isEmpty()) continue;
                String segment = stringArray[i];
                if (i > 0) {
                    char c = segment.charAt(0);
                    ChatFormatting fmt = ChatFormatting.getByCode(c);
                    if (fmt != null) {
                        style = style.applyLegacyFormat(fmt);
                    }
                    segment = segment.substring(1);
                }
                if (segment.isEmpty()) continue;
                arrayList.add(Component.literal(segment).withStyle(style));
            }
            float f6 = 0.0f;
            for (Component component2 : arrayList) {
                f6 += fontRenderer.getWidth(component2.getString());
            }
            f5 = f - f6 / 2.0f;
            for (Component component3 : arrayList) {
                String text = component3.getString();
                Style style2 = component3.getStyle();
                TextColor textColor = style2.getColor();
                int n = textColor != null ? textColor.getValue() : Color.WHITE.getRGB();
                paint.setColor(this.colorWithAlpha(n, f3));
                drawContext.drawString(text, f5, f4, fontRenderer, paint);
                f5 += fontRenderer.getWidth(text);
            }
            f4 += 11.0f;
        }
    }

    private float getComponentWidth(Component component, FontRenderer fontRenderer) {
        float f = 0.0f;
        for (String string : component.getString().split("\n")) {
            String string2 = ChatFormatting.stripFormatting(string);
            if (string2 == null) continue;
            f = Math.max(f, fontRenderer.getWidth(string2));
        }
        return f;
    }

    @Override
    public boolean isVisible() {
        return mc != null && mc.options.keyPlayerList.isDown();
    }

    @Override
    public IHudElement.Size getHudAlignment() {
        float f;
        float f2;
        float f3;
        int n;
        if (mc == null || mc.getConnection() == null) {
            return new IHudElement.Size(200.0f, 30.0f);
        }
        int n2 = mc.getConnection().getOnlinePlayers().size();
        Component component = TabListInfo.header;
        Component component2 = TabListInfo.footer;
        float f4 = 0.0f;
        f4 = component != null && !component.getString().isEmpty() ? (float)component.getString().split("\n").length * 11.0f : 20.0f;
        float f5 = 0.0f;
        if (component2 != null && !component2.getString().isEmpty()) {
            f5 = (float)component2.getString().split("\n").length * 11.0f + 10.0f;
        }
        int n3 = 20;
        int n4 = Math.max(1, (int)Math.ceil((double)n2 / (double)n3));
        int n5 = n = n2 > 0 ? Math.min(n2, n3) : 0;
        if (n4 > 1) {
            f3 = (float)n4 * 140.0f + (float)(n4 - 1) * 10.0f;
        } else {
            f2 = 0.0f;
            if (mc.getConnection() != null) {
                for (PlayerInfo playerInfo : mc.getConnection().getOnlinePlayers()) {
                    String string = playerInfo.getProfile().getName();
                    String string2 = playerInfo.getLatency() + "ms";
                    f = 12.0f + nameFont.getWidth(string) + 5.0f + nameFont.getWidth(string2);
                    f2 = Math.max(f2, f);
                }
            }
            f3 = f2;
        }
        f2 = 0.0f;
        if (component != null) {
            f2 = this.getComponentWidth(component, titleFont);
        }
        float f6 = 0.0f;
        if (component2 != null) {
            f6 = this.getComponentWidth(component2, titleFont);
        }
        float f7 = Math.max(f3, Math.max(f2, f6)) + 20.0f;
        float f8 = f4 + (float)n * 11.0f + f5 + 20.0f;
        if (n2 == 0 && component2 == null) {
            f8 += 10.0f;
        }
        if (n2 > 0 && component != null) {
            f8 += 10.0f;
        }
        float f9 = mc.getWindow().getGuiScaledWidth();
        f = mc.getWindow().getGuiScaledHeight();
        f7 = Math.min(f7, f9 * 0.9f);
        f8 = Math.min(f8, f * 0.9f);
        return new IHudElement.Size(f7, f8);
    }
}